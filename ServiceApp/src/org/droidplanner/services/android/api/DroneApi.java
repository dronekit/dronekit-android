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

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.enums.MAV_TYPE;
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
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.CameraProxy;
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
import com.o3dr.services.android.lib.gcs.action.CalibrationActions;
import com.o3dr.services.android.lib.gcs.action.FollowMeActions;
import com.o3dr.services.android.lib.gcs.event.GCSEvent;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.model.IDroneApi;
import com.o3dr.services.android.lib.model.IMavlinkObserver;
import com.o3dr.services.android.lib.model.IObserver;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Calibration;
import org.droidplanner.core.drone.variables.Camera;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.core.survey.CameraInfo;
import org.droidplanner.core.survey.Footprint;
import org.droidplanner.core.util.Pair;
import org.droidplanner.services.android.R;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.utils.MathUtils;
import org.droidplanner.services.android.utils.ProxyUtils;
import org.droidplanner.services.android.utils.file.IO.CameraInfoLoader;
import org.droidplanner.services.android.utils.file.IO.ParameterMetadataLoader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

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
    private List<CameraDetail> cachedCameraDetails;

    DroneApi(DroidPlannerService dpService, Looper looper, MavLinkServiceApi mavlinkApi, IApiListener listener,
             String ownerId) {

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

    @Override
    public Bundle getAttribute(String type) throws RemoteException {
        Bundle carrier = new Bundle();
        switch (type) {
            case AttributeType.STATE:
                carrier.putParcelable(type, getState());
                break;
            case AttributeType.GPS:
                carrier.putParcelable(type, getGps());
                break;
            case AttributeType.PARAMETERS:
                carrier.putParcelable(type, getParameters());
                break;
            case AttributeType.SPEED:
                carrier.putParcelable(type, getSpeed());
                break;
            case AttributeType.ATTITUDE:
                carrier.putParcelable(type, getAttitude());
                break;
            case AttributeType.HOME:
                carrier.putParcelable(type, getHome());
                break;
            case AttributeType.BATTERY:
                carrier.putParcelable(type, getBattery());
                break;
            case AttributeType.ALTITUDE:
                carrier.putParcelable(type, getAltitude());
                break;
            case AttributeType.MISSION:
                carrier.putParcelable(type, getMission());
                break;
            case AttributeType.SIGNAL:
                carrier.putParcelable(type, getSignal());
                break;
            case AttributeType.TYPE:
                carrier.putParcelable(type, getType());
                break;
            case AttributeType.GUIDED_STATE:
                carrier.putParcelable(type, getGuidedState());
                break;
            case AttributeType.FOLLOW_STATE:
                carrier.putParcelable(type, getFollowState());
                break;
            case AttributeType.CAMERA:
                carrier.putParcelable(type, getCameraProxy());
                break;
        }

        return carrier;
    }

    private CameraProxy getCameraProxy() {
        if (droneMgr == null)
            return null;

        Drone drone = droneMgr.getDrone();
        Camera droneCamera = drone.getCamera();

        List<Footprint> footprints = droneCamera.getFootprints();
        final int printsCount = footprints.size();

        List<FootPrint> proxyPrints = new ArrayList<FootPrint>(footprints.size());
        for (Footprint footprint : footprints) {
            proxyPrints.add(getProxyCameraFootPrint(footprint));
        }

        GPS droneGps = drone.getGps();
        final FootPrint currentFieldOfView = droneGps.isPositionValid()
                ? getProxyCameraFootPrint(droneCamera.getCurrentFieldOfView())
                : new FootPrint();

        return new CameraProxy(ProxyUtils.getCameraDetail(droneCamera.getCamera()),
                currentFieldOfView, proxyPrints, getCameraDetails());
    }

    private Gps getGps() {
        if (droneMgr == null)
            return new Gps();

        final GPS droneGps = droneMgr.getDrone().getGps();
        LatLong dronePosition = droneGps.isPositionValid()
                ? new LatLong(droneGps.getPosition().getLat(), droneGps.getPosition().getLng())
                : null;

        return new Gps(dronePosition, droneGps.getGpsEPH(), droneGps.getSatCount(),
                droneGps.getFixTypeNumeric());
    }

    private State getState() {
        if (droneMgr == null)
            return new State();

        final Drone drone = this.droneMgr.getDrone();
        org.droidplanner.core.drone.variables.State droneState = drone.getState();
        ApmModes droneMode = droneState.getMode();
        Calibration calibration = drone.getCalibrationSetup();
        String calibrationMessage = calibration.isCalibrating() ? calibration.getMessage() : null;

        return new State(isConnected(), getVehicleMode(droneMode), droneState.isArmed(), droneState.isFlying(),
                droneState.getWarning(), drone.getMavlinkVersion(), calibrationMessage,
                droneState.getFlightStartTime());
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

    private Parameters getParameters() {
        if (droneMgr == null)
            return new Parameters();

        final Drone drone = this.droneMgr.getDrone();
        final Map<String, com.o3dr.services.android.lib.drone.property.Parameter> proxyParams = new HashMap<>();

        Map<String, Parameter> droneParameters = drone.getParameters().getParameters();
        if (!droneParameters.isEmpty()) {
            for (Parameter param : droneParameters.values()) {
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
                        ParameterMetadataLoader.load(this.context, metadataType, proxyParams);
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return new Parameters(new ArrayList<>(proxyParams.values()));
    }

    private Speed getSpeed() {
        if (droneMgr == null)
            return new Speed();

        org.droidplanner.core.drone.variables.Speed droneSpeed = this.droneMgr.getDrone().getSpeed();
        return new Speed(droneSpeed.getVerticalSpeed().valueInMetersPerSecond(),
                droneSpeed.getGroundSpeed().valueInMetersPerSecond(),
                droneSpeed.getAirSpeed().valueInMetersPerSecond());
    }

    private Attitude getAttitude() {
        if (droneMgr == null)
            return new Attitude();

        Orientation droneOrientation = this.droneMgr.getDrone().getOrientation();
        return new Attitude(droneOrientation.getRoll(), droneOrientation.getPitch(),
                droneOrientation.getYaw());
    }

    private Home getHome() {
        if (droneMgr == null)
            return new Home();

        org.droidplanner.core.drone.variables.Home droneHome = this.droneMgr.getDrone().getHome();
        LatLongAlt homePosition = droneHome.isValid()
                ? new LatLongAlt(droneHome.getCoord().getLat(), droneHome.getCoord().getLng(),
                droneHome.getAltitude().valueInMeters())
                : null;

        return new Home(homePosition);
    }

    private Battery getBattery() {
        if (droneMgr == null)
            return new Battery();

        org.droidplanner.core.drone.variables.Battery droneBattery = this.droneMgr.getDrone().getBattery();
        return new Battery(droneBattery.getBattVolt(), droneBattery.getBattRemain(),
                droneBattery.getBattCurrent(), droneBattery.getBattDischarge());
    }

    private Altitude getAltitude() {
        if (droneMgr == null)
            return new Altitude();

        org.droidplanner.core.drone.variables.Altitude droneAltitude = this.droneMgr.getDrone().getAltitude();
        return new Altitude(droneAltitude.getAltitude(), droneAltitude.getTargetAltitude());
    }

    private Mission getMission() {
        Mission proxyMission = new Mission();
        if (droneMgr == null)
            return proxyMission;

        final Drone drone = this.droneMgr.getDrone();
        org.droidplanner.core.mission.Mission droneMission = drone.getMission();
        List<org.droidplanner.core.mission.MissionItem> droneMissionItems = droneMission.getItems();


        proxyMission.setCurrentMissionItem((short) drone.getMissionStats().getCurrentWP());
        if (!droneMissionItems.isEmpty()) {
            for (org.droidplanner.core.mission.MissionItem item : droneMissionItems) {
                proxyMission.addMissionItem(ProxyUtils.getProxyMissionItem(item));
            }
        }

        return proxyMission;
    }

    private Signal getSignal() {
        if (droneMgr == null)
            return new Signal();

        Radio droneRadio = this.droneMgr.getDrone().getRadio();
        return new Signal(droneRadio.isValid(), droneRadio.getRxErrors(), droneRadio.getFixed(),
                droneRadio.getTxBuf(), droneRadio.getRssi(), droneRadio.getRemRssi(),
                droneRadio.getNoise(), droneRadio.getRemNoise());
    }

    private Type getType() {
        if (droneMgr == null)
            return new Type();

        final Drone drone = this.droneMgr.getDrone();
        return new Type(getDroneProxyType(drone.getType()), drone.getFirmwareVersion());
    }

    public boolean isConnected() {
        return droneMgr != null && droneMgr.isConnected();
    }

    private GuidedState getGuidedState() {
        if (droneMgr == null)
            return new GuidedState();

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

    public void changeVehicleMode(VehicleMode newMode) {
        if (droneMgr == null)
            return;

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
        } catch (ConnectionException e) {
            notifyConnectionFailed(new ConnectionResult(0, e.getMessage()));
        }
    }

    public void refreshParameters() {
        if (droneMgr == null)
            return;
        this.droneMgr.getDrone().getParameters().refreshParameters();
    }

    public void writeParameters(Parameters parameters) {
        if (droneMgr == null || parameters == null) return;

        List<com.o3dr.services.android.lib.drone.property.Parameter> parametersList = parameters.getParameters();
        if (parametersList.isEmpty())
            return;

        final Drone drone = this.droneMgr.getDrone();
        org.droidplanner.core.drone.profiles.Parameters droneParams = drone.getParameters();
        for (com.o3dr.services.android.lib.drone.property.Parameter proxyParam : parametersList) {
            droneParams.sendParameter(new Parameter(proxyParam.getName(), proxyParam.getValue(), proxyParam.getType()));
        }
    }

    public void setMission(Mission mission, boolean pushToDrone) {
        if (droneMgr == null)
            return;

        org.droidplanner.core.mission.Mission droneMission = this.droneMgr.getDrone().getMission();
        droneMission.clearMissionItems();

        List<MissionItem> itemsList = mission.getMissionItems();
        for (MissionItem item : itemsList) {
            droneMission.addMissionItem(ProxyUtils.getMissionItemImpl(droneMission, item));
        }

        if (pushToDrone)
            droneMission.sendMissionToAPM();
    }

    public void generateDronie() {
        if (droneMgr == null)
            return;

        float bearing = (float) this.droneMgr.getDrone().getMission().makeAndUploadDronie();
        Bundle bundle = new Bundle(1);
        bundle.putFloat(AttributeEventExtra.EXTRA_MISSION_DRONIE_BEARING, bearing);
        notifyAttributeUpdate(AttributeEvent.MISSION_DRONIE_CREATED, bundle);
    }

    public void arm(boolean arm) {
        if (droneMgr == null)
            return;
        MavLinkArm.sendArmMessage(this.droneMgr.getDrone(), arm);
    }

    public void startMagnetometerCalibration(double[] startPointsX, double[] startPointsY, double[] startPointsZ) {
        if (droneMgr == null)
            return;

        this.droneMgr.startMagnetometerCalibration(MathUtils.pointsArrayToThreeSpacePoint(new
                double[][]{startPointsX, startPointsY, startPointsZ}));
    }

    public void stopMagnetometerCalibration() {
        if (droneMgr == null)
            return;
        this.droneMgr.stopMagnetometerCalibration();
    }

    public void startIMUCalibration() {
        if (droneMgr == null)
            return;

        if (!this.droneMgr.getDrone().getCalibrationSetup().startCalibration()) {
            Bundle extrasBundle = new Bundle(1);
            extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE,
                    context.getString(R.string.failed_start_calibration_message));
            notifyAttributeUpdate(AttributeEvent.CALIBRATION_IMU_ERROR, extrasBundle);
        }
    }

    public void sendIMUCalibrationAck(int step) {
        if (droneMgr == null)
            return;

        this.droneMgr.getDrone().getCalibrationSetup().sendAckk(step);
    }

    public void doGuidedTakeoff(double altitude) {
        if (droneMgr == null)
            return;

        this.droneMgr.getDrone().getGuidedPoint().doGuidedTakeoff(new org.droidplanner.core
                .helpers.units.Altitude(altitude));
    }

    public void sendMavlinkMessage(MavlinkMessageWrapper messageWrapper) {
        if (droneMgr == null || messageWrapper == null)
            return;

        MAVLinkMessage message = messageWrapper.getMavLinkMessage();
        if (message == null)
            return;

        Drone drone = droneMgr.getDrone();
        if (drone == null)
            return;

        message.compid = drone.getCompid();
        message.sysid = drone.getSysid();
        drone.getMavClient().sendMavPacket(message.pack());
    }

    public void sendGuidedPoint(LatLong point, boolean force) {
        if (droneMgr == null)
            return;

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

    public void setGuidedAltitude(double altitude) {
        if (droneMgr == null)
            return;

        this.droneMgr.getDrone().getGuidedPoint().changeGuidedAltitude(altitude);
    }

    public void enableFollowMe(FollowType followType) {
        if (droneMgr == null)
            return;

        final FollowAlgorithm.FollowModes selectedMode = followTypeToMode(followType);

        if (selectedMode != null) {
            final Follow followMe = this.droneMgr.getFollowMe();
            if (!followMe.isEnabled())
                followMe.toggleFollowMeState();

            FollowAlgorithm currentAlg = followMe.getFollowAlgorithm();
            if (currentAlg.getType() != selectedMode) {
                followMe.setAlgorithm(selectedMode.getAlgorithmType(droneMgr.getDrone(), droneHandler));
            }
        }
    }

    private FollowState getFollowState() {
        if (droneMgr == null)
            return new FollowState();

        final Follow followMe = this.droneMgr.getFollowMe();

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

        final FollowAlgorithm currentAlg = followMe.getFollowAlgorithm();
        Map<String, Object> modeParams = currentAlg.getParams();
        Bundle params = new Bundle();
        for (Map.Entry<String, Object> entry : modeParams.entrySet()) {
            switch (entry.getKey()) {
                case FollowType.EXTRA_FOLLOW_ROI_TARGET:
                    Coord3D target = (Coord3D) entry.getValue();
                    if (target != null) {
                        params.putParcelable(entry.getKey(), new LatLongAlt(target.getLat(), target.getLng(),
                                target.getAltitude().valueInMeters()));
                    }
                    break;

                case FollowType.EXTRA_FOLLOW_RADIUS:
                    Double radius = (Double) entry.getValue();
                    if (radius != null)
                        params.putDouble(entry.getKey(), radius);
                    break;
            }
        }
        return new FollowState(state, followModeToType(currentAlg.getType()), params);
    }

    private List<CameraDetail> getCameraDetails() {
        if (droneMgr == null)
            return Collections.emptyList();

        if (cachedCameraDetails == null) {
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

            List<CameraDetail> cameraDetails = new ArrayList<CameraDetail>(cameraInfos.size());
            for (CameraInfo camInfo : cameraInfos) {
                cameraDetails.add(new CameraDetail(camInfo.name, camInfo.sensorWidth,
                        camInfo.sensorHeight, camInfo.sensorResolution, camInfo.focalLength,
                        camInfo.overlap, camInfo.sidelap, camInfo.isInLandscapeOrientation));
            }

            cachedCameraDetails = cameraDetails;
        }

        return cachedCameraDetails;
    }

    private static FootPrint getProxyCameraFootPrint(Footprint footprint) {
        if (footprint == null) return null;

        return new FootPrint(footprint.getGSD(),
                MathUtils.coord2DToLatLong(footprint.getVertexInGlobalFrame()));
    }

    public void buildComplexMissionItem(Bundle itemBundle) {
        MissionItem missionItem = MissionItemType.restoreMissionItemFromBundle(itemBundle);
        if (missionItem == null || !(missionItem instanceof MissionItem.ComplexItem))
            return;

        final MissionItemType itemType = missionItem.getType();
        switch (itemType) {
            case SURVEY:
                Survey updatedSurvey = buildSurvey((Survey) missionItem);
                if (updatedSurvey != null)
                    itemType.storeMissionItem(updatedSurvey, itemBundle);
                break;

            case STRUCTURE_SCANNER:
                StructureScanner updatedScanner = buildStructureScanner((StructureScanner)
                        missionItem);
                if (updatedScanner != null)
                    itemType.storeMissionItem(updatedScanner, itemBundle);
                break;

            default:
                Log.w(TAG, "Unrecognized complex mission item.");
                break;
        }
    }

    private Survey buildSurvey(Survey survey) {
        if (droneMgr == null)
            return survey;

        org.droidplanner.core.mission.Mission droneMission = this.droneMgr.getDrone().getMission();
        org.droidplanner.core.mission.survey.Survey updatedSurvey = (org.droidplanner.core.mission.survey.Survey) ProxyUtils.getMissionItemImpl
                (droneMission, survey);

        return (Survey) ProxyUtils.getProxyMissionItem(updatedSurvey);
    }

    private StructureScanner buildStructureScanner(StructureScanner item) {
        if (droneMgr == null)
            return item;

        org.droidplanner.core.mission.Mission droneMission = this.droneMgr.getDrone().getMission();
        org.droidplanner.core.mission.waypoints.StructureScanner updatedScan = (org.droidplanner.core.mission.waypoints.StructureScanner) ProxyUtils
                .getMissionItemImpl(droneMission, item);

        StructureScanner proxyScanner = (StructureScanner) ProxyUtils.getProxyMissionItem(updatedScan);
        return proxyScanner;
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
                generateDronie();
                break;

            case MissionActions.ACTION_LOAD_WAYPOINTS:
                loadWaypoints();
                break;

            case MissionActions.ACTION_SET_MISSION:
                data.setClassLoader(Mission.class.getClassLoader());
                Mission mission = data.getParcelable(MissionActions.EXTRA_MISSION);
                boolean pushToDrone = data.getBoolean(MissionActions.EXTRA_PUSH_TO_DRONE);
                setMission(mission, pushToDrone);
                break;

            case MissionActions.ACTION_BUILD_COMPLEX_MISSION_ITEM:
                buildComplexMissionItem(data);
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
                epmCommand(release);
                break;

            case ExperimentalActions.ACTION_TRIGGER_CAMERA:
                triggerCamera();
                break;

            case ExperimentalActions.ACTION_SEND_MAVLINK_MESSAGE:
                data.setClassLoader(MavlinkMessageWrapper.class.getClassLoader());
                MavlinkMessageWrapper messageWrapper = data.getParcelable(ExperimentalActions.EXTRA_MAVLINK_MESSAGE);
                sendMavlinkMessage(messageWrapper);
                break;

            case ExperimentalActions.ACTION_SET_RELAY:
                if (droneMgr != null) {
                    int relayNumber = data.getInt(ExperimentalActions.EXTRA_RELAY_NUMBER);
                    boolean isOn = data.getBoolean(ExperimentalActions.EXTRA_IS_RELAY_ON);
                    MavLinkDoCmds.setRelay(droneMgr.getDrone(), relayNumber, isOn);
                }
                break;

            //GUIDED ACTIONS
            case GuidedActions.ACTION_DO_GUIDED_TAKEOFF:
                double takeoffAltitude = data.getDouble(GuidedActions.EXTRA_ALTITUDE);
                doGuidedTakeoff(takeoffAltitude);
                break;

            case GuidedActions.ACTION_SEND_GUIDED_POINT:
                data.setClassLoader(LatLong.class.getClassLoader());
                boolean force = data.getBoolean(GuidedActions.EXTRA_FORCE_GUIDED_POINT);
                LatLong guidedPoint = data.getParcelable(GuidedActions.EXTRA_GUIDED_POINT);
                sendGuidedPoint(guidedPoint, force);
                break;

            case GuidedActions.ACTION_SET_GUIDED_ALTITUDE:
                double guidedAltitude = data.getDouble(GuidedActions.EXTRA_ALTITUDE);
                setGuidedAltitude(guidedAltitude);
                break;

            //PARAMETER ACTIONS
            case ParameterActions.ACTION_REFRESH_PARAMETERS:
                refreshParameters();
                break;

            case ParameterActions.ACTION_WRITE_PARAMETERS:
                data.setClassLoader(Parameters.class.getClassLoader());
                Parameters parameters = data.getParcelable(ParameterActions.EXTRA_PARAMETERS);
                writeParameters(parameters);
                break;

            //DRONE STATE ACTIONS
            case StateActions.ACTION_ARM:
                boolean doArm = data.getBoolean(StateActions.EXTRA_ARM);
                arm(doArm);
                break;

            case StateActions.ACTION_SET_VEHICLE_MODE:
                data.setClassLoader(VehicleMode.class.getClassLoader());
                VehicleMode newMode = data.getParcelable(StateActions.EXTRA_VEHICLE_MODE);
                changeVehicleMode(newMode);
                break;

            //CALIBRATION ACTIONS
            case CalibrationActions.ACTION_START_IMU_CALIBRATION:
                startIMUCalibration();
                break;

            case CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK:
                int imuAck = data.getInt(CalibrationActions.EXTRA_IMU_STEP);
                sendIMUCalibrationAck(imuAck);
                break;

            case CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION:
                double[] startX = data.getDoubleArray(CalibrationActions.EXTRA_MAGNETOMETER_START_X);
                double[] startY = data.getDoubleArray(CalibrationActions.EXTRA_MAGNETOMETER_START_Y);
                double[] startZ = data.getDoubleArray(CalibrationActions.EXTRA_MAGNETOMETER_START_Z);
                startMagnetometerCalibration(startX, startY, startZ);
                break;

            case CalibrationActions.ACTION_STOP_MAGNETOMETER_CALIBRATION:
                stopMagnetometerCalibration();
                break;

            //FOLLOW-ME ACTIONS
            case FollowMeActions.ACTION_ENABLE_FOLLOW_ME:
                data.setClassLoader(FollowType.class.getClassLoader());
                FollowType followType = data.getParcelable(FollowMeActions.EXTRA_FOLLOW_TYPE);
                enableFollowMe(followType);
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
                                                new org.droidplanner.core.helpers.units.Altitude(((LatLongAlt) target)
                                                        .getAltitude()));
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
                disableFollowMe();
                break;
        }
    }

    @Override
    public void performAsyncAction(Action action) throws RemoteException {
        performAction(action);
    }

    private void notifyAttributeUpdate(List<Pair<String, Bundle>> attributesInfo){
        if(observersList.isEmpty() || attributesInfo == null || attributesInfo.isEmpty())
            return;

        for(Pair<String, Bundle> info: attributesInfo){
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

    private static FollowAlgorithm.FollowModes followTypeToMode(FollowType followType) {
        final FollowAlgorithm.FollowModes followMode;

        switch (followType) {
            case ABOVE:
                followMode = FollowAlgorithm.FollowModes.ABOVE;
                break;

            case LEAD:
                followMode = FollowAlgorithm.FollowModes.LEAD;
                break;

            default:
            case LEASH:
                followMode = FollowAlgorithm.FollowModes.LEASH;
                break;

            case CIRCLE:
                followMode = FollowAlgorithm.FollowModes.CIRCLE;
                break;

            case LEFT:
                followMode = FollowAlgorithm.FollowModes.LEFT;
                break;

            case RIGHT:
                followMode = FollowAlgorithm.FollowModes.RIGHT;
                break;

            case SPLINE_LEASH:
                followMode = FollowAlgorithm.FollowModes.SPLINE_LEASH;
                break;

            case SPLINE_ABOVE:
                followMode = FollowAlgorithm.FollowModes.SPLINE_ABOVE;
                break;

            case GUIDED_SCAN:
                followMode = FollowAlgorithm.FollowModes.GUIDED_SCAN;
                break;
        }
        return followMode;
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

            case SPLINE_LEASH:
                followType = FollowType.SPLINE_LEASH;
                break;

            case SPLINE_ABOVE:
                followType = FollowType.SPLINE_ABOVE;
                break;

            case GUIDED_SCAN:
                followType = FollowType.GUIDED_SCAN;
                break;
        }

        return followType;
    }

    public void disableFollowMe() {
        if (droneMgr == null)
            return;

        Follow follow = this.droneMgr.getFollowMe();
        if (follow.isEnabled())
            follow.toggleFollowMeState();
    }

    public void triggerCamera() throws RemoteException {
        if (droneMgr == null)
            return;
        MavLinkDoCmds.triggerCamera(this.droneMgr.getDrone());
    }

    public void epmCommand(boolean release) {
        if (droneMgr == null)
            return;

        MavLinkDoCmds.empCommand(this.droneMgr.getDrone(), release);
    }

    public void loadWaypoints() {
        if (droneMgr == null)
            return;
        this.droneMgr.getDrone().getWaypointManager().getWaypoints();
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
                extrasBundle = new Bundle(2);
                extrasBundle.putInt(AttributeEventExtra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE_LEVEL, Log.ERROR);
                extrasBundle.putString(AttributeEventExtra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE,
                        drone.getState().getWarning());
                droneEvent = AttributeEvent.AUTOPILOT_FAILSAFE;
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

            case PARAMETER:
            case PARAMETERS_DOWNLOADED:
                droneEvent = AttributeEvent.PARAMETERS_RECEIVED;
                break;

            case CALIBRATION_IMU:
                if (droneMgr != null) {
                    final String calIMUMessage = this.droneMgr.getDrone().getCalibrationSetup()
                            .getMessage();
                    extrasBundle = new Bundle(1);
                    extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE, calIMUMessage);
                }
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
                if (droneMgr != null) {
                    final Calibration calibration = this.droneMgr.getDrone().getCalibrationSetup();
                    final String message = calibration.getMessage();
                    if (calibration.isCalibrating() && TextUtils.isEmpty(message)) {
                        calibration.setCalibrating(false);
                        droneEvent = AttributeEvent.HEARTBEAT_TIMEOUT;
                    } else {
                        extrasBundle = new Bundle(1);
                        extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE, message);
                        droneEvent = AttributeEvent.CALIBRATION_IMU_TIMEOUT;
                    }
                } else {
                    droneEvent = AttributeEvent.CALIBRATION_IMU_TIMEOUT;
                }
                break;

            case HEARTBEAT_TIMEOUT:
                droneEvent = AttributeEvent.HEARTBEAT_TIMEOUT;
                break;

            case CONNECTING:
                extrasBundle = new Bundle(2);
                extrasBundle.putInt(AttributeEventExtra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE_LEVEL, Log.INFO);
                extrasBundle.putString(AttributeEventExtra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE,
                        "Connecting...");
                droneEvent = AttributeEvent.AUTOPILOT_FAILSAFE;
                break;

            case CHECKING_VEHICLE_LINK:
                extrasBundle = new Bundle(2);
                extrasBundle.putInt(AttributeEventExtra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE_LEVEL, Log.INFO);
                extrasBundle.putString(AttributeEventExtra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE,
                        "Checking vehicle link...");
                droneEvent = AttributeEvent.AUTOPILOT_FAILSAFE;
                break;

            case CONNECTION_FAILED:
                onConnectionFailed("");
                break;

            case HEARTBEAT_FIRST:
                if (droneMgr != null) {
                    final Bundle heartBeatExtras = new Bundle(1);
                    heartBeatExtras.putInt(AttributeEventExtra.EXTRA_MAVLINK_VERSION, drone.getMavlinkVersion());
                    attributesInfo.add(Pair.create(AttributeEvent.HEARTBEAT_FIRST, heartBeatExtras));
                }

            case CONNECTED:
                if(droneMgr != null){
                    //Broadcast the vehicle connection.
                    final ConnectionParameter sanitizedParameter = new ConnectionParameter(connectionParams
                            .getConnectionType(), connectionParams.getParamsBundle(), null);

                    context.sendBroadcast(new Intent(GCSEvent.ACTION_VEHICLE_CONNECTION)
                            .putExtra(GCSEvent.EXTRA_APP_ID, ownerId)
                            .putExtra(GCSEvent.EXTRA_VEHICLE_CONNECTION_PARAMETER, sanitizedParameter));

                    attributesInfo.add(Pair.<String, Bundle>create(AttributeEvent.STATE_CONNECTED, null));
                }
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
                if (droneMgr != null) {
                    final int currentWaypoint = this.droneMgr.getDrone().getMissionStats()
                            .getCurrentWP();
                    extrasBundle = new Bundle(1);
                    extrasBundle.putInt(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, currentWaypoint);
                    droneEvent = AttributeEvent.MISSION_ITEM_UPDATED;
                }
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

            case WARNING_400FT_EXCEEDED:
                droneEvent = AttributeEvent.ALTITUDE_400FT_EXCEEDED;
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
        }

        if(droneEvent != null) {
            notifyAttributeUpdate(droneEvent, extrasBundle);
        }

        if(!attributesInfo.isEmpty()){
            notifyAttributeUpdate(attributesInfo);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_REFRESH_STARTED, null);
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        Bundle paramsBundle = new Bundle(2);
        paramsBundle.putInt(AttributeEventExtra.EXTRA_PARAMETER_INDEX, index);
        paramsBundle.putInt(AttributeEventExtra.EXTRA_PARAMETERS_COUNT, count);
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_RECEIVED, paramsBundle);
    }

    @Override
    public void onEndReceivingParameters() {
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_REFRESH_ENDED, null);
    }

    @Override
    public void onStarted(List<ThreeSpacePoint> points) {
        Bundle paramsBundle = new Bundle();
        double[][] pointsArr = MathUtils.threeSpacePointToPointsArray(points);
        paramsBundle.putSerializable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_X, pointsArr[0]);
        paramsBundle.putSerializable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_Y, pointsArr[1]);
        paramsBundle.putSerializable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_Z, pointsArr[2]);

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_STARTED, paramsBundle);
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
        paramsBundle.putDouble(AttributeEventExtra.EXTRA_CALIBRATION_MAG_FITNESS, fitness);
        paramsBundle.putDoubleArray(AttributeEventExtra.EXTRA_CALIBRATION_MAG_FIT_CENTER, fitCenter);
        paramsBundle.putDoubleArray(AttributeEventExtra.EXTRA_CALIBRATION_MAG_FIT_RADII, fitRadii);

        double[][] pointsArr = MathUtils.threeSpacePointToPointsArray(points);
        paramsBundle.putSerializable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_X, pointsArr[0]);
        paramsBundle.putSerializable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_Y, pointsArr[1]);
        paramsBundle.putSerializable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_POINTS_Z, pointsArr[2]);

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_ESTIMATION, paramsBundle);
    }

    @Override
    public void finished(FitPoints fit, double[] offsets) {
        double fitness = fit.getFitness();

        Bundle paramsBundle = new Bundle(2);
        paramsBundle.putDouble(AttributeEventExtra.EXTRA_CALIBRATION_MAG_FITNESS, fitness);
        paramsBundle.putDoubleArray(AttributeEventExtra.EXTRA_CALIBRATION_MAG_OFFSETS, offsets);

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_COMPLETED, paramsBundle);
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
}
