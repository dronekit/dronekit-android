package org.droidplanner.services.android.drone;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.enums.MAV_SEVERITY;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
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

    public DroneManager(Context context, ConnectionParameter connParams, final Handler handler, MavLinkServiceApi mavlinkApi) {
        this.context = context;
        this.connectionParameter = connParams;

        MAVLinkClient mavClient = new MAVLinkClient(context, this, connParams, mavlinkApi);

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

        this.followMe = new Follow(this.drone, dpHandler, new FusedLocation(context, handler));

        drone.addDroneListener(this);
        drone.getParameters().setParameterListener(this);
        drone.getMagnetometerCalibration().setListener(this);
    }

    public void destroy() {
        Log.d(TAG, "Destroying drone manager.");

        drone.removeDroneListener(this);
        drone.getParameters().setParameterListener(null);
        drone.getMagnetometerCalibration().setListener(null);

        disconnect();

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

        if (!mavClient.isConnected()) {
            mavClient.openConnection();
        } else {
            if (drone.isConnected()) {

                if (drone.isConnectionAlive())
                    listener.onDroneEvent(DroneInterfaces.DroneEventsType.HEARTBEAT_FIRST, drone);
                else {
                    listener.onDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED, drone);
                    listener.onDroneEvent(DroneInterfaces.DroneEventsType.HEARTBEAT_TIMEOUT, drone);
                }

                notifyConnected(appId, listener);
            }
        }

        mavClient.addLoggingFile(appId);
    }

    private void disconnect() {
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

    public Drone getDrone() {
        return this.drone;
    }

    public Follow getFollowMe() {
        return followMe;
    }

    public boolean isConnected() {
        return drone.isConnected();
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
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
