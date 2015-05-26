package org.droidplanner.services.android.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.enums.MAG_CAL_STATUS;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.action.ConnectionActions;
import com.o3dr.services.android.lib.drone.action.ExperimentalActions;
import com.o3dr.services.android.lib.drone.action.GuidedActions;
import com.o3dr.services.android.lib.drone.action.ParameterActions;
import com.o3dr.services.android.lib.drone.action.StateActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationProgress;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationResult;
import com.o3dr.services.android.lib.drone.camera.action.CameraActions;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.action.CalibrationActions;
import com.o3dr.services.android.lib.gcs.action.FollowMeActions;
import com.o3dr.services.android.lib.gcs.event.GCSEvent;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.model.IDroneApi;
import com.o3dr.services.android.lib.model.IMavlinkObserver;
import com.o3dr.services.android.lib.model.IObserver;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.core.util.Pair;
import org.droidplanner.services.android.R;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implementation for the IDroneApi interface.
 */
public final class DroneApi extends IDroneApi.Stub implements DroneEventsListener, IBinder.DeathRecipient {

    private final static String TAG = DroneApi.class.getSimpleName();

    private final Context context;
    private final DroneInterfaces.Handler droneHandler;

    private final ConcurrentLinkedQueue<IObserver> observersList;
    private final ConcurrentLinkedQueue<IMavlinkObserver> mavlinkObserversList;
    private DroneManager droneMgr;
    private final IApiListener apiListener;
    private final String ownerId;
    private final DroidPlannerService service;

    private ConnectionParameter connectionParams;

    DroneApi(DroidPlannerService dpService, Looper looper, IApiListener listener, String ownerId) {

        this.service = dpService;
        this.context = dpService.getApplicationContext();

        final Handler handler = new Handler(looper);

        this.droneHandler = new DroneInterfaces.Handler() {
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

        this.ownerId = ownerId;

        observersList = new ConcurrentLinkedQueue<>();
        mavlinkObserversList = new ConcurrentLinkedQueue<>();

        this.apiListener = listener;
        try {
            this.apiListener.asBinder().linkToDeath(this, 0);
            checkForSelfRelease();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            dpService.releaseDroneApi(this.ownerId);
        }
    }

    void destroy() {
        Log.d(TAG, "Destroying drone api instance for " + this.ownerId);
        this.observersList.clear();
        this.mavlinkObserversList.clear();

        this.apiListener.asBinder().unlinkToDeath(this, 0);

        try {
            this.service.disconnectDroneManager(this.droneMgr, this.ownerId);
        } catch (ConnectionException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public String getOwnerId() {
        return ownerId;
    }

    public DroneManager getDroneManager() {
        return this.droneMgr;
    }

    private Drone getDrone() {
        if (this.droneMgr == null)
            return null;

        return this.droneMgr.getDrone();
    }

    private Follow getFollowMe() {
        if (this.droneMgr == null)
            return null;

        return this.droneMgr.getFollowMe();
    }

    @Override
    public Bundle getAttribute(String type) throws RemoteException {
        Bundle carrier = new Bundle();
        final Drone drone = getDrone();

        switch (type) {
            case AttributeType.STATE:
                carrier.putParcelable(type, DroneApiUtils.getState(drone, isConnected()));
                break;
            case AttributeType.GPS:
                carrier.putParcelable(type, DroneApiUtils.getGps(drone));
                break;
            case AttributeType.PARAMETERS:
                carrier.putParcelable(type, DroneApiUtils.getParameters(drone, context));
                break;
            case AttributeType.SPEED:
                carrier.putParcelable(type, DroneApiUtils.getSpeed(drone));
                break;
            case AttributeType.ATTITUDE:
                carrier.putParcelable(type, DroneApiUtils.getAttitude(drone));
                break;
            case AttributeType.HOME:
                carrier.putParcelable(type, DroneApiUtils.getHome(drone));
                break;
            case AttributeType.BATTERY:
                carrier.putParcelable(type, DroneApiUtils.getBattery(drone));
                break;
            case AttributeType.ALTITUDE:
                carrier.putParcelable(type, DroneApiUtils.getAltitude(drone));
                break;
            case AttributeType.MISSION:
                carrier.putParcelable(type, DroneApiUtils.getMission(drone));
                break;
            case AttributeType.SIGNAL:
                carrier.putParcelable(type, DroneApiUtils.getSignal(drone));
                break;
            case AttributeType.TYPE:
                carrier.putParcelable(type, DroneApiUtils.getType(drone));
                break;
            case AttributeType.GUIDED_STATE:
                carrier.putParcelable(type, DroneApiUtils.getGuidedState(drone));
                break;
            case AttributeType.FOLLOW_STATE:
                carrier.putParcelable(type, DroneApiUtils.getFollowState(getFollowMe()));
                break;
            case AttributeType.CAMERA:
                carrier.putParcelable(type, DroneApiUtils.getCameraProxy(drone, service.getCameraDetails()));
                break;

            case AttributeType.GOPRO:
                carrier.putParcelable(type, DroneApiUtils.getGoPro(drone));
                break;

            case AttributeType.MAGNETOMETER_CALIBRATION_STATUS:
                carrier.putParcelable(type, DroneApiUtils.getMagnetometerCalibrationStatus(drone));
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
            Log.w(TAG, "Client is not longer available.");
            this.context.startService(new Intent(this.context, DroidPlannerService.class)
                    .setAction(DroidPlannerService.ACTION_RELEASE_API_INSTANCE)
                    .putExtra(DroidPlannerService.EXTRA_API_INSTANCE_APP_ID, this.ownerId));
        }
    }

    @Override
    public void addAttributesObserver(IObserver observer) throws RemoteException {
        if (observer != null)
            observersList.add(observer);
    }

    @Override
    public void removeAttributesObserver(IObserver observer) throws RemoteException {
        if (observer != null) {
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
    public void performAction(Action action) throws RemoteException {
        if (action == null)
            return;

        final String type = action.getType();
        if (type == null)
            return;

        Bundle data = action.getData();
        switch (type) {
            // MISSION ACTIONS
            case MissionActions.ACTION_GENERATE_DRONIE:
                final float bearing = DroneApiUtils.generateDronie(getDrone());
                if (bearing != -1) {
                    Bundle bundle = new Bundle(1);
                    bundle.putFloat(AttributeEventExtra.EXTRA_MISSION_DRONIE_BEARING, bearing);
                    notifyAttributeUpdate(AttributeEvent.MISSION_DRONIE_CREATED, bundle);
                }
                break;

            case MissionActions.ACTION_LOAD_WAYPOINTS:
                DroneApiUtils.loadWaypoints(getDrone());
                break;

            case MissionActions.ACTION_SET_MISSION:
                data.setClassLoader(Mission.class.getClassLoader());
                Mission mission = data.getParcelable(MissionActions.EXTRA_MISSION);
                boolean pushToDrone = data.getBoolean(MissionActions.EXTRA_PUSH_TO_DRONE);
                DroneApiUtils.setMission(getDrone(), mission, pushToDrone);
                break;

            case MissionActions.ACTION_BUILD_COMPLEX_MISSION_ITEM:
                DroneApiUtils.buildComplexMissionItem(getDrone(), data);
                break;

            //CONNECTION ACTIONS
            case ConnectionActions.ACTION_CONNECT:
                data.setClassLoader(ConnectionParameter.class.getClassLoader());
                ConnectionParameter parameter = data.getParcelable(ConnectionActions.EXTRA_CONNECT_PARAMETER);
                connect(parameter);
                break;

            case ConnectionActions.ACTION_DISCONNECT:
                disconnect();
                break;

            //EXPERIMENTAL ACTIONS
            case ExperimentalActions.ACTION_EPM_COMMAND:
                boolean release = data.getBoolean(ExperimentalActions.EXTRA_EPM_RELEASE);
                DroneApiUtils.epmCommand(getDrone(), release);
                break;

            case ExperimentalActions.ACTION_TRIGGER_CAMERA:
                DroneApiUtils.triggerCamera(getDrone());
                break;

            case ExperimentalActions.ACTION_SEND_MAVLINK_MESSAGE:
                data.setClassLoader(MavlinkMessageWrapper.class.getClassLoader());
                MavlinkMessageWrapper messageWrapper = data.getParcelable(ExperimentalActions.EXTRA_MAVLINK_MESSAGE);
                DroneApiUtils.sendMavlinkMessage(getDrone(), messageWrapper);
                break;

            case ExperimentalActions.ACTION_SET_RELAY:
                if (droneMgr != null) {
                    int relayNumber = data.getInt(ExperimentalActions.EXTRA_RELAY_NUMBER);
                    boolean isOn = data.getBoolean(ExperimentalActions.EXTRA_IS_RELAY_ON);
                    MavLinkDoCmds.setRelay(droneMgr.getDrone(), relayNumber, isOn);
                }
            case ExperimentalActions.ACTION_SET_SERVO:
                if (droneMgr != null) {
                    int channel = data.getInt(ExperimentalActions.EXTRA_SERVO_CHANNEL);
                    int pwm = data.getInt(ExperimentalActions.EXTRA_SERVO_PWM);
                    MavLinkDoCmds.setServo(droneMgr.getDrone(), channel, pwm);
                }
                break;

            //GUIDED ACTIONS
            case GuidedActions.ACTION_DO_GUIDED_TAKEOFF:
                double takeoffAltitude = data.getDouble(GuidedActions.EXTRA_ALTITUDE);
                DroneApiUtils.doGuidedTakeoff(getDrone(), takeoffAltitude);
                break;

            case GuidedActions.ACTION_SEND_GUIDED_POINT:
                data.setClassLoader(LatLong.class.getClassLoader());
                boolean force = data.getBoolean(GuidedActions.EXTRA_FORCE_GUIDED_POINT);
                LatLong guidedPoint = data.getParcelable(GuidedActions.EXTRA_GUIDED_POINT);
                DroneApiUtils.sendGuidedPoint(getDrone(), guidedPoint, force);
                break;

            case GuidedActions.ACTION_SET_GUIDED_ALTITUDE:
                double guidedAltitude = data.getDouble(GuidedActions.EXTRA_ALTITUDE);
                DroneApiUtils.setGuidedAltitude(getDrone(), guidedAltitude);
                break;

            //PARAMETER ACTIONS
            case ParameterActions.ACTION_REFRESH_PARAMETERS:
                DroneApiUtils.refreshParameters(getDrone());
                break;

            case ParameterActions.ACTION_WRITE_PARAMETERS:
                data.setClassLoader(Parameters.class.getClassLoader());
                Parameters parameters = data.getParcelable(ParameterActions.EXTRA_PARAMETERS);
                DroneApiUtils.writeParameters(getDrone(), parameters);
                break;

            //DRONE STATE ACTIONS
            case StateActions.ACTION_ARM:
                boolean doArm = data.getBoolean(StateActions.EXTRA_ARM);
                DroneApiUtils.arm(getDrone(), doArm);
                break;

            case StateActions.ACTION_SET_VEHICLE_MODE:
                data.setClassLoader(VehicleMode.class.getClassLoader());
                VehicleMode newMode = data.getParcelable(StateActions.EXTRA_VEHICLE_MODE);
                DroneApiUtils.changeVehicleMode(getDrone(), newMode);
                break;

            //CALIBRATION ACTIONS
            case CalibrationActions.ACTION_START_IMU_CALIBRATION:
                if (!DroneApiUtils.startIMUCalibration(getDrone())) {
                    Bundle extrasBundle = new Bundle(1);
                    extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE,
                            context.getString(R.string.failed_start_calibration_message));
                    notifyAttributeUpdate(AttributeEvent.CALIBRATION_IMU_ERROR, extrasBundle);
                }
                break;

            case CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK:
                int imuAck = data.getInt(CalibrationActions.EXTRA_IMU_STEP);
                DroneApiUtils.sendIMUCalibrationAck(getDrone(), imuAck);
                break;

            case CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION:
                final boolean retryOnFailure = data.getBoolean(CalibrationActions.EXTRA_RETRY_ON_FAILURE, false);
                final boolean saveAutomatically = data.getBoolean(CalibrationActions.EXTRA_SAVE_AUTOMATICALLY, true);
                final int startDelay = data.getInt(CalibrationActions.EXTRA_START_DELAY, 0);
                DroneApiUtils.startMagnetometerCalibration(getDrone(), retryOnFailure, saveAutomatically, startDelay);
                break;

            case CalibrationActions.ACTION_CANCEL_MAGNETOMETER_CALIBRATION:
                DroneApiUtils.cancelMagnetometerCalibration(getDrone());
                break;

            case CalibrationActions.ACTION_ACCEPT_MAGNETOMETER_CALIBRATION:
                DroneApiUtils.acceptMagnetometerCalibration(getDrone());
                break;

            //FOLLOW-ME ACTIONS
            case FollowMeActions.ACTION_ENABLE_FOLLOW_ME:
                data.setClassLoader(FollowType.class.getClassLoader());
                FollowType followType = data.getParcelable(FollowMeActions.EXTRA_FOLLOW_TYPE);
                DroneApiUtils.enableFollowMe(getDroneManager(), droneHandler, followType);
                break;

            case FollowMeActions.ACTION_UPDATE_FOLLOW_PARAMS:
                if (droneMgr != null) {
                    data.setClassLoader(LatLong.class.getClassLoader());

                    final FollowAlgorithm followAlgorithm = this.droneMgr.getFollowMe().getFollowAlgorithm();
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
                break;

            case FollowMeActions.ACTION_DISABLE_FOLLOW_ME:
                DroneApiUtils.disableFollowMe(getFollowMe());
                break;

            //************ CAMERA ACTIONS *************//
            case CameraActions.ACTION_START_VIDEO_RECORDING:
                DroneApiUtils.startVideoRecording(getDrone());
                break;

            case CameraActions.ACTION_STOP_VIDEO_RECORDING:
                DroneApiUtils.stopVideoRecording(getDrone());
                break;
        }
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
                    Log.e(TAG, e.getMessage(), e);
                    try {
                        removeAttributesObserver(observer);
                    } catch (RemoteException e1) {
                        Log.e(TAG, e1.getMessage(), e1);
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
                Log.w(TAG, "Unable to forward connection fail to client.", e);
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
                    Log.e(TAG, e.getMessage(), e);
                    try {
                        removeMavlinkObserver(observer);
                    } catch (RemoteException e1) {
                        Log.e(TAG, e1.getMessage(), e1);
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
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
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

            case NAVIGATION:
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
                    accelCalibration.setCalibrating(false);
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

            case FOLLOW_START:
                droneEvent = AttributeEvent.FOLLOW_START;
                break;

            case FOLLOW_STOP:
                droneEvent = AttributeEvent.FOLLOW_STOP;
                break;

            case FOLLOW_UPDATE:
            case FOLLOW_CHANGE_TYPE:
                droneEvent = AttributeEvent.FOLLOW_UPDATE;
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

            case GOPRO_STATUS_UPDATE:
                droneEvent = AttributeEvent.GOPRO_STATE_UPDATED;
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
                DroneApiUtils.getMagnetometerCalibrationProgress(progress));

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_PROGRESS, progressBundle);
    }

    @Override
    public void onCalibrationCompleted(msg_mag_cal_report report) {
        Bundle reportBundle = new Bundle(1);
        reportBundle.putParcelable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_RESULT,
                DroneApiUtils.getMagnetometerCalibrationResult(report));

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_COMPLETED, reportBundle);
    }
}
