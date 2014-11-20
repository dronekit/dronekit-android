package org.droidplanner.services.android.api;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.enums.MAV_TYPE;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.event.Event;
import com.o3dr.services.android.lib.drone.event.Extra;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.FootPrint;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.model.IDroidPlannerApi;
import com.o3dr.services.android.lib.model.IDroidPlannerApiCallback;

import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Calibration;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.core.survey.CameraInfo;
import org.droidplanner.core.survey.Footprint;
import org.droidplanner.services.android.R;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.utils.MathUtils;
import org.droidplanner.services.android.utils.ProxyUtils;
import org.droidplanner.services.android.utils.file.IO.ParameterMetadataLoader;
import org.droidplanner.services.android.utils.file.help.CameraInfoLoader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

/**
 * Created by fhuya on 10/30/14.
 */
final class DPApi extends IDroidPlannerApi.Stub implements DroneEventsListener {

    private final static String TAG = DPApi.class.getSimpleName();

    private final WeakReference<DroidPlannerService> serviceRef;
    private final Context context;

    private final ConcurrentLinkedQueue<IDroidPlannerApiCallback> apiCallbacks;
    private DroneManager droneMgr;

    DPApi(DroidPlannerService dpService, Handler handler, MavLinkServiceApi mavlinkApi) {
        this.context = dpService.getApplicationContext();

        serviceRef = new WeakReference<DroidPlannerService>(dpService);
        apiCallbacks = new ConcurrentLinkedQueue<IDroidPlannerApiCallback>();

        this.droneMgr = new DroneManager(context, handler, mavlinkApi);
        this.droneMgr.addDroneEventsListener(this);
    }

    void destroy() {
        this.droneMgr.removeDroneEventsListener(this);
        this.droneMgr.destroy();

        this.serviceRef.clear();
        this.apiCallbacks.clear();
    }

    public DroneManager getDroneManager(){
        return this.droneMgr;
    }

    private DroidPlannerService getService() {
        final DroidPlannerService service = serviceRef.get();
        if (service == null)
            throw new IllegalStateException("Lost reference to parent service.");

        return service;
    }

    @Override
    public Gps getGps() throws RemoteException {
        final GPS droneGps = droneMgr.getDrone().getGps();
        LatLong dronePosition = droneGps.isPositionValid()
                ? new LatLong(droneGps.getPosition().getLat(), droneGps.getPosition().getLng())
                : null;

        return new Gps(dronePosition, droneGps.getGpsEPH(), droneGps.getSatCount(),
                droneGps.getFixTypeNumeric());
    }

    @Override
    public State getState() throws RemoteException {
        final Drone drone = this.droneMgr.getDrone();
        org.droidplanner.core.drone.variables.State droneState = drone.getState();
        ApmModes droneMode = droneState.getMode();
        Calibration calibration = drone.getCalibrationSetup();
        String calibrationMessage = calibration.isCalibrating() ? calibration.getMessage() : null;

        return new State(getVehicleMode(droneMode), droneState.isArmed(),
                droneState.isFlying(), droneState.getWarning(), drone.getMavlinkVersion(),
                calibrationMessage);
    }

    @Override
    public VehicleMode[] getAllVehicleModes() throws RemoteException {
        final int droneType = this.droneMgr.getDrone().getType();

        List<ApmModes> typeModes = ApmModes.getModeList(droneType);
        final int modesCount = typeModes.size();
        VehicleMode[] vehicleModes = new VehicleMode[modesCount];
        for (int i = 0; i < modesCount; i++) {
            vehicleModes[i] = getVehicleMode(typeModes.get(i));
        }

        return vehicleModes;
    }

    private static VehicleMode getVehicleMode(ApmModes mode) {
        switch (mode) {
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

    private static int getDroneProxyType(int originalType) {
        switch (originalType) {
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
        final Drone drone = this.droneMgr.getDrone();
        final Map<String, com.o3dr.services.android.lib.drone.property.Parameter> proxyParams =
                new HashMap<String, com.o3dr.services.android.lib.drone.property.Parameter>();

        List<Parameter> droneParameters = drone.getParameters().getParametersList();
        if (!droneParameters.isEmpty()) {
            for (Parameter param : droneParameters) {
                if (param.name != null) {
                    proxyParams.put(param.name, new com.o3dr.services.android.lib.drone.property
                            .Parameter(param.name, param.value, param.type));
                }
            }

            try {
                final VehicleProfile profile = drone.getVehicleProfile();
                if (profile != null) {
                    String metadataType = profile.getParameterMetadataType();
                    if (metadataType != null) {
                        ParameterMetadataLoader.load(getService().getApplicationContext(),
                                metadataType, proxyParams);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return new Parameters(new ArrayList<com.o3dr.services.android.lib.drone.property
                .Parameter>(proxyParams.values()));
    }

    @Override
    public Speed getSpeed() throws RemoteException {
        org.droidplanner.core.drone.variables.Speed droneSpeed = this.droneMgr.getDrone().getSpeed();
        return new Speed(droneSpeed.getVerticalSpeed().valueInMetersPerSecond(),
                droneSpeed.getGroundSpeed().valueInMetersPerSecond(),
                droneSpeed.getAirSpeed().valueInMetersPerSecond());
    }

    @Override
    public Attitude getAttitude() throws RemoteException {
        Orientation droneOrientation = this.droneMgr.getDrone().getOrientation();
        return new Attitude(droneOrientation.getRoll(), droneOrientation.getPitch(),
                droneOrientation.getYaw());
    }

    @Override
    public Home getHome() throws RemoteException {
        org.droidplanner.core.drone.variables.Home droneHome = this.droneMgr.getDrone().getHome();
        LatLongAlt homePosition = droneHome.isValid()
                ? new LatLongAlt(droneHome.getCoord().getLat(), droneHome.getCoord().getLng(),
                droneHome.getAltitude().valueInMeters())
                : null;

        return new Home(homePosition);
    }

    @Override
    public Battery getBattery() throws RemoteException {
        org.droidplanner.core.drone.variables.Battery droneBattery = this.droneMgr.getDrone().getBattery();
        return new Battery(droneBattery.getBattVolt(), droneBattery.getBattRemain(),
                droneBattery.getBattCurrent(), droneBattery.getBattDischarge());
    }

    @Override
    public Altitude getAltitude() throws RemoteException {
        org.droidplanner.core.drone.variables.Altitude droneAltitude = this.droneMgr.getDrone().getAltitude();
        return new Altitude(droneAltitude.getAltitude(), droneAltitude.getTargetAltitude());
    }

    @Override
    public Mission getMission() throws RemoteException {
        final Drone drone = this.droneMgr.getDrone();
        org.droidplanner.core.mission.Mission droneMission = drone.getMission();
        List<org.droidplanner.core.mission.MissionItem> droneMissionItems = droneMission.getItems();

        Mission proxyMission = new Mission();
        proxyMission.setCurrentMissionItem((short) drone.getMissionStats().getCurrentWP());
        if (!droneMissionItems.isEmpty()) {
            for (org.droidplanner.core.mission.MissionItem item : droneMissionItems) {
                proxyMission.addMissionItem(ProxyUtils.getProxyMissionItem(item));
            }
        }

        return proxyMission;
    }

    @Override
    public Signal getSignal() throws RemoteException {
        Radio droneRadio = this.droneMgr.getDrone().getRadio();
        return new Signal(droneRadio.isValid(), droneRadio.getRxErrors(), droneRadio.getFixed(),
                droneRadio.getTxBuf(), droneRadio.getRssi(), droneRadio.getRemRssi(),
                droneRadio.getNoise(), droneRadio.getRemNoise());
    }

    @Override
    public Type getType() throws RemoteException {
        final Drone drone = this.droneMgr.getDrone();
        return new Type(getDroneProxyType(drone.getType()), drone.getFirmwareVersion());
    }

    @Override
    public boolean isConnected() throws RemoteException {
        return droneMgr != null && droneMgr.isConnected();
    }

    @Override
    public GuidedState getGuidedState() throws RemoteException {
        final GuidedPoint guidedPoint = this.droneMgr.getDrone().getGuidedPoint();
        int guidedState;
        switch (guidedPoint.getState()) {
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
        return new GuidedState(guidedState, new LatLongAlt(guidedCoord.getLat(),
                guidedCoord.getLng(), guidedAlt));
    }

    @Override
    public void changeVehicleMode(VehicleMode newMode) throws RemoteException {
        int mavType;
        switch (newMode.getDroneType()) {
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

        this.droneMgr.getDrone().getState().changeFlightMode(ApmModes.getMode(newMode.getMode(), mavType));
    }

    @Override
    public void connect(ConnectionParameter connParams) throws RemoteException {
        if (connParams == null || !connParams.equals(droneMgr.getConnectionParameter()))
            droneMgr.setConnectionParameter(connParams);

        try {
            // Do a quick scan to see if we need any droneshare uploads
            if (connParams != null) {
                this.droneMgr.connect();
                getService().kickStartDroneShareUploader(connParams.getDroneSharePrefs());
            }
        } catch (ConnectionException e) {
            notifyConnectionFailed(new ConnectionResult(0, e.getMessage()));
            disconnect();
        }
    }

    @Override
    public void disconnect() throws RemoteException {
        try {
            droneMgr.disconnect();
        } catch (ConnectionException e) {
            notifyConnectionFailed(new ConnectionResult(0, e.getMessage()));
        }
    }

    @Override
    public void refreshParameters() throws RemoteException {
        this.droneMgr.getDrone().getParameters().refreshParameters();
    }

    @Override
    public void writeParameters(Parameters parameters) throws RemoteException {
        if (parameters == null) return;

        List<com.o3dr.services.android.lib.drone.property.Parameter> parametersList = parameters
                .getParameters();
        if (parametersList.isEmpty())
            return;

        final Drone drone = this.droneMgr.getDrone();
        org.droidplanner.core.drone.profiles.Parameters droneParams = drone.getParameters();
        for (com.o3dr.services.android.lib.drone.property.Parameter proxyParam : parametersList) {
            droneParams.sendParameter(new Parameter(proxyParam.getName(), proxyParam.getValue(),
                    proxyParam.getType()));
        }
    }

    @Override
    public void setMission(Mission mission, boolean pushToDrone) throws RemoteException {
        org.droidplanner.core.mission.Mission droneMission = this.droneMgr.getDrone().getMission();
        droneMission.clearMissionItems();

        List<MissionItem> itemsList = mission.getMissionItems();
        for (MissionItem item : itemsList) {
            droneMission.addMissionItem(ProxyUtils.getMissionItem(droneMission, item));
        }

        if (pushToDrone)
            droneMission.sendMissionToAPM();
    }

    @Override
    public void generateDronie() throws RemoteException {
        float bearing = (float) this.droneMgr.getDrone().getMission().makeAndUploadDronie();
        Bundle bundle = new Bundle(1);
        bundle.putFloat(Extra.EXTRA_MISSION_DRONIE_BEARING, bearing);
        notifyDroneEvent(Event.EVENT_MISSION_DRONIE_CREATED, bundle);
    }

    @Override
    public void arm(boolean arm) throws RemoteException {
        MavLinkArm.sendArmMessage(this.droneMgr.getDrone(), arm);
    }

    @Override
    public void startMagnetometerCalibration(double[] startPointsX,
                                             double[] startPointsY,
                                             double[] startPointsZ) throws RemoteException {
        this.droneMgr.startMagnetometerCalibration(MathUtils.pointsArrayToThreeSpacePoint(new
                double[][]{startPointsX, startPointsY, startPointsZ}));
    }

    @Override
    public void stopMagnetometerCalibration() throws RemoteException {
        this.droneMgr.stopMagnetometerCalibration();
    }

    @Override
    public void startIMUCalibration() throws RemoteException {
        if (!this.droneMgr.getDrone().getCalibrationSetup().startCalibration()) {
            Bundle extrasBundle = new Bundle(1);
            extrasBundle.putString(Extra.EXTRA_CALIBRATION_IMU_MESSAGE,
                    context.getString(R.string.failed_start_calibration_message));
            notifyDroneEvent(Event.EVENT_CALIBRATION_IMU_ERROR, extrasBundle);
        }
    }

    @Override
    public void sendIMUCalibrationAck(int step) throws RemoteException {
        this.droneMgr.getDrone().getCalibrationSetup().sendAckk(step);
    }

    @Override
    public void doGuidedTakeoff(double altitude) throws RemoteException {
        this.droneMgr.getDrone().getGuidedPoint().doGuidedTakeoff(new org.droidplanner.core
                .helpers.units.Altitude(altitude));
    }

    @Override
    public void sendGuidedPoint(LatLong point, boolean force) throws RemoteException {
        GuidedPoint guidedPoint = this.droneMgr.getDrone().getGuidedPoint();
        if (guidedPoint.isInitialized()) {
            guidedPoint.newGuidedCoord(MathUtils.latLongToCoord2D(point));
        } else if (force) {
            try {
                guidedPoint.forcedGuidedCoordinate(MathUtils.latLongToCoord2D(point));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void setGuidedAltitude(double altitude) throws RemoteException {
        this.droneMgr.getDrone().getGuidedPoint().changeGuidedAltitude(altitude);
    }

    @Override
    public void setGuidedVelocity(double xVel, double yVel, double zVel) throws RemoteException {
        this.droneMgr.getDrone().getGuidedPoint().newGuidedVelocity(xVel, yVel, zVel);
    }

    @Override
    public void enableFollowMe(FollowType followType) throws RemoteException {
        final FollowAlgorithm.FollowModes selectedMode;
        switch (followType) {
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

        if (selectedMode != null) {
            final Follow followMe = this.droneMgr.getFollowMe();
            if (!followMe.isEnabled())
                followMe.toggleFollowMeState();

            followMe.setType(selectedMode);
        }
    }

    @Override
    public FollowState getFollowState() throws RemoteException {
        final Follow followMe = this.droneMgr.getFollowMe();
        final double radius = followMe.getRadius().valueInMeters();

        final int state;
        switch (followMe.getState()) {

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
        for (int i = 0; i < modesCount; i++) {
            followTypes[i] = followModeToType(followModes[i]);
        }

        return followTypes;
    }

    @Override
    public CameraDetail[] getCameraDetails() throws RemoteException {
        final CameraInfoLoader camInfoLoader = this.droneMgr.getCameraInfoLoader();
        List<String> cameraInfoNames = camInfoLoader.getCameraInfoList();

        List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>(cameraInfoNames.size());
        for (String infoName : cameraInfoNames) {
            try {
                cameraInfos.add(camInfoLoader.openFile(infoName));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        final int infoCount = cameraInfos.size();
        CameraDetail[] cameraDetails = new CameraDetail[infoCount];
        for (int i = 0; i < infoCount; i++) {
            CameraInfo camInfo = cameraInfos.get(i);
            cameraDetails[i] = new CameraDetail(camInfo.name, camInfo.sensorWidth,
                    camInfo.sensorHeight, camInfo.sensorResolution, camInfo.focalLength,
                    camInfo.overlap, camInfo.sidelap, camInfo.isInLandscapeOrientation);
        }

        return cameraDetails;
    }

    @Override
    public FootPrint getLastCameraFootPrint() throws RemoteException {
        Footprint lastFootprint = this.droneMgr.getDrone().getCamera().getLastFootprint();
        return getProxyCameraFootPrint(lastFootprint);
    }

    @Override
    public FootPrint[] getCameraFootPrints() throws RemoteException {
        List<Footprint> footprints = this.droneMgr.getDrone().getCamera().getFootprints();
        final int printsCount = footprints.size();

        FootPrint[] proxyPrints = new FootPrint[printsCount];
        for (int i = 0; i < printsCount; i++) {
            proxyPrints[i] = getProxyCameraFootPrint(footprints.get(i));
        }

        return proxyPrints;
    }

    @Override
    public FootPrint getCurrentFieldOfView() throws RemoteException {
        return getProxyCameraFootPrint(this.droneMgr.getDrone().getCamera().getCurrentFieldOfView
                ());
    }

    private static FootPrint getProxyCameraFootPrint(Footprint footprint) {
        if (footprint == null) return null;

        return new FootPrint(footprint.getGSD(),
                MathUtils.coord2DToLatLong(footprint.getVertexInGlobalFrame()));
    }

    @Override
    public Survey buildSurvey(Survey survey) throws RemoteException {
        org.droidplanner.core.mission.Mission droneMission = this.droneMgr.getDrone().getMission();
        org.droidplanner.core.mission.survey.Survey updatedSurvey = (org.droidplanner.core.mission.survey.Survey) ProxyUtils.getMissionItem
                (droneMission, survey);

        Survey proxySurvey = (Survey) ProxyUtils.getProxyMissionItem(updatedSurvey);
        return proxySurvey;
    }

    @Override
    public StructureScanner buildStructureScanner(StructureScanner item) throws RemoteException {
        org.droidplanner.core.mission.Mission droneMission = this.droneMgr.getDrone().getMission();
        org.droidplanner.core.mission.waypoints.StructureScanner updatedScan = (org.droidplanner.core.mission.waypoints.StructureScanner) ProxyUtils
                .getMissionItem(droneMission, item);

        StructureScanner proxyScanner = (StructureScanner) ProxyUtils.getProxyMissionItem(updatedScan);
        return proxyScanner;
    }

    @Override
    public void requestEventUpdates(IDroidPlannerApiCallback callback) throws RemoteException {
        if (callback != null)
            apiCallbacks.add(callback);
    }

    @Override
    public void removeEventUpdates(IDroidPlannerApiCallback callback) throws RemoteException {
        if (callback != null)
            apiCallbacks.remove(callback);
    }

    private void notifyDroneEvent(String droneEvent, Bundle extrasBundle) {
        if (apiCallbacks.isEmpty())
            return;

        if (droneEvent != null) {
            for (IDroidPlannerApiCallback callback : apiCallbacks) {
                try {
                    callback.onDroneEvent(droneEvent, extrasBundle);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                    try {
                        removeEventUpdates(callback);
                    } catch (RemoteException e1) {
                        Log.e(TAG, e1.getMessage(), e1);
                    }
                }
            }
        }
    }

    private void notifyConnectionFailed(ConnectionResult result) {
        if (apiCallbacks.isEmpty())
            return;

        if (result != null) {
            for (IDroidPlannerApiCallback callback : apiCallbacks) {
                try {
                    callback.onConnectionFailed(result);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                    try {
                        removeEventUpdates(callback);
                    } catch (RemoteException e1) {
                        Log.e(TAG, e1.getMessage(), e1);
                    }
                }
            }
        }
    }

    private static FollowType followModeToType(FollowAlgorithm.FollowModes followMode) {
        final FollowType followType;

        switch (followMode) {
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
        this.droneMgr.getFollowMe().changeRadius(radius);
    }

    @Override
    public void disableFollowMe() throws RemoteException {
        Follow follow = this.droneMgr.getFollowMe();
        if (follow.isEnabled())
            follow.toggleFollowMeState();
    }

    @Override
    public void triggerCamera() throws RemoteException {
        MavLinkROI.triggerCamera(this.droneMgr.getDrone());
    }

    @Override
    public void epmCommand(boolean release) throws RemoteException {
        MavLinkROI.empCommand(this.droneMgr.getDrone(), release);
    }

    @Override
    public void loadWaypoints() throws RemoteException {
        this.droneMgr.getDrone().getWaypointManager().getWaypoints();
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        Bundle extrasBundle = null;
        String droneEvent = null;

        switch (event) {
            case DISCONNECTED:
                droneEvent = Event.EVENT_DISCONNECTED;
                break;

            case GUIDEDPOINT:
                droneEvent = Event.EVENT_GUIDED_POINT;
                break;

            case RADIO:
                droneEvent = Event.EVENT_RADIO;
                break;

            case RC_IN:
                break;
            case RC_OUT:
                break;

            case ARMING_STARTED:
            case ARMING:
                droneEvent = Event.EVENT_ARMING;
                break;

            case AUTOPILOT_WARNING:
                extrasBundle = new Bundle(1);
                extrasBundle.putString(Extra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE,
                        drone.getState().getWarning());
                droneEvent = Event.EVENT_AUTOPILOT_FAILSAFE;
                break;

            case MODE:
                droneEvent = Event.EVENT_VEHICLE_MODE;
                break;

            case NAVIGATION:
            case ATTITUDE:
            case ORIENTATION:
                droneEvent = Event.EVENT_ATTITUDE;
                break;

            case SPEED:
                droneEvent = Event.EVENT_SPEED;
                break;

            case BATTERY:
                droneEvent = Event.EVENT_BATTERY;
                break;

            case STATE:
                droneEvent = Event.EVENT_STATE;
                break;

            case MISSION_UPDATE:
                droneEvent = Event.EVENT_MISSION_UPDATE;
                break;

            case MISSION_RECEIVED:
                droneEvent = Event.EVENT_MISSION_RECEIVED;
                break;

            case FIRMWARE:
            case TYPE:
                droneEvent = Event.EVENT_TYPE_UPDATED;
                break;

            case HOME:
                droneEvent = Event.EVENT_HOME;
                break;

            case GPS:
                droneEvent = Event.EVENT_GPS;
                break;

            case GPS_FIX:
                droneEvent = Event.EVENT_GPS_FIX;
                break;

            case GPS_COUNT:
                droneEvent = Event.EVENT_GPS_COUNT;
                break;

            case PARAMETER:
            case PARAMETERS_DOWNLOADED:
                droneEvent = Event.EVENT_PARAMETERS_RECEIVED;
                break;

            case CALIBRATION_IMU:
                final String calIMUMessage = this.droneMgr.getDrone().getCalibrationSetup()
                        .getMessage();
                extrasBundle = new Bundle(1);
                extrasBundle.putString(Extra.EXTRA_CALIBRATION_IMU_MESSAGE, calIMUMessage);
                droneEvent = Event.EVENT_CALIBRATION_IMU;
                break;

            case CALIBRATION_TIMEOUT:
                    /*
                 * here we will check if we are in calibration mode but if at
				 * the same time 'msg' is empty - then it is actually not doing
				 * calibration what we should do is to reset the calibration
				 * flag and re-trigger the HEARBEAT_TIMEOUT this however should
				 * not be happening
				 */
                final Calibration calibration = this.droneMgr.getDrone().getCalibrationSetup();
                final String message = calibration.getMessage();
                if (calibration.isCalibrating() && TextUtils.isEmpty(message)) {
                    calibration.setCalibrating(false);
                    droneEvent = Event.EVENT_HEARTBEAT_TIMEOUT;
                } else {
                    extrasBundle = new Bundle(1);
                    extrasBundle.putString(Extra.EXTRA_CALIBRATION_IMU_MESSAGE, message);
                    droneEvent = Event.EVENT_CALIBRATION_IMU_TIMEOUT;
                }
                break;

            case HEARTBEAT_TIMEOUT:
                droneEvent = Event.EVENT_HEARTBEAT_TIMEOUT;
                break;

            case HEARTBEAT_FIRST:
                extrasBundle = new Bundle(1);
                extrasBundle.putInt(Extra.EXTRA_MAVLINK_VERSION, drone.getMavlinkVersion());
                droneEvent = Event.EVENT_HEARTBEAT_FIRST;
                break;

            case HEARTBEAT_RESTORED:
                extrasBundle = new Bundle(1);
                extrasBundle.putInt(Extra.EXTRA_MAVLINK_VERSION, drone.getMavlinkVersion());
                droneEvent = Event.EVENT_HEARTBEAT_RESTORED;
                break;

            case CONNECTED:
                droneEvent = Event.EVENT_CONNECTED;
                break;

            case MISSION_SENT:
                droneEvent = Event.EVENT_MISSION_SENT;
                break;

            case INVALID_POLYGON:
                break;

            case MISSION_WP_UPDATE:
                final int currentWaypoint = this.droneMgr.getDrone().getMissionStats()
                        .getCurrentWP();
                extrasBundle = new Bundle(1);
                extrasBundle.putInt(Extra.EXTRA_MISSION_CURRENT_WAYPOINT, currentWaypoint);
                droneEvent = Event.EVENT_MISSION_ITEM_UPDATE;
                break;

            case FOLLOW_START:
                droneEvent = Event.EVENT_FOLLOW_START;
                break;

            case FOLLOW_STOP:
                droneEvent = Event.EVENT_FOLLOW_STOP;
                break;

            case FOLLOW_UPDATE:
            case FOLLOW_CHANGE_TYPE:
                droneEvent = Event.EVENT_FOLLOW_UPDATE;
                break;

            case WARNING_400FT_EXCEEDED:
                droneEvent = Event.EVENT_WARNING_400FT_EXCEEDED;
                break;

            case WARNING_SIGNAL_WEAK:
                droneEvent = Event.EVENT_WARNING_SIGNAL_WEAK;
                break;

            case WARNING_NO_GPS:
                droneEvent = Event.EVENT_WARNING_NO_GPS;
                break;

            case MAGNETOMETER:
                break;

            case FOOTPRINT:
                droneEvent = Event.EVENT_FOOTPRINT;
                break;
        }

        notifyDroneEvent(droneEvent, extrasBundle);
    }

    @Override
    public void onBeginReceivingParameters() {
        notifyDroneEvent(Event.EVENT_PARAMETERS_REFRESH_STARTED, null);
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        Bundle paramsBundle = new Bundle(2);
        paramsBundle.putInt(Extra.EXTRA_PARAMETER_INDEX, index);
        paramsBundle.putInt(Extra.EXTRA_PARAMETERS_COUNT, count);
        notifyDroneEvent(Event.EVENT_PARAMETERS_RECEIVED, paramsBundle);
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameter) {
        notifyDroneEvent(Event.EVENT_PARAMETERS_REFRESH_ENDED, null);
    }

    @Override
    public void onStarted(List<ThreeSpacePoint> points) {
        Bundle paramsBundle = new Bundle();
        double[][] pointsArr = MathUtils.threeSpacePointToPointsArray(points);
        paramsBundle.putSerializable(Extra.EXTRA_CALIBRATION_MAG_POINTS_X, pointsArr[0]);
        paramsBundle.putSerializable(Extra.EXTRA_CALIBRATION_MAG_POINTS_Y, pointsArr[1]);
        paramsBundle.putSerializable(Extra.EXTRA_CALIBRATION_MAG_POINTS_Z, pointsArr[2]);

        notifyDroneEvent(Event.EVENT_CALIBRATION_MAG_STARTED, paramsBundle);
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

        double[][] pointsArr = MathUtils.threeSpacePointToPointsArray(points);
        paramsBundle.putSerializable(Extra.EXTRA_CALIBRATION_MAG_POINTS_X, pointsArr[0]);
        paramsBundle.putSerializable(Extra.EXTRA_CALIBRATION_MAG_POINTS_Y, pointsArr[1]);
        paramsBundle.putSerializable(Extra.EXTRA_CALIBRATION_MAG_POINTS_Z, pointsArr[2]);

        notifyDroneEvent(Event.EVENT_CALIBRATION_MAG_ESTIMATION, paramsBundle);
    }

    @Override
    public void finished(FitPoints fit, double[] offsets) {
        double fitness = fit.getFitness();

        Bundle paramsBundle = new Bundle(2);
        paramsBundle.putDouble(Extra.EXTRA_CALIBRATION_MAG_FITNESS, fitness);
        paramsBundle.putDoubleArray(Extra.EXTRA_CALIBRATION_MAG_OFFSETS, offsets);

        notifyDroneEvent(Event.EVENT_CALIBRATION_MAG_COMPLETED, paramsBundle);
    }

    @Override
    public void onConnectionFailed(String error) {
        notifyConnectionFailed(new ConnectionResult(0, error));
    }
}
