package org.droidplanner.services.android.core.drone;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.common.msg_command_ack;
import com.google.android.gms.location.LocationRequest;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.action.CapabilityActions;
import com.o3dr.services.android.lib.drone.action.StateActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.gcs.action.FollowMeActions;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.gcs.returnToMe.ReturnToMeState;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.api.MavLinkServiceApi;
import org.droidplanner.services.android.communication.connection.DroneshareClient;
import org.droidplanner.services.android.communication.service.MAVLinkClient;
import org.droidplanner.services.android.communication.service.UploaderService;
import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.services.android.core.drone.autopilot.Drone;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.core.drone.autopilot.apm.ArduCopter;
import org.droidplanner.services.android.core.drone.autopilot.apm.ArduPlane;
import org.droidplanner.services.android.core.drone.autopilot.apm.ArduRover;
import org.droidplanner.services.android.core.drone.autopilot.apm.ArduSolo;
import org.droidplanner.services.android.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.core.drone.autopilot.px4.Px4Native;
import org.droidplanner.services.android.core.drone.companion.solo.SoloComp;
import org.droidplanner.services.android.core.drone.profiles.Parameters;
import org.droidplanner.services.android.core.drone.variables.StreamRates;
import org.droidplanner.services.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.gcs.GCSHeartbeat;
import org.droidplanner.services.android.core.gcs.ReturnToMe;
import org.droidplanner.services.android.core.gcs.follow.Follow;
import org.droidplanner.services.android.core.gcs.follow.FollowAlgorithm;
import org.droidplanner.services.android.core.gcs.location.FusedLocation;
import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.coordinates.Coord3D;
import org.droidplanner.services.android.core.parameters.Parameter;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.utils.AndroidApWarningParser;
import org.droidplanner.services.android.utils.CommonApiUtils;
import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * Bridge between the communication channel, the drone instance(s), and the connected client(s).
 */
public class DroneManager implements Drone, MAVLinkStreams.MavlinkInputStream, DroneInterfaces.OnDroneListener,
        DroneInterfaces.OnParameterManagerListener, LogMessageListener, MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener, DroneInterfaces.AttributeEventListener {

    private static final String TAG = DroneManager.class.getSimpleName();

    private static final int SOLOLINK_API_MIN_VERSION = 20412;

    private final ConcurrentHashMap<String, DroneEventsListener> connectedApps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DroneshareClient> tlogUploaders = new ConcurrentHashMap<>();

    private final Context context;
    private final Handler handler;

    private MavLinkDrone drone;
    private Follow followMe;
    private ReturnToMe returnToMe;

    private final MAVLinkClient mavClient;
    private final MavLinkMsgHandler mavLinkMsgHandler;
    private final DroneCommandTracker commandTracker;
    private final ConnectionParameter connectionParameter;

    private final GCSHeartbeat gcsHeartbeat;

    public DroneManager(Context context, ConnectionParameter connParams, final Handler handler,
                        MavLinkServiceApi mavlinkApi) {
        this.context = context;
        this.handler = handler;
        this.connectionParameter = connParams;

        commandTracker = new DroneCommandTracker(handler);

        mavClient = new MAVLinkClient(context, this, connParams, mavlinkApi);
        mavClient.setCommandTracker(commandTracker);

        this.gcsHeartbeat = new GCSHeartbeat(mavClient, 1);

        this.mavLinkMsgHandler = new MavLinkMsgHandler(this);
    }

    public void onVehicleTypeReceived(FirmwareType type) {
        if (drone != null) {
            return;
        }

        final DroidPlannerPrefs dpPrefs = new DroidPlannerPrefs(context);

        switch (type) {
            case ARDU_COPTER:
                if (isCompanionComputerEnabled()) {
                    Timber.i("Instantiating ArduSolo autopilot.");
                    this.drone = new ArduSolo(context, mavClient, handler, dpPrefs, new AndroidApWarningParser(), this, this);
                } else {
                    Timber.i("Instantiating ArduCopter autopilot.");
                    this.drone = new ArduCopter(context, mavClient, handler, dpPrefs, new AndroidApWarningParser(), this, this);
                }
                break;

            case ARDU_SOLO:
                Timber.i("Instantiating ArduCopter autopilot.");
                this.drone = new ArduSolo(context, mavClient, handler, dpPrefs, new AndroidApWarningParser(), this, this);
                break;

            case ARDU_PLANE:
                Timber.i("Instantiating ArduPlane autopilot.");
                this.drone = new ArduPlane(context, mavClient, handler, dpPrefs, new AndroidApWarningParser(), this, this);
                break;

            case ARDU_ROVER:
                Timber.i("Instantiating ArduPlane autopilot.");
                this.drone = new ArduRover(context, mavClient, handler, dpPrefs, new AndroidApWarningParser(), this, this);
                break;

            case PX4_NATIVE:
                Timber.i("Instantiating PX4 Native autopilot.");
                this.drone = new Px4Native(handler, mavClient, new AndroidApWarningParser(), this);
                break;
        }

        this.followMe = new Follow(this, handler, new FusedLocation(context, handler));
        this.returnToMe = new ReturnToMe(this, new FusedLocation(context, handler,
                LocationRequest.PRIORITY_HIGH_ACCURACY, 1000L, 1000L, ReturnToMe.UPDATE_MINIMAL_DISPLACEMENT), this);

        final StreamRates streamRates = drone.getStreamRates();
        if (streamRates != null) {
            streamRates.setRates(dpPrefs.getRates());
        }

        drone.addDroneListener(this);

        final Parameters parameters = drone.getParameters();
        if (parameters != null) {
            parameters.setParameterListener(this);
        }

        final MagnetometerCalibrationImpl magnetometer = drone.getMagnetometerCalibration();
        if (magnetometer != null) {
            magnetometer.setListener(this);
        }
    }

    private void destroyAutopilot() {
        if (drone == null)
            return;

        drone.removeDroneListener(this);

        final Parameters parameters = drone.getParameters();
        if (parameters != null)
            parameters.setParameterListener(null);

        final MagnetometerCalibrationImpl magnetometer = drone.getMagnetometerCalibration();
        if (magnetometer != null)
            magnetometer.setListener(null);

        if (drone instanceof ArduSolo) {
            ((ArduSolo) drone).getSoloComp().destroy();
        }

        drone = null;
    }

    public void destroy() {
        Log.d(TAG, "Destroying drone manager.");

        destroyAutopilot();
        disconnect();

        connectedApps.clear();
        tlogUploaders.clear();

        if (followMe != null && followMe.isEnabled())
            followMe.toggleFollowMeState();

        if (returnToMe != null)
            returnToMe.disable();
    }

    public void connect(String appId, DroneEventsListener listener) throws ConnectionException {
        if (listener == null || TextUtils.isEmpty(appId))
            return;

        connectedApps.put(appId, listener);

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
    private boolean isCompanionComputerEnabled() {
        return drone instanceof ArduSolo
                || this.connectionParameter.getConnectionType() == ConnectionType.TYPE_UDP
                && SoloComp.isAvailable(context)
                && doAnyListenersSupportSoloLinkApi();

    }

    public int getConnectedAppsCount() {
        return connectedApps.size();
    }

    public void disconnect(String appId) throws ConnectionException {
        if (TextUtils.isEmpty(appId))
            return;

        if (drone instanceof GenericMavLinkDrone) {
            ((GenericMavLinkDrone)drone).tryStoppingVideoStream(appId);
        }

        Log.d(TAG, "Disconnecting client " + appId);
        DroneEventsListener listener = connectedApps.remove(appId);

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
        if (drone != null)
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
        this.gcsHeartbeat.setActive(true);

        // Start a new ga analytics session. The new session will be tagged
        // with the mavlink connection mechanism, as well as whether the user has an active droneshare account.
        GAUtils.startNewSession(null);

        if (!connectedApps.isEmpty()) {
            for (Map.Entry<String, DroneEventsListener> entry : connectedApps.entrySet()) {
                notifyConnected(entry.getKey(), entry.getValue());
            }
        }
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
        this.gcsHeartbeat.setActive(false);

        if (!connectedApps.isEmpty()) {
            for (Map.Entry<String, DroneEventsListener> entry : connectedApps.entrySet()) {
                notifyDisconnected(entry.getKey(), entry.getValue());
            }
        }

        notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED);
    }

    private void notifyDroneEvent(DroneInterfaces.DroneEventsType event) {
        if (drone != null) {
            drone.notifyDroneEvent(event);
        }
    }

    private void handleCommandAck(msg_command_ack ack) {
        if (ack != null) {
            commandTracker.onCommandAck(msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK, ack);
        }
    }

    @Override
    public void notifyReceivedData(MAVLinkPacket packet) {
        MAVLinkMessage receivedMsg = packet.unpack();
        if (receivedMsg == null)
            return;

        if (receivedMsg.msgid == msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK) {
            final msg_command_ack commandAck = (msg_command_ack) receivedMsg;
            handleCommandAck(commandAck);
        } else {
            this.mavLinkMsgHandler.receiveData(receivedMsg);
            if (this.drone != null) {
                this.drone.onMavLinkMessageReceived(receivedMsg);
            }
        }

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

    public MavLinkDrone getDrone() {
        return this.drone;
    }

    public Follow getFollowMe() {
        return followMe;
    }

    public boolean isConnected() {
        return drone != null && drone.isConnected();
    }

    @Override
    public DroneAttribute getAttribute(String attributeType) {
        switch (attributeType) {
            case AttributeType.FOLLOW_STATE:
                return CommonApiUtils.getFollowState(followMe);

            case AttributeType.RETURN_TO_ME_STATE:
                return returnToMe == null ? new ReturnToMeState() : returnToMe.getState();

            default:
                return drone == null ? null : drone.getAttribute(attributeType);
        }
    }

    @Override
    public boolean executeAsyncAction(Action action, final ICommandListener listener) {
        final String type = action.getType();
        Bundle data = action.getData();

        switch (type) {
            // MISSION ACTIONS
            case MissionActions.ACTION_GENERATE_DRONIE:
                final float bearing = CommonApiUtils.generateDronie(drone);
                if (bearing != -1) {
                    Bundle bundle = new Bundle(1);
                    bundle.putFloat(AttributeEventExtra.EXTRA_MISSION_DRONIE_BEARING, bearing);
                    notifyDroneAttributeEvent(AttributeEvent.MISSION_DRONIE_CREATED, bundle);
                }
                return true;

            //FOLLOW-ME ACTIONS
            case FollowMeActions.ACTION_ENABLE_FOLLOW_ME:
                data.setClassLoader(FollowType.class.getClassLoader());
                FollowType followType = data.getParcelable(FollowMeActions.EXTRA_FOLLOW_TYPE);
                CommonApiUtils.enableFollowMe(this, handler, followType, listener);
                return true;

            case FollowMeActions.ACTION_UPDATE_FOLLOW_PARAMS:
                if (followMe != null) {
                    data.setClassLoader(LatLong.class.getClassLoader());

                    final FollowAlgorithm followAlgorithm = followMe.getFollowAlgorithm();
                    if (followAlgorithm != null) {
                        Map<String, Object> paramsMap = new HashMap<>();
                        Set<String> dataKeys = data.keySet();

                        for (String key : dataKeys) {
                            if (FollowType.EXTRA_FOLLOW_ROI_TARGET.equals(key)) {
                                LatLong target = data.getParcelable(key);
                                if (target != null) {
                                    final Coord2D roiTarget;
                                    if (target instanceof LatLongAlt) {
                                        roiTarget = new Coord3D(target.getLatitude(), target.getLongitude(),
                                                ((LatLongAlt) target).getAltitude());
                                    } else {
                                        roiTarget = new Coord2D(target.getLatitude(), target.getLongitude());
                                    }
                                    paramsMap.put(key, roiTarget);
                                }
                            } else
                                paramsMap.put(key, data.get(key));
                        }

                        followAlgorithm.updateAlgorithmParams(paramsMap);
                    }
                }
                return true;

            case FollowMeActions.ACTION_DISABLE_FOLLOW_ME:
                CommonApiUtils.disableFollowMe(followMe);
                return true;

            //************ RETURN TO ME ACTIONS *********//
            case StateActions.ACTION_ENABLE_RETURN_TO_ME:
                final boolean isEnabled = data.getBoolean(StateActions.EXTRA_IS_RETURN_TO_ME_ENABLED, false);
                if (returnToMe != null) {
                    if (isEnabled) {
                        returnToMe.enable(listener);
                    } else {
                        returnToMe.disable();
                    }
                    CommonApiUtils.postSuccessEvent(listener);
                } else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                }
                return true;

            //**************** CAPABILITY ACTIONS **************//
            case CapabilityActions.ACTION_CHECK_FEATURE_SUPPORT:
                if (listener != null) {
                    final String featureId = data.getString(CapabilityActions.EXTRA_FEATURE_ID);
                    if (!TextUtils.isEmpty(featureId)) {
                        switch (featureId) {

                            case CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING:
                            case CapabilityApi.FeatureIds.COMPASS_CALIBRATION:
                                if (this.isCompanionComputerEnabled()) {
                                    CommonApiUtils.postSuccessEvent(listener);
                                } else {
                                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                                }
                                break;

                            case CapabilityApi.FeatureIds.KILL_SWITCH:
                                if (CommonApiUtils.isKillSwitchSupported(drone)) {
                                    CommonApiUtils.postSuccessEvent(listener);
                                } else {
                                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                                }
                                break;

                            default:
                                CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                                break;
                        }
                    }
                }
                return true;

            default:
                if (drone != null) {
                    return drone.executeAsyncAction(action, listener);
                } else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                    return true;
                }
        }
    }

    private void notifyDroneAttributeEvent(String attributeEvent, Bundle eventInfo) {
        notifyDroneAttributeEvent(attributeEvent, eventInfo, false);
    }

    /**
     * Temporary delegate to prevent sending of newly defined payload to older version of the api
     * #FIXME: remove when old version of the api is phased out.
     *
     * @param attributeEvent
     * @param eventInfo
     * @param checkForSoloLinkApi
     */
    private void notifyDroneAttributeEvent(String attributeEvent, Bundle eventInfo, boolean checkForSoloLinkApi) {
        if (TextUtils.isEmpty(attributeEvent) || connectedApps.isEmpty())
            return;

        for (DroneEventsListener listener : connectedApps.values()) {
            if (checkForSoloLinkApi && !supportSoloLinkApi(listener)) {
                continue;
            }
            listener.onAttributeEvent(attributeEvent, eventInfo, checkForSoloLinkApi);
        }
    }

    private boolean supportSoloLinkApi(DroneEventsListener listener) {
        return listener != null && listener.getApiVersionCode() >= SOLOLINK_API_MIN_VERSION;
    }

    /**
     * FIXME: remove when android solo v2 is released.
     *
     * @return
     */
    private boolean doAnyListenersSupportSoloLinkApi() {
        if (connectedApps.isEmpty())
            return false;

        for (DroneEventsListener listener : connectedApps.values()) {
            if (supportSoloLinkApi(listener))
                return true;
        }

        return false;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case HEARTBEAT_FIRST:
            case CONNECTED:
                event = DroneInterfaces.DroneEventsType.CONNECTED;
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
    public void onMessageLogged(int logLevel, String message) {
        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener listener : connectedApps.values()) {
            listener.onMessageLogged(logLevel, message);
        }
    }

    @Override
    public void onCalibrationCancelled() {
        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener listener : connectedApps.values())
            listener.onCalibrationCancelled();
    }

    @Override
    public void onCalibrationProgress(msg_mag_cal_progress progress) {
        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener listener : connectedApps.values())
            listener.onCalibrationProgress(progress);
    }

    @Override
    public void onCalibrationCompleted(msg_mag_cal_report report) {
        if (connectedApps.isEmpty())
            return;

        for (DroneEventsListener listener : connectedApps.values())
            listener.onCalibrationCompleted(report);
    }

    @Override
    public void onAttributeEvent(String attributeEvent, Bundle eventInfo, boolean checkForSololinkApi) {
        notifyDroneAttributeEvent(attributeEvent, eventInfo, checkForSololinkApi);
    }
}
