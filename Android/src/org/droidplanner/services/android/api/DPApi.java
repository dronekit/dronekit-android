package org.droidplanner.services.android.api;

import android.content.Context;
import android.os.Bundle;
import android.os.DeadObjectException;
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
import com.ox3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.ox3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.ox3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.ox3dr.services.android.lib.drone.mission.item.raw.MissionItemMessage;
import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.GuidedState;
import com.ox3dr.services.android.lib.drone.property.Home;
import com.ox3dr.services.android.lib.drone.mission.Mission;
import com.ox3dr.services.android.lib.drone.property.Signal;
import com.ox3dr.services.android.lib.drone.property.Speed;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.Type;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;
import com.ox3dr.services.android.lib.drone.property.Parameters;
import com.ox3dr.services.android.lib.gcs.follow.FollowType;
import com.ox3dr.services.android.lib.gcs.follow.FollowState;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerApiCallback;

import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.Calibration;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.mission.survey.CameraInfo;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.services.android.R;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.utils.MathUtil;
import org.droidplanner.services.android.utils.file.IO.ParameterMetadataLoader;
import org.droidplanner.services.android.utils.file.help.CameraInfoLoader;
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
final class DPApi extends IDroidPlannerApi.Stub implements DroneEventsListener {

    private final static String TAG = DPApi.class.getSimpleName();

    private final Bundle emptyBundle = new Bundle();
    private final WeakReference<DroidPlannerService> serviceRef;
    private final Context context;

    private IDroidPlannerApiCallback apiCallback;
    private DroneManager droneMgr;

    DPApi(DroidPlannerService dpService, ConnectionParameter connParams,
          IDroidPlannerApiCallback callback) throws RemoteException {
        serviceRef = new WeakReference<DroidPlannerService>(dpService);
        this.context = dpService.getApplicationContext();

        this.apiCallback = callback;

        this.droneMgr = dpService.getDroneForConnection(connParams);
        this.droneMgr.addDroneEventsListener(this);

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

    private void handleDeadObjectException(DeadObjectException e){
        Log.e(TAG, e.getMessage(), e);
        getService().disconnectFromApi(getDroneMgr().getConnectionParameter(), getCallback());
    }

    void destroy() {
        getDroneMgr().removeDroneEventsListener(this);
        this.serviceRef.clear();
        this.apiCallback = null;
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

        return new State(getVehicleMode(droneMode), droneState.isArmed(),
                droneState.isFlying(), droneState.getWarning(), drone.getMavlinkVersion());
    }

    @Override
    public VehicleMode[] getAllVehicleModes() throws RemoteException {
        final int droneType = getDroneMgr().getDrone().getType();

        List<ApmModes> typeModes = ApmModes.getModeList(droneType);
        final int modesCount = typeModes.size();
        VehicleMode[] vehicleModes = new VehicleMode[modesCount];
        for(int i = 0; i < modesCount; i++){
            vehicleModes[i] = getVehicleMode(typeModes.get(i));
        }

        return vehicleModes;
    }

    private static VehicleMode getVehicleMode(ApmModes mode){
        switch(mode){
            case FIXED_WING_MANUAL:
                return VehicleMode.PLANE_MANUAL;
            case FIXED_WING_CIRCLE:
                return VehicleMode.PLANE_CIRCLE;
                
            case FIXED_WING_STABILIZE:
                return VehicleMode.PLANE_STABILIZE;
                
            case FIXED_WING_TRAINING:
                return VehicleMode.PLANE_TRAINING;
                
            case FIXED_WING_FLY_BY_WIRE_A:
                return VehicleMode.PLANE_FLY_BY_WIRE_A;
                
            case FIXED_WING_FLY_BY_WIRE_B:
                return VehicleMode.PLANE_FLY_BY_WIRE_B;
                
            case FIXED_WING_AUTO:
                return VehicleMode.PLANE_AUTO;
                
            case FIXED_WING_RTL:
                return VehicleMode.PLANE_RTL;
                
            case FIXED_WING_LOITER:
                return VehicleMode.PLANE_LOITER;
                
            case FIXED_WING_GUIDED:
                return VehicleMode.PLANE_GUIDED;
                

            case ROTOR_STABILIZE:
                return VehicleMode.COPTER_STABILIZE;
                
            case ROTOR_ACRO:
                return VehicleMode.COPTER_ACRO;
                
            case ROTOR_ALT_HOLD:
                return VehicleMode.COPTER_ALT_HOLD;
                
            case ROTOR_AUTO:
                return VehicleMode.COPTER_AUTO;
                
            case ROTOR_GUIDED:
                return VehicleMode.COPTER_GUIDED;
                
            case ROTOR_LOITER:
                return VehicleMode.COPTER_LOITER;
                
            case ROTOR_RTL:
                return VehicleMode.COPTER_RTL;
                
            case ROTOR_CIRCLE:
                return VehicleMode.COPTER_CIRCLE;
                
            case ROTOR_LAND:
                return VehicleMode.COPTER_LAND;
                
            case ROTOR_TOY:
                return VehicleMode.COPTER_DRIFT;
                
            case ROTOR_SPORT:
                return VehicleMode.COPTER_SPORT;
                
            case ROTOR_AUTOTUNE:
                return VehicleMode.COPTER_AUTOTUNE;
                
            case ROTOR_POSHOLD:
                return VehicleMode.COPTER_POSHOLD;
                

            case ROVER_MANUAL:
                return VehicleMode.ROVER_MANUAL;
                
            case ROVER_LEARNING:
                return VehicleMode.ROVER_LEARNING;
                
            case ROVER_STEERING:
                return VehicleMode.ROVER_STEERING;
                
            case ROVER_HOLD:
                return VehicleMode.ROVER_HOLD;
                
            case ROVER_AUTO:
                return VehicleMode.ROVER_AUTO;
                
            case ROVER_RTL:
                return VehicleMode.ROVER_RTL;
                
            case ROVER_GUIDED:
                return VehicleMode.ROVER_GUIDED;
                
            case ROVER_INITIALIZING:
                return VehicleMode.ROVER_INITIALIZING;
                

            default:
            case UNKNOWN:
                return null;
                
        }
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
        //TODO: complete implementation
        throw new UnsupportedOperationException("Method not yet implemented.");

    }

    @Override
    public MissionItemMessage[] getRawMissionItems() throws RemoteException {
        //TODO: complete implementation
        throw new UnsupportedOperationException("Method not yet implemented.");
//        return new MissionItemMessage[0];
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
    public GuidedState getGuidedState() throws RemoteException {
        final GuidedPoint guidedPoint = getDroneMgr().getDrone().getGuidedPoint();
        int guidedState;
        switch(guidedPoint.getState()){
            default:
            case UNINITIALIZED:
                guidedState = GuidedState.STATE_UNINITIALIZED;
                break;

            case ACTIVE:
                guidedState = GuidedState.STATE_ACTIVE;
                break;

            case IDLE:
                guidedState = GuidedState.STATE_IDLE;
                break;
        }

        Coord2D guidedCoord = guidedPoint.getCoord() == null
                ? new Coord2D(0, 0)
                : guidedPoint.getCoord();
        double guidedAlt = guidedPoint.getAltitude() == null
                ? 0
                : guidedPoint.getAltitude().valueInMeters();
        return new GuidedState(guidedState, new LatLongAlt((float)guidedCoord.getLat(),
                (float) guidedCoord.getLng(), (float) guidedAlt));
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
    public void connect() throws RemoteException {
        try {
            getDroneMgr().connect();
        } catch (ConnectionException e) {
            try {
                getCallback().onConnectionFailed(new ConnectionResult(0, e.getMessage()));
            }catch(DeadObjectException d){
                handleDeadObjectException(d);
            }
        }
    }

    @Override
    public void disconnect() throws RemoteException {
        try {
            getDroneMgr().disconnect();
        } catch (ConnectionException e) {
            try {
                getCallback().onConnectionFailed(new ConnectionResult(0, e.getMessage()));
            }catch(DeadObjectException d){
                handleDeadObjectException(d);
            }
        }
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
    public void setMission(Mission mission, boolean pushToDrone) throws RemoteException {
        //TODO: complete implementation
        throw new UnsupportedOperationException("Method not yet implemented.");

    }

    @Override
    public void setRawMissionItems(MissionItemMessage[] missionItems, boolean pushToDrone) throws RemoteException {
        //TODO: complete implementation
        throw new UnsupportedOperationException("Method not yet implemented.");
    }

    @Override
    public void generateDronie() throws RemoteException {
        getDroneMgr().getDrone().getMission().makeAndUploadDronie();
    }

    @Override
    public void arm(boolean arm) throws RemoteException {
        MavLinkArm.sendArmMessage(getDroneMgr().getDrone(), arm);
    }

    @Override
    public void startMagnetometerCalibration(List<Point3D> startPoints) throws RemoteException {
        getDroneMgr().startMagnetometerCalibration(MathUtil.point3DToThreeSpacePoint(startPoints));
    }

    @Override
    public void stopMagnetometerCalibration() throws RemoteException {
        getDroneMgr().stopMagnetometerCalibration();
    }

    @Override
    public void startIMUCalibration() throws RemoteException {
        if(!getDroneMgr().getDrone().getCalibrationSetup().startCalibration()){
            Bundle extrasBundle = new Bundle(1);
            extrasBundle.putString(Extra.EXTRA_CALIBRATION_IMU_MESSAGE,
                    context.getString(R.string.failed_start_calibration_message));
            try {
                getCallback().onDroneEvent(Event.EVENT_CALIBRATION_IMU_ERROR, extrasBundle);
            }catch(DeadObjectException e){
                handleDeadObjectException(e);
            }
        }
    }

    @Override
    public void sendIMUCalibrationAck(int step) throws RemoteException {
        getDroneMgr().getDrone().getCalibrationSetup().sendAckk(step);
    }

    @Override
    public void doGuidedTakeoff(double altitude) throws RemoteException {
        getDroneMgr().getDrone().getGuidedPoint().doGuidedTakeoff(new org.droidplanner.core
                .helpers.units.Altitude(altitude));
    }

    @Override
    public void sendGuidedPoint(LatLong point, boolean force) throws RemoteException {
        GuidedPoint guidedPoint = getDroneMgr().getDrone().getGuidedPoint();
        if(guidedPoint.isInitialized()){
            guidedPoint.newGuidedCoord(MathUtil.latLongToCoord2D(point));
        }
        else if(force){
            try {
                guidedPoint.forcedGuidedCoordinate(MathUtil.latLongToCoord2D(point));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void setGuidedAltitude(double altitude) throws RemoteException {
        getDroneMgr().getDrone().getGuidedPoint().changeGuidedAltitude(altitude);
    }

    @Override
    public void setGuidedVelocity(double xVel, double yVel, double zVel) throws RemoteException {
        getDroneMgr().getDrone().getGuidedPoint().newGuidedVelocity(xVel, yVel, zVel);
    }

    @Override
    public void enableFollowMe(FollowType followType) throws RemoteException {
        final FollowAlgorithm.FollowModes selectedMode;
        switch(followType){
            case ABOVE:
                selectedMode = FollowAlgorithm.FollowModes.ABOVE;
                break;

            case LEAD:
                selectedMode = FollowAlgorithm.FollowModes.LEAD;
                break;

            case LEASH:
                selectedMode = FollowAlgorithm.FollowModes.LEASH;
                break;

            case CIRCLE:
                selectedMode = FollowAlgorithm.FollowModes.CIRCLE;
                break;

            case LEFT:
                selectedMode = FollowAlgorithm.FollowModes.LEFT;
                break;

            case RIGHT:
                selectedMode = FollowAlgorithm.FollowModes.RIGHT;
                break;

            default:
                selectedMode = null;
                break;
        }

        if(selectedMode != null){
            final Follow followMe = getDroneMgr().getFollowMe();
            if(!followMe.isEnabled())
                followMe.toggleFollowMeState();

            followMe.setType(selectedMode);
        }
    }

    @Override
    public FollowState getFollowState() throws RemoteException {
        final Follow followMe = getDroneMgr().getFollowMe();
        final double radius = followMe.getRadius().valueInMeters();

        final int state;
        switch(followMe.getState()){

            default:
            case FOLLOW_INVALID_STATE:
                state = FollowState.STATE_INVALID;
                break;

            case FOLLOW_DRONE_NOT_ARMED:
                state = FollowState.STATE_DRONE_NOT_ARMED;
                break;

            case FOLLOW_DRONE_DISCONNECTED:
                state = FollowState.STATE_DRONE_DISCONNECTED;
                break;

            case FOLLOW_START:
                state = FollowState.STATE_START;
                break;

            case FOLLOW_RUNNING:
                state = FollowState.STATE_RUNNING;
                break;

            case FOLLOW_END:
                state = FollowState.STATE_END;
                break;
        }

        return new FollowState(state, radius, followModeToType(followMe.getType()));
    }

    @Override
    public FollowType[] getFollowTypes() throws RemoteException {
        final FollowAlgorithm.FollowModes[] followModes = FollowAlgorithm.FollowModes.values();
        final int modesCount = followModes.length;
        final FollowType[] followTypes = new FollowType[modesCount];
        for(int i = 0; i < modesCount; i++){
            followTypes[i] = followModeToType(followModes[i]);
        }

        return followTypes;
    }

    @Override
    public CameraDetail[] getCameraDetails() throws RemoteException {
        final CameraInfoLoader camInfoLoader = getDroneMgr().getCameraInfoLoader();
        List<String> cameraInfoNames = camInfoLoader.getCameraInfoList();

        List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>(cameraInfoNames.size());
        for(String infoName : cameraInfoNames){
            try {
                cameraInfos.add(camInfoLoader.openFile(infoName));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        final int infoCount = cameraInfos.size();
        CameraDetail[] cameraDetails = new CameraDetail[infoCount];
        for(int i = 0; i < infoCount; i++){
            CameraInfo camInfo = cameraInfos.get(i);
            cameraDetails[i] = new CameraDetail(camInfo.name, camInfo.sensorWidth,
                    camInfo.sensorHeight, camInfo.sensorResolution, camInfo.focalLength,
                    camInfo.overlap, camInfo.sidelap, camInfo.isInLandscapeOrientation);
        }

        return cameraDetails;
    }

    @Override
    public Survey updateSurveyMissionItem(Survey survey) throws RemoteException {
        //TODO: complete implementation
        throw new UnsupportedOperationException("Method not yet implemented.");

    }

    @Override
    public StructureScanner updateStructureScanner(StructureScanner item) throws RemoteException {
        //TODO: complete implementation
        throw new UnsupportedOperationException("Method not yet implemented.");

    }

    private static FollowType followModeToType(FollowAlgorithm.FollowModes followMode){
        final FollowType followType;

        switch(followMode){
            default:
            case LEASH:
                followType = FollowType.LEASH;
                break;

            case LEAD:
                followType = FollowType.LEAD;
                break;

            case RIGHT:
                followType = FollowType.RIGHT;
                break;

            case LEFT:
                followType = FollowType.LEFT;
                break;

            case CIRCLE:
                followType = FollowType.CIRCLE;
                break;

            case ABOVE:
                followType = FollowType.ABOVE;
                break;
        }

        return followType;
    }

    @Override
    public void setFollowMeRadius(double radius) throws RemoteException {
        getDroneMgr().getFollowMe().changeRadius(radius);
    }

    @Override
    public void disableFollowMe() throws RemoteException {
        Follow follow = getDroneMgr().getFollowMe();
        if(follow.isEnabled())
            follow.toggleFollowMeState();
    }

    @Override
    public void enableDroneShare(String username, String password, boolean isEnabled) throws RemoteException {
        //TODO: complete implementation
        throw new UnsupportedOperationException("Method not yet implemented.");

    }

    @Override
    public void triggerCamera() throws RemoteException {
        MavLinkROI.triggerCamera(getDroneMgr().getDrone());
    }

    @Override
    public void epmCommand(boolean release) throws RemoteException {
        MavLinkROI.empCommand(getDroneMgr().getDrone(), release);
    }

    @Override
    public void loadWaypoints() throws RemoteException {
        getDroneMgr().getDrone().getWaypointManager().getWaypoints();
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
                    callback.onDroneEvent(Event.EVENT_GUIDED_POINT, emptyBundle);
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

                case ARMING_STARTED:
                case ARMING:
                    callback.onDroneEvent(Event.EVENT_ARMING, emptyBundle);
                    break;

                case AUTOPILOT_WARNING:
                    String warning = drone.getState().getWarning();
                    extrasBundle.putString(Extra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE, warning);
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
                case INVALID_POLYGON:
                    break;
                case MISSION_WP_UPDATE:
                    break;

                case FOLLOW_START:
                    callback.onDroneEvent(Event.EVENT_FOLLOW_START, emptyBundle);
                    break;

                case FOLLOW_STOP:
                    callback.onDroneEvent(Event.EVENT_FOLLOW_STOP, emptyBundle);
                    break;

                case FOLLOW_UPDATE:
                case FOLLOW_CHANGE_TYPE:
                    callback.onDroneEvent(Event.EVENT_FOLLOW_UPDATE, emptyBundle);
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
        }catch(DeadObjectException e){
            handleDeadObjectException(e);
        }catch(RemoteException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        try {
            getCallback().onDroneEvent(Event.EVENT_PARAMETERS_REFRESH_STARTED, emptyBundle);
        }catch(DeadObjectException e){
            handleDeadObjectException(e);
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
        } catch(DeadObjectException e){
            handleDeadObjectException(e);
        }catch(RemoteException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameter) {
        try{
            getCallback().onDroneEvent(Event.EVENT_PARAMETERS_REFRESH_ENDED, emptyBundle);
        }catch(DeadObjectException e){
            handleDeadObjectException(e);
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
        } catch(DeadObjectException e){
            handleDeadObjectException(e);
        }catch (RemoteException e) {
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
        }
        catch(DeadObjectException e){
            handleDeadObjectException(e);
        }
        catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void finished(FitPoints fit, double[] offsets) {
        try {
            double fitness = fit.getFitness();

            Bundle paramsBundle = new Bundle(2);
            paramsBundle.putDouble(Extra.EXTRA_CALIBRATION_MAG_FITNESS, fitness);
            paramsBundle.putDoubleArray(Extra.EXTRA_CALIBRATION_MAG_OFFSETS, offsets);

            try {
                getCallback().onDroneEvent(Event.EVENT_CALIBRATION_MAG_COMPLETED, paramsBundle);
            }catch(DeadObjectException e){
                handleDeadObjectException(e);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
