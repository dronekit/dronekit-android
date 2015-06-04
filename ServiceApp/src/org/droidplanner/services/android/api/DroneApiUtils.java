package org.droidplanner.services.android.api;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.enums.MAG_CAL_STATUS;
import com.MAVLink.enums.MAV_TYPE;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationProgress;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationResult;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationStatus;
import com.o3dr.services.android.lib.drone.camera.GoPro;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.CameraProxy;
import com.o3dr.services.android.lib.drone.property.EkfStatus;
import com.o3dr.services.android.lib.drone.property.FootPrint;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;

import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.camera.GoProImpl;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Camera;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.survey.Footprint;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.utils.MathUtils;
import org.droidplanner.services.android.utils.ProxyUtils;
import org.droidplanner.services.android.utils.file.IO.ParameterMetadataLoader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fredia Huya-Kouadio on 3/23/15.
 */
public class DroneApiUtils {
    private static final String TAG = DroneApiUtils.class.getSimpleName();

    static VehicleMode getVehicleMode(ApmModes mode) {
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

            case ROTOR_BRAKE:
                return VehicleMode.COPTER_BRAKE;


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

    static int getDroneProxyType(int originalType) {
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

    static FootPrint getProxyCameraFootPrint(Footprint footprint) {
        if (footprint == null) return null;

        return new FootPrint(footprint.getGSD(),
                MathUtils.coord2DToLatLong(footprint.getVertexInGlobalFrame()));
    }

    static FollowAlgorithm.FollowModes followTypeToMode(FollowType followType) {
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

            case LOOK_AT_ME:
                followMode = FollowAlgorithm.FollowModes.LOOK_AT_ME;
                break;
        }
        return followMode;
    }

    static FollowType followModeToType(FollowAlgorithm.FollowModes followMode) {
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

            case LOOK_AT_ME:
                followType = FollowType.LOOK_AT_ME;
                break;
        }

        return followType;
    }

    static CameraProxy getCameraProxy(Drone drone, List<CameraDetail> cameraDetails) {
        final CameraDetail camDetail;
        final FootPrint currentFieldOfView;
        final List<FootPrint> proxyPrints = new ArrayList<>();

        if (drone == null) {
            camDetail = new CameraDetail();
            currentFieldOfView = new FootPrint();
        } else {
            Camera droneCamera = drone.getCamera();

            camDetail = ProxyUtils.getCameraDetail(droneCamera.getCamera());

            List<Footprint> footprints = droneCamera.getFootprints();
            for (Footprint footprint : footprints) {
                proxyPrints.add(DroneApiUtils.getProxyCameraFootPrint(footprint));
            }

            GPS droneGps = drone.getGps();
            currentFieldOfView = droneGps.isPositionValid()
                    ? DroneApiUtils.getProxyCameraFootPrint(droneCamera.getCurrentFieldOfView())
                    : new FootPrint();
        }

        return new CameraProxy(camDetail, currentFieldOfView, proxyPrints, cameraDetails);
    }

    static Gps getGps(Drone drone) {
        if (drone == null)
            return new Gps();

        final GPS droneGps = drone.getGps();
        LatLong dronePosition = droneGps.isPositionValid()
                ? new LatLong(droneGps.getPosition().getLat(), droneGps.getPosition().getLng())
                : null;

        return new Gps(dronePosition, droneGps.getGpsEPH(), droneGps.getSatCount(),
                droneGps.getFixTypeNumeric());
    }

    static State getState(Drone drone, boolean isConnected) {
        if (drone == null)
            return new State();

        org.droidplanner.core.drone.variables.State droneState = drone.getState();
        ApmModes droneMode = droneState.getMode();
        AccelCalibration accelCalibration = drone.getCalibrationSetup();
        String calibrationMessage = accelCalibration.isCalibrating() ? accelCalibration.getMessage() : null;
        final msg_ekf_status_report ekfStatus = droneState.getEkfStatus();
        final EkfStatus proxyEkfStatus = ekfStatus == null
                ? new EkfStatus()
                : new EkfStatus(ekfStatus.flags, ekfStatus.compass_variance, ekfStatus.pos_horiz_variance, ekfStatus
                .terrain_alt_variance, ekfStatus.velocity_variance, ekfStatus.pos_vert_variance);

        return new State(isConnected, DroneApiUtils.getVehicleMode(droneMode), droneState.isArmed(), droneState.isFlying(),
                droneState.getErrorId(), drone.getMavlinkVersion(), calibrationMessage,
                droneState.getFlightStartTime(), proxyEkfStatus, isConnected && drone.isConnectionAlive());
    }

    static Parameters getParameters(Drone drone, Context context) {
        if (drone == null)
            return new Parameters();

        final Map<String, Parameter> proxyParams = new HashMap<>();

        Map<String, org.droidplanner.core.parameters.Parameter> droneParameters = drone.getParameters().getParameters();
        if (!droneParameters.isEmpty()) {
            for (org.droidplanner.core.parameters.Parameter param : droneParameters.values()) {
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
                        ParameterMetadataLoader.load(context, metadataType, proxyParams);
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return new Parameters(new ArrayList<>(proxyParams.values()));
    }

    static Speed getSpeed(Drone drone) {
        if (drone == null)
            return new Speed();

        org.droidplanner.core.drone.variables.Speed droneSpeed = drone.getSpeed();
        return new Speed(droneSpeed.getVerticalSpeed(), droneSpeed.getGroundSpeed(), droneSpeed.getAirSpeed());
    }

    static Attitude getAttitude(Drone drone) {
        if (drone == null)
            return new Attitude();

        Orientation droneOrientation = drone.getOrientation();
        return new Attitude(droneOrientation.getRoll(), droneOrientation.getPitch(),
                droneOrientation.getYaw());
    }

    static Home getHome(Drone drone) {
        if (drone == null)
            return new Home();

        org.droidplanner.core.drone.variables.Home droneHome = drone.getHome();
        LatLongAlt homePosition = droneHome.isValid()
                ? new LatLongAlt(droneHome.getCoord().getLat(), droneHome.getCoord().getLng(),
                droneHome.getAltitude())
                : null;

        return new Home(homePosition);
    }

    static GoPro getGoPro(Drone drone) {
        if (drone == null)
            return new GoPro();

        GoProImpl impl = drone.getGoProImpl();
        return new GoPro(impl.isConnected(), impl.isRecording());
    }

    static Battery getBattery(Drone drone) {
        if (drone == null)
            return new Battery();

        org.droidplanner.core.drone.variables.Battery droneBattery = drone.getBattery();
        return new Battery(droneBattery.getBattVolt(), droneBattery.getBattRemain(),
                droneBattery.getBattCurrent(), droneBattery.getBattDischarge());
    }

    static Altitude getAltitude(Drone drone) {
        if (drone == null)
            return new Altitude();

        org.droidplanner.core.drone.variables.Altitude droneAltitude = drone.getAltitude();
        return new Altitude(droneAltitude.getAltitude(), droneAltitude.getTargetAltitude());
    }

    static Mission getMission(Drone drone) {
        Mission proxyMission = new Mission();
        if (drone == null)
            return proxyMission;

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

    static Signal getSignal(Drone drone) {
        if (drone == null)
            return new Signal();

        Radio droneRadio = drone.getRadio();
        return new Signal(droneRadio.isValid(), droneRadio.getRxErrors(), droneRadio.getFixed(),
                droneRadio.getTxBuf(), droneRadio.getRssi(), droneRadio.getRemRssi(),
                droneRadio.getNoise(), droneRadio.getRemNoise());
    }

    static Type getType(Drone drone) {
        if (drone == null)
            return new Type();

        return new Type(DroneApiUtils.getDroneProxyType(drone.getType()), drone.getFirmwareVersion());
    }

    static GuidedState getGuidedState(Drone drone) {
        if (drone == null)
            return new GuidedState();

        final GuidedPoint guidedPoint = drone.getGuidedPoint();
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
        double guidedAlt = guidedPoint.getAltitude();
        return new GuidedState(guidedState, new LatLongAlt(guidedCoord.getLat(), guidedCoord.getLng(), guidedAlt));
    }

    static void changeVehicleMode(Drone drone, VehicleMode newMode) {
        if (drone == null)
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

        drone.getState().changeFlightMode(ApmModes.getMode(newMode.getMode(), mavType));
    }

    static FollowState getFollowState(Follow followMe) {
        if (followMe == null)
            return new FollowState();

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
                                target.getAltitude()));
                    }
                    break;

                case FollowType.EXTRA_FOLLOW_RADIUS:
                    Double radius = (Double) entry.getValue();
                    if (radius != null)
                        params.putDouble(entry.getKey(), radius);
                    break;
            }
        }
        return new FollowState(state, DroneApiUtils.followModeToType(currentAlg.getType()), params);
    }

    static void disableFollowMe(Follow follow) {
        if (follow == null)
            return;

        if (follow.isEnabled())
            follow.toggleFollowMeState();
    }

    static void triggerCamera(Drone drone) throws RemoteException {
        if (drone == null)
            return;
        MavLinkDoCmds.triggerCamera(drone);
    }

    static void epmCommand(Drone drone, boolean release) {
        if (drone == null)
            return;

        MavLinkDoCmds.empCommand(drone, release);
    }

    static void loadWaypoints(Drone drone) {
        if (drone == null)
            return;
        drone.getWaypointManager().getWaypoints();
    }

    static void refreshParameters(Drone drone) {
        if (drone == null)
            return;
        drone.getParameters().refreshParameters();
    }

    static void writeParameters(Drone drone, Parameters parameters) {
        if (drone == null || parameters == null) return;

        List<Parameter> parametersList = parameters.getParameters();
        if (parametersList.isEmpty())
            return;

        org.droidplanner.core.drone.profiles.Parameters droneParams = drone.getParameters();
        for (Parameter proxyParam : parametersList) {
            droneParams.sendParameter(new org.droidplanner.core.parameters.Parameter(proxyParam.getName(), proxyParam.getValue(), proxyParam.getType()));
        }
    }

    static void setMission(Drone drone, Mission mission, boolean pushToDrone) {
        if (drone == null)
            return;

        org.droidplanner.core.mission.Mission droneMission = drone.getMission();
        droneMission.clearMissionItems();

        List<MissionItem> itemsList = mission.getMissionItems();
        for (MissionItem item : itemsList) {
            droneMission.addMissionItem(ProxyUtils.getMissionItemImpl(droneMission, item));
        }

        if (pushToDrone)
            droneMission.sendMissionToAPM();
    }

    static float generateDronie(Drone drone) {
        if (drone == null)
            return -1;

        return (float) drone.getMission().makeAndUploadDronie();
    }

    static void arm(Drone drone, boolean arm) {
        if (drone == null)
            return;
        MavLinkArm.sendArmMessage(drone, arm);
    }

    static void startMagnetometerCalibration(Drone drone, boolean retryOnFailure, boolean saveAutomatically, int
            startDelay) {
        if (drone == null)
            return;

        drone.getMagnetometerCalibration().startCalibration(retryOnFailure, saveAutomatically, startDelay);
    }

    static void cancelMagnetometerCalibration(Drone drone) {
        if (drone == null)
            return;

        drone.getMagnetometerCalibration().cancelCalibration();
    }

    public static void acceptMagnetometerCalibration(Drone drone) {
        if (drone == null)
            return;

        drone.getMagnetometerCalibration().acceptCalibration();
    }

    static boolean startIMUCalibration(Drone drone) {
        return drone != null && drone.getCalibrationSetup().startCalibration();
    }

    static void sendIMUCalibrationAck(Drone drone, int step) {
        if (drone == null)
            return;

        drone.getCalibrationSetup().sendAck(step);
    }

    static void doGuidedTakeoff(Drone drone, double altitude) {
        if (drone == null)
            return;

        drone.getGuidedPoint().doGuidedTakeoff(altitude);
    }

    static void sendMavlinkMessage(Drone drone, MavlinkMessageWrapper messageWrapper) {
        if (drone == null || messageWrapper == null)
            return;

        MAVLinkMessage message = messageWrapper.getMavLinkMessage();
        if (message == null)
            return;

        message.compid = drone.getCompid();
        message.sysid = drone.getSysid();

        //Set the target system and target component for MAVLink messages that support those
        //attributes.
        try {
            Class<?> tempMessage = message.getClass();
            Field target_system = tempMessage.getDeclaredField("target_system");
            Field target_component = tempMessage.getDeclaredField("target_component");

            target_system.setByte(message, (byte) message.sysid);
            target_component.setByte(message, (byte) message.compid);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | ExceptionInInitializerError e) {
            Log.e(TAG, e.getMessage(), e);
        }

        drone.getMavClient().sendMavPacket(message.pack());
    }

    static void sendGuidedPoint(Drone drone, LatLong point, boolean force) {
        if (drone == null)
            return;

        GuidedPoint guidedPoint = drone.getGuidedPoint();
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

    static void setGuidedAltitude(Drone drone, double altitude) {
        if (drone == null)
            return;

        drone.getGuidedPoint().changeGuidedAltitude(altitude);
    }

    static void enableFollowMe(DroneManager droneMgr, DroneInterfaces.Handler droneHandler, FollowType followType) {
        if (droneMgr == null)
            return;

        final FollowAlgorithm.FollowModes selectedMode = DroneApiUtils.followTypeToMode(followType);

        if (selectedMode != null) {
            final Follow followMe = droneMgr.getFollowMe();
            if (!followMe.isEnabled())
                followMe.toggleFollowMeState();

            FollowAlgorithm currentAlg = followMe.getFollowAlgorithm();
            if (currentAlg.getType() != selectedMode) {
                followMe.setAlgorithm(selectedMode.getAlgorithmType(droneMgr.getDrone(), droneHandler));
            }
        }
    }

    static void buildComplexMissionItem(Drone drone, Bundle itemBundle) {
        MissionItem missionItem = MissionItemType.restoreMissionItemFromBundle(itemBundle);
        if (missionItem == null || !(missionItem instanceof MissionItem.ComplexItem))
            return;

        final MissionItemType itemType = missionItem.getType();
        switch (itemType) {
            case SURVEY:
                Survey updatedSurvey = buildSurvey(drone, (Survey) missionItem);
                if (updatedSurvey != null)
                    itemType.storeMissionItem(updatedSurvey, itemBundle);
                break;

            case STRUCTURE_SCANNER:
                StructureScanner updatedScanner = buildStructureScanner(drone, (StructureScanner) missionItem);
                if (updatedScanner != null)
                    itemType.storeMissionItem(updatedScanner, itemBundle);
                break;

            default:
                Log.w(TAG, "Unrecognized complex mission item.");
                break;
        }
    }

    static Survey buildSurvey(Drone drone, Survey survey) {
        org.droidplanner.core.mission.Mission droneMission = drone == null ? null : drone.getMission();
        org.droidplanner.core.mission.survey.Survey updatedSurvey = (org.droidplanner.core.mission.survey.Survey) ProxyUtils.getMissionItemImpl
                (droneMission, survey);

        return (Survey) ProxyUtils.getProxyMissionItem(updatedSurvey);
    }

    static StructureScanner buildStructureScanner(Drone drone, StructureScanner item) {
        org.droidplanner.core.mission.Mission droneMission = drone == null ? null : drone.getMission();
        org.droidplanner.core.mission.waypoints.StructureScanner updatedScan = (org.droidplanner.core.mission.waypoints.StructureScanner) ProxyUtils
                .getMissionItemImpl(droneMission, item);

        StructureScanner proxyScanner = (StructureScanner) ProxyUtils.getProxyMissionItem(updatedScan);
        return proxyScanner;
    }

    static void startVideoRecording(Drone drone) {
        if (drone == null)
            return;

        drone.getGoProImpl().startRecording();
    }

    static void stopVideoRecording(Drone drone) {
        if (drone == null)
            return;

        drone.getGoProImpl().stopRecording();
    }

    static MagnetometerCalibrationStatus getMagnetometerCalibrationStatus(Drone drone) {
        final MagnetometerCalibrationStatus calStatus = new MagnetometerCalibrationStatus();
        if (drone != null) {
            final MagnetometerCalibrationImpl magCalImpl = drone.getMagnetometerCalibration();
            calStatus.setCalibrationCancelled(magCalImpl.isCancelled());

            Collection<MagnetometerCalibrationImpl.Info> calibrationInfo = magCalImpl.getMagCalibrationTracker().values();
            for (MagnetometerCalibrationImpl.Info info : calibrationInfo) {
                calStatus.addCalibrationProgress(getMagnetometerCalibrationProgress(info.getCalProgress()));
                calStatus.addCalibrationResult(getMagnetometerCalibrationResult(info.getCalReport()));
            }
        }

        return calStatus;
    }

    static MagnetometerCalibrationProgress getMagnetometerCalibrationProgress(msg_mag_cal_progress msgProgress) {
        if (msgProgress == null)
            return null;

        return new MagnetometerCalibrationProgress(msgProgress.compass_id, msgProgress.completion_pct,
                msgProgress.direction_x, msgProgress.direction_y, msgProgress.direction_z);
    }

    static MagnetometerCalibrationResult getMagnetometerCalibrationResult(msg_mag_cal_report msgReport) {
        if (msgReport == null)
            return null;

        return new MagnetometerCalibrationResult(msgReport.compass_id,
                msgReport.cal_status == MAG_CAL_STATUS.MAG_CAL_SUCCESS, msgReport.autosaved == 1, msgReport.fitness,
                msgReport.ofs_x, msgReport.ofs_y, msgReport.ofs_z,
                msgReport.diag_x, msgReport.diag_y, msgReport.diag_z,
                msgReport.offdiag_x, msgReport.offdiag_y, msgReport.offdiag_z);
    }
}
