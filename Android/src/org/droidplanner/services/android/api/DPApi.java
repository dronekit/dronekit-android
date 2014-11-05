package org.droidplanner.services.android.api;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.coordinate.LatLongAlt;
import com.ox3dr.services.android.lib.coordinate.Point3D;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.drone.connection.ConnectionResult;
import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.event.Extra;
import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.Home;
import com.ox3dr.services.android.lib.drone.property.Mission;
import com.ox3dr.services.android.lib.drone.property.Signal;
import com.ox3dr.services.android.lib.drone.property.Speed;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.Type;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;
import com.ox3dr.services.android.lib.drone.property.Parameters;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerApiCallback;

import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.Calibration;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.services.android.R;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.utils.MathUtil;
import org.droidplanner.services.android.utils.file.IO.ParameterMetadataLoader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

/**
* Created by fhuya on 10/30/14.
*/
final class DPApi extends IDroidPlannerApi.Stub implements DroneEventsListener, MagnetometerCalibration.OnMagCalibrationListener {

    private final static String TAG = DPApi.class.getSimpleName();

    private final Bundle emptyBundle = new Bundle();
    private final WeakReference<DroidPlannerService> serviceRef;
    private final Context context;

    private MagnetometerCalibration magCalibration;

    private IDroidPlannerApiCallback apiCallback;
    private ConnectionParameter connParams;
    private DroneManager droneMgr;

    DPApi(DroidPlannerService dpService, ConnectionParameter connParams,
          IDroidPlannerApiCallback callback) throws RemoteException {
        serviceRef = new WeakReference<DroidPlannerService>(dpService);
        this.context = dpService.getApplicationContext();

        this.apiCallback = callback;
        this.connParams = connParams;

        try {
            this.droneMgr = dpService.getDroneForConnection(connParams);
            this.magCalibration = new MagnetometerCalibration(this.droneMgr.getDrone(), this,
                    this.droneMgr.getHandler());
            start();
        } catch (ConnectionException e) {
            callback.onConnectionFailed(new ConnectionResult(0, e.getMessage()));
            disconnectFromDrone();
        }
    }

    private DroidPlannerService getService() {
        final DroidPlannerService service = serviceRef.get();
        if (service == null)
            throw new IllegalStateException("Lost reference to parent service.");

        return service;
    }

    private DroneManager getDroneMgr(){
        if(droneMgr == null)
            throw new IllegalStateException("Invalid state: drone manager is null");

        return droneMgr;
    }

    private IDroidPlannerApiCallback getCallback(){
        if(apiCallback == null)
            throw new IllegalStateException("Invalid state: api callback is null");

        return apiCallback;
    }

    private ConnectionParameter getConnectionParameter(){
        if(connParams == null)
            throw new IllegalStateException("Invalid state: connection parameter is null");

        return connParams;
    }

    private void start() throws RemoteException {
        getDroneMgr().addDroneEventsListener(this);
    }

    @Override
    public void disconnectFromDrone() throws RemoteException {
        getDroneMgr().removeDroneEventsListener(this);
        getService().disconnectFromApi(getConnectionParameter(), getCallback());
        this.apiCallback = null;
        this.connParams = null;
        this.droneMgr = null;
    }

    @Override
    public Gps getGps() throws RemoteException {
        final GPS droneGps = getDroneMgr().getDrone().getGps();
        LatLong dronePosition = droneGps.isPositionValid()
                ? new LatLong((float) droneGps.getPosition().getLat(), (float) droneGps.getPosition()
                .getLng())
                : null;

        return new Gps(dronePosition, (float) droneGps.getGpsEPH(), droneGps.getSatCount(),
                droneGps.getFixTypeNumeric());
    }

    @Override
    public State getState() throws RemoteException {
        final Drone drone =getDroneMgr().getDrone();
        org.droidplanner.core.drone.variables.State droneState = drone.getState();
        ApmModes droneMode = droneState.getMode();

        return new State(getProxyMode(droneMode), droneState.isArmed(),
                droneState.isFlying(), droneState.getWarning(), drone.getMavlinkVersion());
    }

    private static VehicleMode getProxyMode(ApmModes mode){
        final int proxyType = getDroneProxyType(mode.getType());
        if(proxyType == -1) return null;

        return new VehicleMode(mode.getNumber(), proxyType, mode.getName());
    }

    @Override
    public VehicleMode[] getAllVehicleModes() throws RemoteException {
        final int droneType = getDroneMgr().getDrone().getType();
        final int proxyType = getDroneProxyType(droneType);

        List<ApmModes> typeModes = ApmModes.getModeList(droneType);
        final int modesCount = typeModes.size();
        VehicleMode[] vehicleModes = new VehicleMode[modesCount];
        for(int i = 0; i < modesCount; i++){
            ApmModes mode = typeModes.get(i);
            vehicleModes[i] = new VehicleMode(mode.getNumber(), proxyType, mode.getName());
        }

        return vehicleModes;
    }

    private static int getDroneProxyType(int originalType){
        switch(originalType){
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return Type.TYPE_COPTER;

            case MAV_TYPE.MAV_TYPE_FIXED_WING:
                return Type.TYPE_PLANE;

            case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
            case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
                return Type.TYPE_ROVER;

            default:
                return -1;
        }
    }

    @Override
    public Parameters getParameters() throws RemoteException {
        final Drone drone =getDroneMgr().getDrone();
        final Map<String, com.ox3dr.services.android.lib.drone.property.Parameter> proxyParams =
                new HashMap<String, com.ox3dr.services.android.lib.drone.property.Parameter>();

        List<Parameter> droneParameters = drone.getParameters().getParametersList();
        if(!droneParameters.isEmpty()){
            for(Parameter param : droneParameters){
                proxyParams.put(param.name, new com.ox3dr.services.android.lib.drone.property
                        .Parameter(param.name, param.value, param.type));
            }

            try {
                //TODO: implement drone metadata type
                ParameterMetadataLoader.load(getService().getApplicationContext(), null, proxyParams);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return new Parameters(new ArrayList<com.ox3dr.services.android.lib.drone.property
                .Parameter>(proxyParams.values()));
    }

    @Override
    public Speed getSpeed() throws RemoteException {
        org.droidplanner.core.drone.variables.Speed droneSpeed =getDroneMgr().getDrone().getSpeed();
        return new Speed(droneSpeed.getVerticalSpeed().valueInMetersPerSecond(),
                droneSpeed.getGroundSpeed().valueInMetersPerSecond(),
                droneSpeed.getAirSpeed().valueInMetersPerSecond());
    }

    @Override
    public Attitude getAttitude() throws RemoteException {
        Orientation droneOrientation =getDroneMgr().getDrone().getOrientation();
        return new Attitude(droneOrientation.getRoll(), droneOrientation.getPitch(),
                droneOrientation.getYaw());
    }

    @Override
    public Home getHome() throws RemoteException {
        org.droidplanner.core.drone.variables.Home droneHome =getDroneMgr().getDrone().getHome();
        LatLongAlt homePosition = droneHome.isValid()
                ? new LatLongAlt((float) droneHome.getCoord().getLat(), (float) droneHome.getCoord()
                .getLng(), (float) droneHome.getAltitude().valueInMeters())
                : null;

        return new Home(homePosition);
    }

    @Override
    public Battery getBattery() throws RemoteException {
        org.droidplanner.core.drone.variables.Battery droneBattery =getDroneMgr().getDrone().getBattery();
        return new Battery(droneBattery.getBattVolt(), droneBattery.getBattRemain(),
                droneBattery.getBattCurrent(), droneBattery.getBattDischarge());
    }

    @Override
    public Altitude getAltitude() throws RemoteException {
        org.droidplanner.core.drone.variables.Altitude droneAltitude =getDroneMgr().getDrone().getAltitude();
        return new Altitude(droneAltitude.getAltitude(), droneAltitude.getTargetAltitude());
    }

    @Override
    public Mission getMission() throws RemoteException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Signal getSignal() throws RemoteException {
        Radio droneRadio = getDroneMgr().getDrone().getRadio();
        return new Signal(droneRadio.isValid(), droneRadio.getRxErrors(), droneRadio.getFixed(),
                droneRadio.getTxBuf(), droneRadio.getRssi(), droneRadio.getRemRssi(),
                droneRadio.getNoise(), droneRadio.getRemNoise());
    }

    @Override
    public Type getType() throws RemoteException {
        final Drone drone = getDroneMgr().getDrone();
        return new Type(getDroneProxyType(drone.getType()), drone.getFirmwareVersion());
    }

    @Override
    public boolean isConnected() throws RemoteException {
        return getDroneMgr().isConnected();
    }

    @Override
    public void changeVehicleMode(VehicleMode newMode) throws RemoteException {
        int mavType;
        switch(newMode.getDroneType()){
            default:
            case Type.TYPE_COPTER:
                mavType = MAV_TYPE.MAV_TYPE_QUADROTOR;
                break;

            case Type.TYPE_PLANE:
                mavType = MAV_TYPE.MAV_TYPE_FIXED_WING;
                break;

            case Type.TYPE_ROVER:
                mavType = MAV_TYPE.MAV_TYPE_GROUND_ROVER;
                break;
        }

       getDroneMgr().getDrone().getState().changeFlightMode(ApmModes.getMode(newMode.getMode(), mavType));
    }

    @Override
    public void refreshParameters() throws RemoteException {
       getDroneMgr().getDrone().getParameters().refreshParameters();
    }

    @Override
    public void writeParameters(Parameters parameters) throws RemoteException {
        if(parameters == null) return;

        List<com.ox3dr.services.android.lib.drone.property.Parameter> parametersList = parameters
                .getParameters();
        if(parametersList.isEmpty())
            return;

        final Drone drone =getDroneMgr().getDrone();
        org.droidplanner.core.drone.profiles.Parameters droneParams = drone.getParameters();
        for(com.ox3dr.services.android.lib.drone.property.Parameter proxyParam : parametersList){
            droneParams.sendParameter(new Parameter(proxyParam.getName(), proxyParam.getValue(),
                    proxyParam.getType()));
        }
    }

    @Override
    public void sendMission(Mission mission) throws RemoteException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void arm(boolean arm) throws RemoteException {
        MavLinkArm.sendArmMessage(getDroneMgr().getDrone(), arm);
    }

    @Override
    public void startMagnetometerCalibration(List<Point3D> startPoints) throws RemoteException {
        if(magCalibration.isRunning()){
            magCalibration.stop();
        }

        magCalibration.start(MathUtil.point3DToThreeSpacePoint(startPoints));
    }

    @Override
    public void stopMagnetometerCalibration() throws RemoteException {
        if(magCalibration.isRunning())
            magCalibration.stop();
    }

    @Override
    public void startIMUCalibration() throws RemoteException {
        if(!getDroneMgr().getDrone().getCalibrationSetup().startCalibration()){
            Bundle extrasBundle = new Bundle(1);
            extrasBundle.putString(Extra.EXTRA_CALIBRATION_IMU_MESSAGE,
                    context.getString(R.string.failed_start_calibration_message));
            getCallback().onDroneEvent(Event.EVENT_CALIBRATION_IMU_ERROR, extrasBundle);
        }
    }

    @Override
    public void sendIMUCalibrationAck(int step) throws RemoteException {
        getDroneMgr().getDrone().getCalibrationSetup().sendAckk(step);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        final IDroidPlannerApiCallback callback = getCallback();
        Bundle extrasBundle;

        try {
            switch (event) {
                case DISCONNECTED:
                    callback.onDroneEvent(Event.EVENT_DISCONNECTED, emptyBundle);
                    break;

                case GUIDEDPOINT:
                    break;
                case NAVIGATION:
                    break;

                case RADIO:
                    callback.onDroneEvent(Event.EVENT_RADIO, emptyBundle);
                    break;

                case RC_IN:
                    break;
                case RC_OUT:
                    break;

                case ARMING:
                    callback.onDroneEvent(Event.EVENT_ARMING, emptyBundle);
                    break;

                case AUTOPILOT_WARNING:
                    callback.onDroneEvent(Event.EVENT_AUTOPILOT_FAILSAFE, emptyBundle);
                    break;

                case MODE:
                    callback.onDroneEvent(Event.EVENT_VEHICLE_MODE, emptyBundle);
                    break;

                case ATTITUDE:
                case ORIENTATION:
                    callback.onDroneEvent(Event.EVENT_ATTITUDE, emptyBundle);
                    break;

                case SPEED:
                    callback.onDroneEvent(Event.EVENT_SPEED, emptyBundle);
                    break;

                case BATTERY:
                    callback.onDroneEvent(Event.EVENT_BATTERY, emptyBundle);
                    break;

                case STATE:
                    callback.onDroneEvent(Event.EVENT_STATE, emptyBundle);
                    break;

                case MISSION_UPDATE:
                    break;
                case MISSION_RECEIVED:
                    break;

                case FIRMWARE:
                case TYPE:
                    callback.onDroneEvent(Event.EVENT_TYPE_UPDATED, emptyBundle);
                    break;

                case HOME:
                    callback.onDroneEvent(Event.EVENT_HOME, emptyBundle);
                    break;

                case GPS:
                case GPS_FIX:
                case GPS_COUNT:
                    callback.onDroneEvent(Event.EVENT_GPS, emptyBundle);
                    break;

                case PARAMETER:
                case PARAMETERS_DOWNLOADED:
                    callback.onDroneEvent(Event.EVENT_PARAMETERS_RECEIVED, emptyBundle);
                    break;

                case CALIBRATION_IMU:
                    callback.onDroneEvent(Event.EVENT_CALIBRATION_IMU, emptyBundle);
                    break;

                case CALIBRATION_TIMEOUT:
                    /*
				 * here we will check if we are in calibration mode but if at
				 * the same time 'msg' is empty - then it is actually not doing
				 * calibration what we should do is to reset the calibration
				 * flag and re-trigger the HEARBEAT_TIMEOUT this however should
				 * not be happening
				 */
                    final Calibration calibration = getDroneMgr().getDrone().getCalibrationSetup();
                    final String message = calibration.getMessage();
                    if(calibration.isCalibrating() && TextUtils.isEmpty(message)){
                        calibration.setCalibrating(false);
                        callback.onDroneEvent(Event.EVENT_HEARTBEAT_TIMEOUT, emptyBundle);
                    }
                    else {
                        extrasBundle = new Bundle(1);
                        extrasBundle.putString(Extra.EXTRA_CALIBRATION_IMU_MESSAGE, message);
                        callback.onDroneEvent(Event.EVENT_CALIBRATION_IMU_TIMEOUT, extrasBundle);
                    }
                    break;

                case HEARTBEAT_TIMEOUT:
                    callback.onDroneEvent(Event.EVENT_HEARTBEAT_TIMEOUT, emptyBundle);
                    break;

                case HEARTBEAT_FIRST:
                    extrasBundle = new Bundle();
                    extrasBundle.putInt(Extra.EXTRA_MAVLINK_VERSION, drone.getMavlinkVersion());
                    callback.onDroneEvent(Event.EVENT_HEARTBEAT_FIRST, extrasBundle);
                    break;

                case HEARTBEAT_RESTORED:
                    extrasBundle = new Bundle();
                    extrasBundle.putInt(Extra.EXTRA_MAVLINK_VERSION, drone.getMavlinkVersion());
                    callback.onDroneEvent(Event.EVENT_HEARTBEAT_RESTORED, extrasBundle);
                    break;

                case CONNECTED:
                    callback.onDroneEvent(Event.EVENT_CONNECTED, emptyBundle);
                    break;

                case MISSION_SENT:
                    break;
                case ARMING_STARTED:
                    break;
                case INVALID_POLYGON:
                    break;
                case MISSION_WP_UPDATE:
                    break;
                case FOLLOW_START:
                    break;
                case FOLLOW_STOP:
                    break;
                case FOLLOW_UPDATE:
                    break;
                case FOLLOW_CHANGE_TYPE:
                    break;
                case WARNING_400FT_EXCEEDED:
                    break;
                case WARNING_SIGNAL_WEAK:
                    break;
                case WARNING_NO_GPS:
                    break;
                case MAGNETOMETER:
                    break;
                case FOOTPRINT:
                    break;
            }
        }catch(RemoteException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        try {
            getCallback().onDroneEvent(Event.EVENT_PARAMETERS_REFRESH_STARTED, emptyBundle);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        try{
            Bundle paramsBundle = new Bundle(2);
            paramsBundle.putInt(Extra.EXTRA_PARAMETER_INDEX, index);
            paramsBundle.putInt(Extra.EXTRA_PARAMETERS_COUNT, count);
            getCallback().onDroneEvent(Event.EVENT_PARAMETERS_RECEIVED, paramsBundle);
        } catch(RemoteException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameter) {
        try{
            getCallback().onDroneEvent(Event.EVENT_PARAMETERS_REFRESH_ENDED, emptyBundle);
        } catch(RemoteException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onStarted(List<ThreeSpacePoint> points) {
        Bundle paramsBundle = new Bundle();
        paramsBundle.putParcelableArrayList(Extra.EXTRA_CALIBRATION_MAG_POINTS,
                MathUtil.threeSpacePointToPoint3D(points));
        try {
            getCallback().onDroneEvent(Event.EVENT_CALIBRATION_MAG_STARTED, paramsBundle);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void newEstimation(FitPoints fit, List<ThreeSpacePoint> points) {
        double fitness = fit.getFitness();
        double[] fitCenter = fit.center.isNaN()
                ? null
                : new double[]{fit.center.getEntry(0), fit.center.getEntry(1), fit.center.getEntry(2)};
        double[] fitRadii = fit.radii.isNaN()
                ? null
                : new double[]{fit.radii.getEntry(0), fit.radii.getEntry(1), fit.radii.getEntry(2)};

        Bundle paramsBundle = new Bundle(4);
        paramsBundle.putDouble(Extra.EXTRA_CALIBRATION_MAG_FITNESS, fitness);
        paramsBundle.putDoubleArray(Extra.EXTRA_CALIBRATION_MAG_FIT_CENTER, fitCenter);
        paramsBundle.putDoubleArray(Extra.EXTRA_CALIBRATION_MAG_FIT_RADII, fitRadii);
        paramsBundle.putParcelableArrayList(Extra.EXTRA_CALIBRATION_MAG_POINTS,
                MathUtil.threeSpacePointToPoint3D(points));

        try {
            getCallback().onDroneEvent(Event.EVENT_CALIBRATION_MAG_ESTIMATION, paramsBundle);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void finished(FitPoints fit) {
        try {
            double[] offsets = magCalibration.sendOffsets();
            double fitness = fit.getFitness();

            Bundle paramsBundle = new Bundle(2);
            paramsBundle.putDouble(Extra.EXTRA_CALIBRATION_MAG_FITNESS, fitness);
            paramsBundle.putDoubleArray(Extra.EXTRA_CALIBRATION_MAG_OFFSETS, offsets);

            getCallback().onDroneEvent(Event.EVENT_CALIBRATION_MAG_COMPLETED, paramsBundle);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
