package org.droidplanner.services.android.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Surface;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.o3dr.services.android.lib.drone.action.ConnectionActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.action.CameraActions;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.gcs.event.GCSEvent;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.IDroneApi;
import com.o3dr.services.android.lib.model.IMavlinkObserver;
import com.o3dr.services.android.lib.model.IObserver;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.autopilot.Drone;
import org.droidplanner.services.android.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.core.parameters.Parameter;
import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.core.drone.DroneEventsListener;
import org.droidplanner.services.android.utils.CommonApiUtils;
import org.droidplanner.services.android.utils.video.VideoManager;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import timber.log.Timber;

/**
 * Implementation for the IDroneApi interface.
 */
public final class DroneApi extends IDroneApi.Stub implements DroneEventsListener, IBinder.DeathRecipient {

    private final Context context;

    private final ConcurrentLinkedQueue<IObserver> observersList;
    private final ConcurrentLinkedQueue<IMavlinkObserver> mavlinkObserversList;
    private DroneManager droneMgr;
    private final IApiListener apiListener;
    private final String ownerId;
    private final DroidPlannerService service;

    private ConnectionParameter connectionParams;

    DroneApi(DroidPlannerService dpService, IApiListener listener, String ownerId) {

        this.service = dpService;
        this.context = dpService.getApplicationContext();

        this.ownerId = ownerId;

        observersList = new ConcurrentLinkedQueue<>();
        mavlinkObserversList = new ConcurrentLinkedQueue<>();

        this.apiListener = listener;
        try {
            this.apiListener.asBinder().linkToDeath(this, 0);
            checkForSelfRelease();
        } catch (RemoteException e) {
            Timber.e(e, e.getMessage());
            dpService.releaseDroneApi(this.ownerId);
        }
    }

    @Override
    public int getApiVersionCode(){
        try {
            return apiListener.getApiVersionCode();
        } catch (RemoteException e) {
            Timber.e(e, e.getMessage());
        }

        return -1;
    }

    @Override
    public int getClientVersionCode(){
        try {
            return apiListener.getClientVersionCode();
        } catch (RemoteException e) {
            Timber.e(e, e.getMessage());
        }
        return -1;
    }

    void destroy() {
        Timber.d("Destroying drone api instance for %s", this.ownerId);
        this.observersList.clear();
        this.mavlinkObserversList.clear();

        try {
            this.apiListener.asBinder().unlinkToDeath(this, 0);
        } catch (NoSuchElementException e) {
            Timber.e(e, e.getMessage());
        }

        try {
            this.service.disconnectDroneManager(this.droneMgr, this.ownerId);
        } catch (ConnectionException e) {
            Timber.e(e, e.getMessage());
        }
    }

    public String getOwnerId() {
        return ownerId;
    }

    public DroneManager getDroneManager() {
        return this.droneMgr;
    }

    private MavLinkDrone getDrone(){
        if(this.droneMgr == null)
            return null;

        return this.droneMgr.getDrone();
    }

    @Override
    public Bundle getAttribute(String type) throws RemoteException {
        final Bundle carrier = new Bundle();

        switch(type){
            case AttributeType.CAMERA:
                carrier.putParcelable(type, CommonApiUtils.getCameraProxy(getDrone(), service.getCameraDetails()));
            break;

            default:
                if(droneMgr != null) {
                    final DroneAttribute attribute = droneMgr.getAttribute(type);
                    if (attribute != null)
                        carrier.putParcelable(type, attribute);
                }
                break;
        }
        return carrier;
    }

    public boolean isConnected() {
        return droneMgr != null && droneMgr.isConnected();
    }

    public void connect(ConnectionParameter connParams) {
        try {
            this.connectionParams = connParams;
            this.droneMgr = service.connectDroneManager(connParams, ownerId, this);
        } catch (ConnectionException e) {
            notifyConnectionFailed(new ConnectionResult(0, e.getMessage()));
            disconnect();
        }
    }

    public void disconnect() {
        try {
            service.disconnectDroneManager(this.droneMgr, this.ownerId);
            this.droneMgr = null;
        } catch (ConnectionException e) {
            notifyConnectionFailed(new ConnectionResult(0, e.getMessage()));
        }
    }

    private void checkForSelfRelease() {
        //Check if the apiListener is still connected instead.
        if (!apiListener.asBinder().pingBinder()) {
            Timber.w("Client is not longer available.");
            this.context.startService(new Intent(this.context, DroidPlannerService.class)
                    .setAction(DroidPlannerService.ACTION_RELEASE_API_INSTANCE)
                    .putExtra(DroidPlannerService.EXTRA_API_INSTANCE_APP_ID, this.ownerId));
        }
    }

    @Override
    public void addAttributesObserver(IObserver observer) throws RemoteException {
        if (observer != null) {
            Timber.d("Adding attributes observer.");
            observersList.add(observer);
        }
    }

    @Override
    public void removeAttributesObserver(IObserver observer) throws RemoteException {
        if (observer != null) {
            Timber.d("Removing attributes observer.");
            observersList.remove(observer);
            checkForSelfRelease();
        }
    }

    @Override
    public void addMavlinkObserver(IMavlinkObserver observer) throws RemoteException {
        if (observer != null)
            mavlinkObserversList.add(observer);
    }

    @Override
    public void removeMavlinkObserver(IMavlinkObserver observer) throws RemoteException {
        if (observer != null) {
            mavlinkObserversList.remove(observer);
            checkForSelfRelease();
        }
    }

    @Override
    public void executeAction(Action action, ICommandListener listener) throws RemoteException {
        if (action == null)
            return;

        final String type = action.getType();
        if (type == null)
            return;

        Bundle data = action.getData();
        if (data != null)
            data.setClassLoader(context.getClassLoader());

        switch (type) {
            //CONNECTION ACTIONS
            case ConnectionActions.ACTION_CONNECT:
                ConnectionParameter parameter = data.getParcelable(ConnectionActions.EXTRA_CONNECT_PARAMETER);
                connect(parameter);
                break;

            case ConnectionActions.ACTION_DISCONNECT:
                disconnect();
                break;

            //CAMERA ACTIONS
            case CameraActions.ACTION_START_VIDEO_STREAM: {
                final Surface videoSurface = data.getParcelable(CameraActions.EXTRA_VIDEO_DISPLAY);
                final String videoTag = data.getString(CameraActions.EXTRA_VIDEO_TAG, "");

                Bundle videoProps = data.getBundle(CameraActions.EXTRA_VIDEO_PROPERTIES);
                if(videoProps == null){
                    //Only case where it's null is when interacting with a deprecated client version.
                    //In this case, we assume that the client is attempting to start a solo stream, since that's
                    //the only api that was exposed.
                    videoProps = new Bundle();
                    videoProps.putInt(CameraActions.EXTRA_VIDEO_PROPS_UDP_PORT, VideoManager.ARTOO_UDP_PORT);
                }

                CommonApiUtils.startVideoStream(getDrone(), videoProps, ownerId, videoTag, videoSurface, listener);
                break;
            }

            case CameraActions.ACTION_STOP_VIDEO_STREAM: {
                final String videoTag = data.getString(CameraActions.EXTRA_VIDEO_TAG, "");
                CommonApiUtils.stopVideoStream(getDrone(), ownerId, videoTag, listener);
                break;
            }

            // MISSION ACTIONS
            case MissionActions.ACTION_BUILD_COMPLEX_MISSION_ITEM:
                CommonApiUtils.buildComplexMissionItem(getDrone(), data);
                break;

            default:
                if(droneMgr != null) {
                    droneMgr.executeAsyncAction(action, listener);
                }else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                }
                break;
        }
    }

    @Override
    public void executeAsyncAction(Action action, ICommandListener listener) throws RemoteException {
        executeAction(action, listener);
    }

    @Override
    public void performAction(Action action) throws RemoteException {
        executeAction(action, null);
    }

    @Override
    public void performAsyncAction(Action action) throws RemoteException {
        performAction(action);
    }

    private void notifyAttributeUpdate(List<Pair<String, Bundle>> attributesInfo) {
        if (observersList.isEmpty() || attributesInfo == null || attributesInfo.isEmpty())
            return;

        for (Pair<String, Bundle> info : attributesInfo) {
            notifyAttributeUpdate(info.first, info.second);
        }
    }

    private void notifyAttributeUpdate(String attributeEvent, Bundle extrasBundle) {
        if (observersList.isEmpty())
            return;

        if (attributeEvent != null) {
            for (IObserver observer : observersList) {
                try {
                    observer.onAttributeUpdated(attributeEvent, extrasBundle);
                } catch (RemoteException e) {
                    Timber.e(e, e.getMessage());
                    try {
                        removeAttributesObserver(observer);
                    } catch (RemoteException e1) {
                        Timber.e(e, e1.getMessage());
                    }
                }
            }
        }
    }

    private void notifyConnectionFailed(ConnectionResult result) {
        if (result != null) {
            try {
                apiListener.onConnectionFailed(result);
                return;
            } catch (RemoteException e) {
                Timber.w(e, "Unable to forward connection fail to client.");
            }
            checkForSelfRelease();
        }
    }

    @Override
    public void onReceivedMavLinkMessage(MAVLinkMessage msg) {
        if (mavlinkObserversList.isEmpty())
            return;

        if (msg != null) {
            final MavlinkMessageWrapper msgWrapper = new MavlinkMessageWrapper(msg);
            for (IMavlinkObserver observer : mavlinkObserversList) {
                try {
                    observer.onMavlinkMessageReceived(msgWrapper);
                } catch (RemoteException e) {
                    Timber.e(e, e.getMessage());
                    try {
                        removeMavlinkObserver(observer);
                    } catch (RemoteException e1) {
                        Timber.e(e1, e1.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void onMessageLogged(int logLevel, String message) {
        final Bundle args = new Bundle(2);
        args.putInt(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE_LEVEL, logLevel);
        args.putString(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE, message);
        notifyAttributeUpdate(AttributeEvent.AUTOPILOT_MESSAGE, args);
    }

    @Override
    public void onAttributeEvent(String attributeEvent, Bundle eventInfo, boolean checkForSololinkApi) {
        if (TextUtils.isEmpty(attributeEvent))
            return;

        notifyAttributeUpdate(attributeEvent, eventInfo);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, MavLinkDrone drone) {
        Bundle extrasBundle = null;
        String droneEvent = null;
        final List<Pair<String, Bundle>> attributesInfo = new ArrayList<>();

        switch (event) {
            case DISCONNECTED:
                //Broadcast the disconnection with the vehicle.
                context.sendBroadcast(new Intent(GCSEvent.ACTION_VEHICLE_DISCONNECTION)
                        .putExtra(GCSEvent.EXTRA_APP_ID, ownerId));

                droneEvent = AttributeEvent.STATE_DISCONNECTED;
                break;

            case GUIDEDPOINT:
                droneEvent = AttributeEvent.GUIDED_POINT_UPDATED;
                break;

            case RADIO:
                droneEvent = AttributeEvent.SIGNAL_UPDATED;
                break;

            case RC_IN:
                break;
            case RC_OUT:
                break;

            case ARMING_STARTED:
            case ARMING:
                droneEvent = AttributeEvent.STATE_ARMING;
                break;

            case AUTOPILOT_WARNING:
                extrasBundle = new Bundle(1);
                extrasBundle.putString(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID, drone.getState().getErrorId());
                droneEvent = AttributeEvent.AUTOPILOT_ERROR;
                break;

            case MODE:
                droneEvent = AttributeEvent.STATE_VEHICLE_MODE;
                break;

            case ATTITUDE:
            case ORIENTATION:
                droneEvent = AttributeEvent.ATTITUDE_UPDATED;
                break;

            case SPEED:
                droneEvent = AttributeEvent.SPEED_UPDATED;
                break;

            case BATTERY:
                droneEvent = AttributeEvent.BATTERY_UPDATED;
                break;

            case STATE:
                droneEvent = AttributeEvent.STATE_UPDATED;
                break;

            case MISSION_UPDATE:
                droneEvent = AttributeEvent.MISSION_UPDATED;
                break;

            case MISSION_RECEIVED:
                droneEvent = AttributeEvent.MISSION_RECEIVED;
                break;

            case FIRMWARE:
            case TYPE:
                droneEvent = AttributeEvent.TYPE_UPDATED;
                break;

            case HOME:
                droneEvent = AttributeEvent.HOME_UPDATED;
                break;

            case GPS:
                droneEvent = AttributeEvent.GPS_POSITION;
                break;

            case GPS_FIX:
                droneEvent = AttributeEvent.GPS_FIX;
                break;

            case GPS_COUNT:
                droneEvent = AttributeEvent.GPS_COUNT;
                break;

            case CALIBRATION_IMU:
                final String calIMUMessage = drone.getCalibrationSetup().getMessage();
                extrasBundle = new Bundle(1);
                extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE, calIMUMessage);
                droneEvent = AttributeEvent.CALIBRATION_IMU;
                break;

            case CALIBRATION_TIMEOUT:
                    /*
                 * here we will check if we are in calibration mode but if at
				 * the same time 'msg' is empty - then it is actually not doing
				 * calibration what we should do is to reset the calibration
				 * flag and re-trigger the HEARBEAT_TIMEOUT this however should
				 * not be happening
				 */
                final AccelCalibration accelCalibration = drone.getCalibrationSetup();
                final String message = accelCalibration.getMessage();
                if (accelCalibration.isCalibrating() && TextUtils.isEmpty(message)) {
                    accelCalibration.cancelCalibration();
                    droneEvent = AttributeEvent.HEARTBEAT_TIMEOUT;
                } else {
                    extrasBundle = new Bundle(1);
                    extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE, message);
                    droneEvent = AttributeEvent.CALIBRATION_IMU_TIMEOUT;
                }

                break;

            case HEARTBEAT_TIMEOUT:
                droneEvent = AttributeEvent.HEARTBEAT_TIMEOUT;
                break;

            case CONNECTING:
                droneEvent = AttributeEvent.STATE_CONNECTING;
                break;

            case CONNECTION_FAILED:
                disconnect();
                onConnectionFailed("");
                break;

            case HEARTBEAT_FIRST:
                final Bundle heartBeatExtras = new Bundle(1);
                heartBeatExtras.putInt(AttributeEventExtra.EXTRA_MAVLINK_VERSION, drone.getMavlinkVersion());
                attributesInfo.add(Pair.create(AttributeEvent.HEARTBEAT_FIRST, heartBeatExtras));

            case CONNECTED:
                //Broadcast the vehicle connection.
                final ConnectionParameter sanitizedParameter = new ConnectionParameter(connectionParams
                        .getConnectionType(), connectionParams.getParamsBundle(), null);

                context.sendBroadcast(new Intent(GCSEvent.ACTION_VEHICLE_CONNECTION)
                        .putExtra(GCSEvent.EXTRA_APP_ID, ownerId)
                        .putExtra(GCSEvent.EXTRA_VEHICLE_CONNECTION_PARAMETER, sanitizedParameter));

                attributesInfo.add(Pair.<String, Bundle>create(AttributeEvent.STATE_CONNECTED, null));
                break;

            case HEARTBEAT_RESTORED:
                extrasBundle = new Bundle(1);
                extrasBundle.putInt(AttributeEventExtra.EXTRA_MAVLINK_VERSION, drone.getMavlinkVersion());
                droneEvent = AttributeEvent.HEARTBEAT_RESTORED;
                break;

            case MISSION_SENT:
                droneEvent = AttributeEvent.MISSION_SENT;
                break;

            case INVALID_POLYGON:
                break;

            case MISSION_WP_UPDATE:
                final int currentWaypoint = drone.getMissionStats().getCurrentWP();
                extrasBundle = new Bundle(1);
                extrasBundle.putInt(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, currentWaypoint);
                droneEvent = AttributeEvent.MISSION_ITEM_UPDATED;
                break;

            case MISSION_WP_REACHED:
                final int lastReachedWaypoint = drone.getMissionStats().getLastReachedWP();
                extrasBundle = new Bundle(1);
                extrasBundle.putInt(AttributeEventExtra.EXTRA_MISSION_LAST_REACHED_WAYPOINT, lastReachedWaypoint);
                droneEvent = AttributeEvent.MISSION_ITEM_REACHED;
                break;

            case ALTITUDE:
                droneEvent = AttributeEvent.ALTITUDE_UPDATED;
                break;

            case WARNING_SIGNAL_WEAK:
                droneEvent = AttributeEvent.SIGNAL_WEAK;
                break;

            case WARNING_NO_GPS:
                droneEvent = AttributeEvent.WARNING_NO_GPS;
                break;

            case MAGNETOMETER:
                break;

            case FOOTPRINT:
                droneEvent = AttributeEvent.CAMERA_FOOTPRINTS_UPDATED;
                break;

            case EKF_STATUS_UPDATE:
                droneEvent = AttributeEvent.STATE_EKF_REPORT;
                break;

            case EKF_POSITION_STATE_UPDATE:
                droneEvent = AttributeEvent.STATE_EKF_POSITION;
                break;
        }

        if (droneEvent != null) {
            notifyAttributeUpdate(droneEvent, extrasBundle);
        }

        if (!attributesInfo.isEmpty()) {
            notifyAttributeUpdate(attributesInfo);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_REFRESH_STARTED, null);
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        Bundle paramsBundle = new Bundle(4);
        paramsBundle.putInt(AttributeEventExtra.EXTRA_PARAMETER_INDEX, index);
        paramsBundle.putInt(AttributeEventExtra.EXTRA_PARAMETERS_COUNT, count);
        paramsBundle.putString(AttributeEventExtra.EXTRA_PARAMETER_NAME, parameter.name);
        paramsBundle.putDouble(AttributeEventExtra.EXTRA_PARAMETER_VALUE, parameter.value);
        notifyAttributeUpdate(AttributeEvent.PARAMETER_RECEIVED, paramsBundle);
    }

    @Override
    public void onEndReceivingParameters() {
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_REFRESH_COMPLETED, null);
    }

    @Override
    public DroneSharePrefs getDroneSharePrefs() {
        if (connectionParams == null)
            return null;

        return connectionParams.getDroneSharePrefs();
    }

    @Override
    public void onConnectionFailed(String error) {
        notifyConnectionFailed(new ConnectionResult(0, error));
    }

    @Override
    public void binderDied() {
        checkForSelfRelease();
    }

    @Override
    public void onCalibrationCancelled() {
        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_CANCELLED, null);
    }

    @Override
    public void onCalibrationProgress(msg_mag_cal_progress progress) {
        Bundle progressBundle = new Bundle(1);
        progressBundle.putParcelable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_PROGRESS,
                CommonApiUtils.getMagnetometerCalibrationProgress(progress));

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_PROGRESS, progressBundle);
    }

    @Override
    public void onCalibrationCompleted(msg_mag_cal_report report) {
        Bundle reportBundle = new Bundle(1);
        reportBundle.putParcelable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_RESULT,
                CommonApiUtils.getMagnetometerCalibrationResult(report));

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_COMPLETED, reportBundle);
    }
}
