package org.droidplanner.services.android.drone;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.enums.MAV_SEVERITY;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.LogMessageListener;
import org.droidplanner.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.services.android.api.MavLinkServiceApi;
import org.droidplanner.services.android.communication.connection.DroneshareClient;
import org.droidplanner.services.android.communication.service.MAVLinkClient;
import org.droidplanner.services.android.communication.service.UploaderService;
import org.droidplanner.services.android.drone.companion.solo.SoloComp;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.location.FusedLocation;
import org.droidplanner.services.android.utils.AndroidApWarningParser;
import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridge between the communication channel, the drone instance(s), and the connected client(s).
 */
public class DroneManager implements MAVLinkStreams.MavlinkInputStream, DroneInterfaces.OnDroneListener,
        DroneInterfaces.OnParameterManagerListener, LogMessageListener, MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener {

    private static final String TAG = DroneManager.class.getSimpleName();

    private final ConcurrentHashMap<String, DroneEventsListener> connectedApps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DroneshareClient> tlogUploaders = new ConcurrentHashMap<>();

    private final Context context;
    private final Drone drone;
    private final Follow followMe;
    private final MavLinkMsgHandler mavLinkMsgHandler;
    private final ConnectionParameter connectionParameter;

    private final SoloComp soloComp;
    private final SoloComp.SoloCompListener soloCompListener = new SoloComp.SoloCompListener() {
        @Override
        public void onConnected() {
            if(isConnected()) {
                drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED);
            }
        }

        @Override
        public void onDisconnected() {
            drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED);
        }

        @Override
        public void onTlvPacketReceived(TLVPacket packet) {
            //TODO: filter the message that are broadcast.
            switch(packet.getMessageType()){
                case TLVMessageTypes.TYPE_ARTOO_INPUT_REPORT_MESSAGE:
                    //Drop this message as only the battery info is enabled, and that info is already
                    //available from the autopilot.
                    break;

                case TLVMessageTypes.TYPE_SOLO_GET_BUTTON_SETTING:
                case TLVMessageTypes.TYPE_SOLO_SET_BUTTON_SETTING:
                    //Drop these messages as they are already being handled by the 'onPresetButtonLoaded(...)' method.
                    break;

                default:
                    final Bundle messageInfo = new Bundle();
                    messageInfo.putParcelable(AttributeEventExtra.EXTRA_SOLOLINK_MESSAGE_DATA, packet);

                    notifyDroneAttributeEvent(AttributeEvent.SOLOLINK_MESSAGE_RECEIVED, messageInfo);
                    break;
            }
        }

        @Override
        public void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings) {
            notifyDroneAttributeEvent(AttributeEvent.SOLOLINK_BUTTON_SETTINGS_UPDATED, null);
        }

        @Override
        public void onWifiInfoUpdated(String wifiName, String wifiPassword) {
            notifyDroneAttributeEvent(AttributeEvent.SOLOLINK_WIFI_SETTINGS_UPDATED, null);
        }

        @Override
        public void onButtonPacketReceived(ButtonPacket packet) {
            final Bundle eventInfo = new Bundle();
            eventInfo.putParcelable(AttributeEventExtra.EXTRA_SOLOLINK_BUTTON_EVENT, packet);
            notifyDroneAttributeEvent(AttributeEvent.SOLOLINK_BUTTON_EVENT, eventInfo);
        }
    };

    public DroneManager(Context context, ConnectionParameter connParams, final Handler handler, MavLinkServiceApi mavlinkApi) {
        this.context = context;
        this.connectionParameter = connParams;

        final DroneCommandTracker commandTracker = new DroneCommandTracker(handler);

        MAVLinkClient mavClient = new MAVLinkClient(context, this, connParams, mavlinkApi);
        mavClient.setCommandTracker(commandTracker);

        DroneInterfaces.Clock clock = new DroneInterfaces.Clock() {
            @Override
            public long elapsedRealtime() {
                return SystemClock.elapsedRealtime();
            }
        };

        final DroneInterfaces.Handler dpHandler = new DroneInterfaces.Handler() {
            @Override
            public void removeCallbacks(Runnable thread) {
                handler.removeCallbacks(thread);
            }

            @Override
            public void post(Runnable thread) {
                handler.post(thread);
            }

            @Override
            public void postDelayed(Runnable thread, long timeout) {
                handler.postDelayed(thread, timeout);
            }
        };

        DroidPlannerPrefs dpPrefs = new DroidPlannerPrefs(context);

        this.drone = new DroneImpl(mavClient, clock, dpHandler, dpPrefs, new AndroidApWarningParser(), this);
        this.drone.getStreamRates().setRates(dpPrefs.getRates());

        this.mavLinkMsgHandler = new MavLinkMsgHandler(this.drone);
        this.mavLinkMsgHandler.setCommandTracker(commandTracker);

        this.followMe = new Follow(this.drone, dpHandler, new FusedLocation(context, handler));

        drone.addDroneListener(this);
        drone.getParameters().setParameterListener(this);
        drone.getMagnetometerCalibration().setListener(this);

        soloComp = new SoloComp(context, handler);
        soloComp.setListener(soloCompListener);
    }

    public SoloComp getSoloComp() {
        return soloComp;
    }

    public void destroy() {
        Log.d(TAG, "Destroying drone manager.");

        drone.removeDroneListener(this);
        drone.getParameters().setParameterListener(null);
        drone.getMagnetometerCalibration().setListener(null);

        disconnect();

        soloComp.destroy();

        connectedApps.clear();
        tlogUploaders.clear();

        if (followMe.isEnabled())
            followMe.toggleFollowMeState();
    }

    public void connect(String appId, DroneEventsListener listener) throws ConnectionException {
        if (listener == null || TextUtils.isEmpty(appId))
            return;

        connectedApps.put(appId, listener);

        MAVLinkClient mavClient = (MAVLinkClient) drone.getMavClient();

        if(isCompanionComputerEnabled() && !soloComp.isConnected()){
            soloComp.start();
        }

        if (!mavClient.isConnected()) {
            mavClient.openConnection();
        } else {
            if (isConnected()) {

                listener.onDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED, drone);
                if (!drone.isConnectionAlive())
                    listener.onDroneEvent(DroneInterfaces.DroneEventsType.HEARTBEAT_TIMEOUT, drone);

                notifyConnected(appId, listener);
            }
        }

        mavClient.addLoggingFile(appId);
    }

    private void disconnect() {
        if(isCompanionComputerEnabled())
            soloComp.stop();

        if (!connectedApps.isEmpty()) {
            for (String appId : connectedApps.keySet()) {
                try {
                    disconnect(appId);
                } catch (ConnectionException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * @return True if we can expect to find a companion computer on the connected channel.
     */
    public boolean isCompanionComputerEnabled(){
        return this.connectionParameter.getConnectionType() == ConnectionType.TYPE_UDP && soloComp.isAvailable();
    }

    public int getConnectedAppsCount() {
        return connectedApps.size();
    }

    public void disconnect(String appId) throws ConnectionException {
        if (TextUtils.isEmpty(appId))
            return;

        Log.d(TAG, "Disconnecting client " + appId);
        DroneEventsListener listener = connectedApps.remove(appId);

        final MAVLinkClient mavClient = (MAVLinkClient) drone.getMavClient();
        if (listener != null) {
            mavClient.removeLoggingFile(appId);

            listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
            notifyDisconnected(appId, listener);
        }

        if (mavClient.isConnected() && connectedApps.isEmpty()) {
            mavClient.closeConnection();
        }
    }

    @Override
    public void notifyStartingConnection() {
        onDroneEvent(DroneInterfaces.DroneEventsType.CONNECTING, drone);
    }

    private void notifyConnected(String appId, DroneEventsListener listener) {
        if (TextUtils.isEmpty(appId) || listener == null)
            return;

        final DroneSharePrefs droneSharePrefs = listener.getDroneSharePrefs();

        //TODO: restore live upload functionality when issue
        // 'https://github.com/diydrones/droneapi-java/issues/2' is fixed.
        boolean isLiveUploadEnabled = false; //droneSharePrefs.isLiveUploadEnabled();
        if (droneSharePrefs != null && isLiveUploadEnabled && droneSharePrefs.areLoginCredentialsSet()) {

            Log.i(TAG, "Starting live upload for " + appId);
            try {
                DroneshareClient uploader = tlogUploaders.get(appId);
                if (uploader == null) {
                    uploader = new DroneshareClient();
                    tlogUploaders.put(appId, uploader);
                }

                uploader.connect(droneSharePrefs.getUsername(), droneSharePrefs.getPassword());
            } catch (Exception e) {
                Log.e(TAG, "DroneShare uploader error for " + appId, e);
            }
        } else {
            Log.i(TAG, "Skipping live upload for " + appId);
        }
    }

    @Override
    public void notifyConnected() {
        // Start a new ga analytics session. The new session will be tagged
        // with the mavlink connection mechanism, as well as whether the user has an active droneshare account.
        GAUtils.startNewSession(null);

        if (!connectedApps.isEmpty()) {
            for (Map.Entry<String, DroneEventsListener> entry : connectedApps.entrySet()) {
                notifyConnected(entry.getKey(), entry.getValue());
            }
        }

        this.drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.CHECKING_VEHICLE_LINK);
    }

    public void kickStartDroneShareUpload() {
        // See if we can at least do a delayed upload
        if (!connectedApps.isEmpty()) {
            for (Map.Entry<String, DroneEventsListener> entry : connectedApps.entrySet()) {
                kickStartDroneShareUpload(entry.getKey(), entry.getValue().getDroneSharePrefs());
            }
        }
    }

    private void kickStartDroneShareUpload(String appId, DroneSharePrefs prefs) {
        if (TextUtils.isEmpty(appId) || prefs == null)
            return;

        UploaderService.kickStart(context, appId, prefs);
    }

    private void notifyDisconnected(String appId, DroneEventsListener listener) {
        if (TextUtils.isEmpty(appId) || listener == null)
            return;

        kickStartDroneShareUpload(appId, listener.getDroneSharePrefs());

        DroneshareClient uploader = tlogUploaders.remove(appId);
        if (uploader != null) {
            try {
                uploader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error while closing the drone share upload handler.", e);
            }
        }
    }

    @Override
    public void notifyDisconnected() {
        if (!connectedApps.isEmpty()) {
            for (Map.Entry<String, DroneEventsListener> entry : connectedApps.entrySet()) {
                notifyDisconnected(entry.getKey(), entry.getValue());
            }
        }

        this.drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED);
    }

    @Override
    public void notifyReceivedData(MAVLinkPacket packet) {
        MAVLinkMessage receivedMsg = packet.unpack();
        this.mavLinkMsgHandler.receiveData(receivedMsg);

        if (!connectedApps.isEmpty()) {
            for (DroneEventsListener droneEventsListener : connectedApps.values()) {
                droneEventsListener.onReceivedMavLinkMessage(receivedMsg);
            }
        }

        if (!tlogUploaders.isEmpty()) {
            final byte[] packetData = packet.encodePacket();
            for (DroneshareClient uploader : tlogUploaders.values()) {
                try {
                    uploader.filterMavlink(uploader.interfaceNum, packetData);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void onStreamError(String errorMsg) {
        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onConnectionFailed(errorMsg);
        }
    }

    private void notifyDroneAttributeEvent(String attributeEvent, Bundle eventInfo){
        if(TextUtils.isEmpty(attributeEvent) || connectedApps.isEmpty())
            return;

        for(DroneEventsListener listener: connectedApps.values()){
            listener.onAttributeEvent(attributeEvent, eventInfo);
        }
    }

    public Drone getDrone() {
        return this.drone;
    }

    public Follow getFollowMe() {
        return followMe;
    }

    public boolean isConnected() {
        return drone.isConnected() && (!isCompanionComputerEnabled() || soloComp.isConnected());
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch(event){
            case HEARTBEAT_FIRST:
                if(isCompanionComputerEnabled()) {
                    //Try connecting the companion computer
                    if(!soloComp.isConnected()) {
                        soloComp.start();
                        return;
                    }
                }

                event = DroneInterfaces.DroneEventsType.CONNECTED;
                break;

            case DISCONNECTED:
                if(isCompanionComputerEnabled()){
                    if(soloComp.isConnected()){
                        soloComp.stop();
                        return;
                    }
                }
                break;
        }

        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onDroneEvent(event, drone);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onBeginReceivingParameters();
        }
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onParameterReceived(parameter, index, count);
        }
    }

    @Override
    public void onEndReceivingParameters() {
        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onEndReceivingParameters();
        }
    }

    public ConnectionParameter getConnectionParameter() {
        return connectionParameter;
    }

    @Override
    public void onMessageLogged(int mavSeverity, String message) {
        if (connectedApps.isEmpty())
            return;

        final int logLevel;
        switch (mavSeverity) {
            case MAV_SEVERITY.MAV_SEVERITY_ALERT:
            case MAV_SEVERITY.MAV_SEVERITY_CRITICAL:
            case MAV_SEVERITY.MAV_SEVERITY_EMERGENCY:
            case MAV_SEVERITY.MAV_SEVERITY_ERROR:
                logLevel = Log.ERROR;
                break;

            case MAV_SEVERITY.MAV_SEVERITY_WARNING:
                logLevel = Log.WARN;
                break;

            case MAV_SEVERITY.MAV_SEVERITY_NOTICE:
                logLevel = Log.INFO;
                break;

            default:
            case MAV_SEVERITY.MAV_SEVERITY_INFO:
                logLevel = Log.VERBOSE;
                break;

            case MAV_SEVERITY.MAV_SEVERITY_DEBUG:
                logLevel = Log.DEBUG;
                break;
        }

        for (DroneEventsListener listener : connectedApps.values()) {
            listener.onMessageLogged(logLevel, message);
        }
    }

    @Override
    public void onCalibrationCancelled() {
        if(connectedApps.isEmpty())
            return;

        for(DroneEventsListener listener: connectedApps.values())
            listener.onCalibrationCancelled();
    }

    @Override
    public void onCalibrationProgress(msg_mag_cal_progress progress) {
        if(connectedApps.isEmpty())
            return;

        for(DroneEventsListener listener: connectedApps.values())
            listener.onCalibrationProgress(progress);
    }

    @Override
    public void onCalibrationCompleted(msg_mag_cal_report report) {
        if(connectedApps.isEmpty())
            return;

        for(DroneEventsListener listener: connectedApps.values())
            listener.onCalibrationCompleted(report);
    }
}
