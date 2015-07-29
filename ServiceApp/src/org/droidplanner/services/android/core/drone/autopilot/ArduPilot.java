package org.droidplanner.services.android.core.drone.autopilot;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.ardupilotmega.msg_mount_configure;
import com.MAVLink.ardupilotmega.msg_mount_status;
import com.MAVLink.ardupilotmega.msg_radio;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_mission_item_reached;
import com.MAVLink.common.msg_named_value_int;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_radio_status;
import com.MAVLink.common.msg_raw_imu;
import com.MAVLink.common.msg_rc_channels_raw;
import com.MAVLink.common.msg_servo_output_raw;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_MODE_FLAG;
import com.MAVLink.enums.MAV_MOUNT_MODE;
import com.MAVLink.enums.MAV_SEVERITY;
import com.MAVLink.enums.MAV_STATE;
import com.MAVLink.enums.MAV_SYS_STATUS_SENSOR;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.action.CapabilityActions;
import com.o3dr.services.android.lib.drone.action.ExperimentalActions;
import com.o3dr.services.android.lib.drone.action.GimbalActions;
import com.o3dr.services.android.lib.drone.action.GuidedActions;
import com.o3dr.services.android.lib.drone.action.ParameterActions;
import com.o3dr.services.android.lib.drone.action.StateActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.action.SoloLinkActions;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.action.CalibrationActions;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.MAVLink.MavLinkParameters;
import org.droidplanner.services.android.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.services.android.core.drone.DroneEvents;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.LogMessageListener;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.drone.profiles.Parameters;
import org.droidplanner.services.android.core.drone.profiles.VehicleProfile;
import org.droidplanner.services.android.core.drone.variables.Altitude;
import org.droidplanner.services.android.core.drone.variables.ApmModes;
import org.droidplanner.services.android.core.drone.variables.Battery;
import org.droidplanner.services.android.core.drone.variables.Camera;
import org.droidplanner.services.android.core.drone.variables.GPS;
import org.droidplanner.services.android.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.core.drone.variables.HeartBeat;
import org.droidplanner.services.android.core.drone.variables.Home;
import org.droidplanner.services.android.core.drone.variables.Magnetometer;
import org.droidplanner.services.android.core.drone.variables.MissionStats;
import org.droidplanner.services.android.core.drone.variables.Navigation;
import org.droidplanner.services.android.core.drone.variables.Orientation;
import org.droidplanner.services.android.core.drone.variables.RC;
import org.droidplanner.services.android.core.drone.variables.Radio;
import org.droidplanner.services.android.core.drone.variables.Speed;
import org.droidplanner.services.android.core.drone.variables.State;
import org.droidplanner.services.android.core.drone.variables.StreamRates;
import org.droidplanner.services.android.core.drone.variables.Type;
import org.droidplanner.services.android.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.helpers.coordinates.Coord3D;
import org.droidplanner.services.android.core.mission.Mission;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;
import org.droidplanner.services.android.core.parameters.Parameter;
import org.droidplanner.services.android.utils.CommonApiUtils;

public abstract class ArduPilot implements MavLinkDrone {

    public static final int AUTOPILOT_COMPONENT_ID = 1;
    public static final int ARTOO_COMPONENT_ID = 0;

    private final DroneEvents events;
    private final Type type;
    private VehicleProfile profile;
    private final org.droidplanner.services.android.core.drone.variables.GPS GPS;

    private final org.droidplanner.services.android.core.drone.variables.RC RC;
    private final Speed speed;
    private final Battery battery;
    private final Radio radio;
    private final Home home;
    private final Mission mission;
    private final MissionStats missionStats;
    private final StreamRates streamRates;
    private final Altitude altitude;
    private final Orientation orientation;
    private final Navigation navigation;
    private final GuidedPoint guidedPoint;
    private final AccelCalibration accelCalibrationSetup;
    private final WaypointManager waypointManager;
    private final Magnetometer mag;
    private final Camera footprints;
    private final State state;
    private final HeartBeat heartbeat;
    private final Parameters parameters;

    private final MAVLinkStreams.MAVLinkOutputStream MavClient;
    private final Preferences preferences;

    private final LogMessageListener logListener;
    private final MagnetometerCalibrationImpl magCalibration;

    private final Context context;

    public ArduPilot(Context context, MAVLinkStreams.MAVLinkOutputStream mavClient,
                     DroneInterfaces.Handler handler, Preferences pref, AutopilotWarningParser warningParser,
                     LogMessageListener logListener) {

        this.context = context;
        this.MavClient = mavClient;
        this.preferences = pref;
        this.logListener = logListener;

        events = new DroneEvents(this, handler);
        state = new State(this, handler, warningParser);
        heartbeat = new HeartBeat(this, handler);
        parameters = new Parameters(this, handler);
        this.waypointManager = new WaypointManager(this, handler);

        RC = new RC(this);
        GPS = new GPS(this);
        this.type = new Type(this);
        this.speed = new Speed(this);
        this.battery = new Battery(this);
        this.radio = new Radio(this);
        this.home = new Home(this);
        this.mission = new Mission(this);
        this.missionStats = new MissionStats(this);
        this.streamRates = new StreamRates(this);
        this.altitude = new Altitude(this);
        this.orientation = new Orientation(this);
        this.navigation = new Navigation(this);
        this.guidedPoint = new GuidedPoint(this, handler);
        this.accelCalibrationSetup = new AccelCalibration(this, handler);
        this.magCalibration = new MagnetometerCalibrationImpl(this);
        this.mag = new Magnetometer(this);
        this.footprints = new Camera(this);

        loadVehicleProfile();
    }

    @Override
    public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed, double climb) {
        this.altitude.setAltitude(altitude);
        speed.setGroundAndAirSpeeds(groundSpeed, airSpeed, climb);
    }

    @Override
    public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error) {
        missionStats.setDistanceToWp(disttowp);
        altitude.setAltitudeError(alt_error);
        speed.setSpeedError(aspd_error);
        notifyDroneEvent(DroneInterfaces.DroneEventsType.ORIENTATION);
    }

    @Override
    public boolean isConnected() {
        return MavClient.isConnected() && heartbeat.hasHeartbeat();
    }

    @Override
    public boolean isConnectionAlive() {
        return heartbeat.isConnectionAlive();
    }

    @Override
    public void addDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.addDroneListener(listener);
    }

    @Override
    public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.removeDroneListener(listener);
    }

    @Override
    public void notifyDroneEvent(final DroneInterfaces.DroneEventsType event) {
        events.notifyDroneEvent(event);
    }

    @Override
    public GPS getGps() {
        return GPS;
    }

    @Override
    public byte getSysid() {
        return heartbeat.getSysid();
    }

    @Override
    public byte getCompid() {
        return heartbeat.getCompid();
    }

    @Override
    public int getMavlinkVersion() {
        return heartbeat.getMavlinkVersion();
    }

    @Override
    public void onHeartbeat(msg_heartbeat msg) {
        heartbeat.onHeartbeat(msg);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public void setType(int type) {
        this.type.setType(type);
    }

    @Override
    public int getType() {
        return type.getType();
    }

    @Override
    public FirmwareType getFirmwareType() {
        return type.getFirmwareType();
    }

    @Override
    public void loadVehicleProfile() {
        profile = preferences.loadVehicleProfile(getFirmwareType());
    }

    @Override
    public VehicleProfile getVehicleProfile() {
        return profile;
    }

    @Override
    public MAVLinkStreams.MAVLinkOutputStream getMavClient() {
        return MavClient;
    }

    @Override
    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public WaypointManager getWaypointManager() {
        return waypointManager;
    }

    @Override
    public RC getRC() {
        return RC;
    }

    @Override
    public Speed getSpeed() {
        return speed;
    }

    @Override
    public Battery getBattery() {
        return battery;
    }

    @Override
    public Radio getRadio() {
        return radio;
    }

    @Override
    public Home getHome() {
        return home;
    }

    @Override
    public Mission getMission() {
        return mission;
    }

    @Override
    public MissionStats getMissionStats() {
        return missionStats;
    }

    @Override
    public StreamRates getStreamRates() {
        return streamRates;
    }

    @Override
    public Altitude getAltitude() {
        return altitude;
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public Navigation getNavigation() {
        return navigation;
    }

    @Override
    public GuidedPoint getGuidedPoint() {
        return guidedPoint;
    }

    @Override
    public AccelCalibration getCalibrationSetup() {
        return accelCalibrationSetup;
    }

    @Override
    public MagnetometerCalibrationImpl getMagnetometerCalibration() {
        return magCalibration;
    }

    @Override
    public String getFirmwareVersion() {
        return type.getFirmwareVersion();
    }

    @Override
    public void setFirmwareVersion(String message) {
        type.setFirmwareVersion(message);
    }

    @Override
    public Magnetometer getMagnetometer() {
        return mag;
    }

    public Camera getCamera() {
        return footprints;
    }

    @Override
    public void logMessage(int mavSeverity, String message) {
        if (logListener != null)
            logListener.onMessageLogged(mavSeverity, message);
    }


    @Override
    public DroneAttribute getAttribute(String attributeType) {
        if (TextUtils.isEmpty(attributeType))
            return null;

        switch (attributeType) {
            case AttributeType.STATE:
                return CommonApiUtils.getState(this, isConnected());

            case AttributeType.GPS:
                return CommonApiUtils.getGps(this);

            case AttributeType.PARAMETERS:
                return CommonApiUtils.getParameters(this, context);

            case AttributeType.SPEED:
                return CommonApiUtils.getSpeed(this);

            case AttributeType.ATTITUDE:
                return CommonApiUtils.getAttitude(this);

            case AttributeType.HOME:
                return CommonApiUtils.getHome(this);

            case AttributeType.BATTERY:
                return CommonApiUtils.getBattery(this);

            case AttributeType.ALTITUDE:
                return CommonApiUtils.getAltitude(this);

            case AttributeType.MISSION:
                return CommonApiUtils.getMission(this);

            case AttributeType.SIGNAL:
                return CommonApiUtils.getSignal(this);

            case AttributeType.TYPE:
                return CommonApiUtils.getType(this);

            case AttributeType.GUIDED_STATE:
                return CommonApiUtils.getGuidedState(this);

            case AttributeType.MAGNETOMETER_CALIBRATION_STATUS:
                return CommonApiUtils.getMagnetometerCalibrationStatus(this);
        }

        return null;
    }

    @Override
    public void executeAsyncAction(Action action, ICommandListener listener) {
        final String type = action.getType();
        Bundle data = action.getData();

        switch(type){
            // MISSION ACTIONS
            case MissionActions.ACTION_LOAD_WAYPOINTS:
                CommonApiUtils.loadWaypoints(this);
                break;

            case MissionActions.ACTION_SET_MISSION:
                data.setClassLoader(com.o3dr.services.android.lib.drone.mission.Mission.class.getClassLoader());
                com.o3dr.services.android.lib.drone.mission.Mission mission = data.getParcelable(MissionActions.EXTRA_MISSION);
                boolean pushToDrone = data.getBoolean(MissionActions.EXTRA_PUSH_TO_DRONE);
                CommonApiUtils.setMission(this, mission, pushToDrone);
                break;

            case MissionActions.ACTION_START_MISSION:
                boolean forceModeChange = data.getBoolean(MissionActions.EXTRA_FORCE_MODE_CHANGE);
                boolean forceArm = data.getBoolean(MissionActions.EXTRA_FORCE_ARM);
                CommonApiUtils.startMission(this, forceModeChange, forceArm, listener);
                break;

            //EXPERIMENTAL ACTIONS
            case ExperimentalActions.ACTION_EPM_COMMAND:
                boolean release = data.getBoolean(ExperimentalActions.EXTRA_EPM_RELEASE);
                CommonApiUtils.epmCommand(this, release, listener);
                break;

            case ExperimentalActions.ACTION_TRIGGER_CAMERA:
                CommonApiUtils.triggerCamera(this);
                break;

            case ExperimentalActions.ACTION_SET_ROI:
                LatLongAlt roi = data.getParcelable(ExperimentalActions.EXTRA_SET_ROI_LAT_LONG_ALT);
                if (roi != null) {
                    Coord3D coord3DRoi = new Coord3D(roi.getLatitude(), roi.getLongitude(), roi.getAltitude());
                    MavLinkDoCmds.setROI(this, coord3DRoi, listener);
                }
                break;

            case ExperimentalActions.ACTION_SEND_MAVLINK_MESSAGE:
                data.setClassLoader(MavlinkMessageWrapper.class.getClassLoader());
                MavlinkMessageWrapper messageWrapper = data.getParcelable(ExperimentalActions.EXTRA_MAVLINK_MESSAGE);
                CommonApiUtils.sendMavlinkMessage(this, messageWrapper);
                break;

            case ExperimentalActions.ACTION_SET_RELAY:
                    int relayNumber = data.getInt(ExperimentalActions.EXTRA_RELAY_NUMBER);
                    boolean isOn = data.getBoolean(ExperimentalActions.EXTRA_IS_RELAY_ON);
                    MavLinkDoCmds.setRelay(this, relayNumber, isOn, listener);
                break;

            case ExperimentalActions.ACTION_SET_SERVO:
                    int channel = data.getInt(ExperimentalActions.EXTRA_SERVO_CHANNEL);
                    int pwm = data.getInt(ExperimentalActions.EXTRA_SERVO_PWM);
                    MavLinkDoCmds.setServo(this, channel, pwm, listener);
                break;

            //GUIDED ACTIONS
            case GuidedActions.ACTION_DO_GUIDED_TAKEOFF:
                double takeoffAltitude = data.getDouble(GuidedActions.EXTRA_ALTITUDE);
                CommonApiUtils.doGuidedTakeoff(this, takeoffAltitude, listener);
                break;

            case GuidedActions.ACTION_SEND_GUIDED_POINT:
                data.setClassLoader(LatLong.class.getClassLoader());
                boolean force = data.getBoolean(GuidedActions.EXTRA_FORCE_GUIDED_POINT);
                LatLong guidedPoint = data.getParcelable(GuidedActions.EXTRA_GUIDED_POINT);
                CommonApiUtils.sendGuidedPoint(this, guidedPoint, force, listener);
                break;

            case GuidedActions.ACTION_SET_GUIDED_ALTITUDE:
                double guidedAltitude = data.getDouble(GuidedActions.EXTRA_ALTITUDE);
                CommonApiUtils.setGuidedAltitude(this, guidedAltitude);
                break;

            //PARAMETER ACTIONS
            case ParameterActions.ACTION_REFRESH_PARAMETERS:
                CommonApiUtils.refreshParameters(this);
                break;

            case ParameterActions.ACTION_WRITE_PARAMETERS:
                data.setClassLoader(com.o3dr.services.android.lib.drone.property.Parameters.class.getClassLoader());
                com.o3dr.services.android.lib.drone.property.Parameters parameters = data.getParcelable(ParameterActions.EXTRA_PARAMETERS);
                CommonApiUtils.writeParameters(this, parameters);
                break;

            //DRONE STATE ACTIONS
            case StateActions.ACTION_ARM:
                boolean doArm = data.getBoolean(StateActions.EXTRA_ARM);
                boolean emergencyDisarm = data.getBoolean(StateActions.EXTRA_EMERGENCY_DISARM);
                CommonApiUtils.arm(this, doArm, emergencyDisarm, listener);
                break;

            case StateActions.ACTION_SET_VEHICLE_MODE:
                data.setClassLoader(VehicleMode.class.getClassLoader());
                VehicleMode newMode = data.getParcelable(StateActions.EXTRA_VEHICLE_MODE);
                CommonApiUtils.changeVehicleMode(this, newMode, listener);
                break;

            //CALIBRATION ACTIONS
            case CalibrationActions.ACTION_START_IMU_CALIBRATION:
                CommonApiUtils.startIMUCalibration(this, listener);
                break;

            case CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK:
                int imuAck = data.getInt(CalibrationActions.EXTRA_IMU_STEP);
                CommonApiUtils.sendIMUCalibrationAck(this, imuAck);
                break;

            case CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION:
                final boolean retryOnFailure = data.getBoolean(CalibrationActions.EXTRA_RETRY_ON_FAILURE, false);
                final boolean saveAutomatically = data.getBoolean(CalibrationActions.EXTRA_SAVE_AUTOMATICALLY, true);
                final int startDelay = data.getInt(CalibrationActions.EXTRA_START_DELAY, 0);
                CommonApiUtils.startMagnetometerCalibration(this, retryOnFailure, saveAutomatically, startDelay);
                break;

            case CalibrationActions.ACTION_CANCEL_MAGNETOMETER_CALIBRATION:
                CommonApiUtils.cancelMagnetometerCalibration(this);
                break;

            case CalibrationActions.ACTION_ACCEPT_MAGNETOMETER_CALIBRATION:
                CommonApiUtils.acceptMagnetometerCalibration(this);
                break;

            //************ Gimbal ACTIONS *************//
            case GimbalActions.ACTION_SET_GIMBAL_ORIENTATION:
                double pitch = data.getDouble(GimbalActions.GIMBAL_PITCH);
                double roll = data.getDouble(GimbalActions.GIMBAL_ROLL);
                double yaw = data.getDouble(GimbalActions.GIMBAL_YAW);
                MavLinkDoCmds.setGimbalOrientation(this, pitch, roll, yaw, listener);
                break;

            case GimbalActions.ACTION_RESET_GIMBAL_MOUNT_MODE:
                MavLinkDoCmds.resetROI(this, listener);
                break;

            case GimbalActions.ACTION_SET_GIMBAL_MOUNT_MODE:
                final int mountMode = data.getInt(GimbalActions.GIMBAL_MOUNT_MODE, MAV_MOUNT_MODE.MAV_MOUNT_MODE_MAVLINK_TARGETING);

                Parameter mountParam = getParameters().getParameter("MNT_MODE");
                if (mountParam == null) {
                    msg_mount_configure msg = new msg_mount_configure();
                    msg.target_system = getSysid();
                    msg.target_component = getCompid();
                    msg.mount_mode = (byte) mountMode;
                    msg.stab_pitch =  0;
                    msg.stab_roll =  0;
                    msg.stab_yaw =  0;
                    getMavClient().sendMavMessage(msg, listener);
                } else {
                    MavLinkParameters.sendParameter(this, "MNT_MODE", 1, mountMode);
                }
                break;

            default:
                CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                break;
        }
    }

    @Override
    public void onMavLinkMessageReceived(MAVLinkMessage message) {
        if (message.compid != AUTOPILOT_COMPONENT_ID
                && message.compid != ARTOO_COMPONENT_ID) {
            return;
        }

        if (getParameters().processMessage(message)) {
            return;
        }

        getWaypointManager().processMessage(message);
        getCalibrationSetup().processMessage(message);

        switch (message.msgid) {
            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                msg_heartbeat msg_heart = (msg_heartbeat) message;
                setType(msg_heart.type);
                checkIfFlying(msg_heart);
                processState(msg_heart);
                ApmModes newMode = ApmModes.getMode(msg_heart.custom_mode, getType());
                getState().setMode(newMode);
                onHeartbeat(msg_heart);
                break;

            case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
                // These are any warnings sent from APM:Copter with
                // gcs_send_text_P()
                // This includes important thing like arm fails, prearm fails, low
                // battery, etc.
                // also less important things like "erasing logs" and
                // "calibrating barometer"
                msg_statustext msg_statustext = (msg_statustext) message;
                processStatusText(msg_statustext);
                break;

            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
                msg_attitude m_att = (msg_attitude) message;
                getOrientation().setRollPitchYaw(m_att.roll * 180.0 / Math.PI,
                        m_att.pitch * 180.0 / Math.PI, m_att.yaw * 180.0 / Math.PI);
                break;

            case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
                msg_vfr_hud m_hud = (msg_vfr_hud) message;
                setAltitudeGroundAndAirSpeeds(m_hud.alt, m_hud.groundspeed, m_hud.airspeed, m_hud.climb);
                break;

            case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
                getMissionStats().setWpno(((msg_mission_current) message).seq);
                break;

            case msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED:
                getMissionStats().setLastReachedWaypointNumber(((msg_mission_item_reached) message).seq);
                break;

            case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
                msg_nav_controller_output m_nav = (msg_nav_controller_output) message;
                setDisttowpAndSpeedAltErrors(m_nav.wp_dist, m_nav.alt_error, m_nav.aspd_error);
                getNavigation().setNavPitchRollYaw(m_nav.nav_pitch, m_nav.nav_roll, m_nav.nav_bearing);
                break;

            case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
                msg_raw_imu msg_imu = (msg_raw_imu) message;
                getMagnetometer().newData(msg_imu);
                break;

            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
                getGps().setPosition(((msg_global_position_int) message).lat / 1E7,
                        ((msg_global_position_int) message).lon / 1E7);
                break;

            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
                msg_sys_status m_sys = (msg_sys_status) message;
                getBattery().setBatteryState(m_sys.voltage_battery / 1000.0,
                        m_sys.battery_remaining, m_sys.current_battery / 100.0);
                checkControlSensorsHealth(m_sys);
                break;

            case msg_radio.MAVLINK_MSG_ID_RADIO:
                msg_radio m_radio = (msg_radio) message;
                getRadio().setRadioState(m_radio.rxerrors, m_radio.fixed, m_radio.rssi,
                        m_radio.remrssi, m_radio.txbuf, m_radio.noise, m_radio.remnoise);
                break;

            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                msg_radio_status m_radio_status = (msg_radio_status) message;
                getRadio().setRadioState(m_radio_status.rxerrors, m_radio_status.fixed, m_radio_status.rssi,
                        m_radio_status.remrssi, m_radio_status.txbuf, m_radio_status.noise, m_radio_status.remnoise);
                break;

            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
                getGps().setGpsState(((msg_gps_raw_int) message).fix_type,
                        ((msg_gps_raw_int) message).satellites_visible, ((msg_gps_raw_int) message).eph);
                break;

            case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
                getRC().setRcInputValues((msg_rc_channels_raw) message);
                break;

            case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
                getRC().setRcOutputValues((msg_servo_output_raw) message);
                break;

            case msg_camera_feedback.MAVLINK_MSG_ID_CAMERA_FEEDBACK:
                getCamera().newImageLocation((msg_camera_feedback) message);
                break;

            case msg_mount_status.MAVLINK_MSG_ID_MOUNT_STATUS:
                getCamera().updateMountOrientation(((msg_mount_status) message));
                break;

            case msg_named_value_int.MAVLINK_MSG_ID_NAMED_VALUE_INT:
                processNamedValueInt((msg_named_value_int) message);
                break;

            //*************** Magnetometer calibration messages handling *************//
            case msg_mag_cal_progress.MAVLINK_MSG_ID_MAG_CAL_PROGRESS:
            case msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT:
                getMagnetometerCalibration().processCalibrationMessage(message);
                break;

            //*************** EKF State handling ******************//
            case msg_ekf_status_report.MAVLINK_MSG_ID_EKF_STATUS_REPORT:
                getState().setEkfStatus((msg_ekf_status_report) message);
                break;

            case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM:
                msg_mission_item missionItem = (msg_mission_item) message;
                if (missionItem.seq == Home.HOME_WAYPOINT_INDEX) {
                    getHome().setHome(missionItem);
                }
                break;

            default:
                break;
        }
    }

    private void checkControlSensorsHealth(msg_sys_status sysStatus) {
        boolean isRCFailsafe = (sysStatus.onboard_control_sensors_health & MAV_SYS_STATUS_SENSOR
                .MAV_SYS_STATUS_SENSOR_RC_RECEIVER) == 0;
        if (isRCFailsafe) {
            getState().parseAutopilotError("RC FAILSAFE");
        }
    }

    private void processNamedValueInt(msg_named_value_int message) {
        if (message == null)
            return;

        switch (message.getName()) {
            case "ARMMASK":
                //Give information about the vehicle's ability to arm successfully.
                final ApmModes vehicleMode = getState().getMode();
                if (ApmModes.isCopter(vehicleMode.getType())) {
                    final int value = message.value;
                    final boolean isReadyToArm = (value & (1 << vehicleMode.getNumber())) != 0;
                    final String armReadinessMsg = isReadyToArm ? "READY TO ARM" : "UNREADY FOR ARMING";
                    logMessage(MAV_SEVERITY.MAV_SEVERITY_NOTICE, armReadinessMsg);
                }
                break;
        }
    }

    private void checkIfFlying(msg_heartbeat msg_heart) {
        final short systemStatus = msg_heart.system_status;
        final boolean wasFlying = getState().isFlying();

        final boolean isFlying = systemStatus == MAV_STATE.MAV_STATE_ACTIVE
                || (wasFlying
                && (systemStatus == MAV_STATE.MAV_STATE_CRITICAL || systemStatus == MAV_STATE.MAV_STATE_EMERGENCY));

        getState().setIsFlying(isFlying);
    }

    private void processState(msg_heartbeat msg_heart) {
        checkArmState(msg_heart);
        checkFailsafe(msg_heart);
    }

    private void checkFailsafe(msg_heartbeat msg_heart) {
        boolean failsafe2 = msg_heart.system_status == MAV_STATE.MAV_STATE_CRITICAL
                || msg_heart.system_status == MAV_STATE.MAV_STATE_EMERGENCY;

        if (failsafe2) {
            getState().repeatWarning();
        }
    }

    private void checkArmState(msg_heartbeat msg_heart) {
        getState().setArmed(
                (msg_heart.base_mode & MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED);
    }

    private void processStatusText(msg_statustext statusText) {
        String message = statusText.getText();
        if (TextUtils.isEmpty(message))
            return;

        if (message.startsWith("ArduCopter") || message.startsWith("ArduPlane")
                || message.startsWith("ArduRover") || message.startsWith("Solo")
                || message.startsWith("APM:Copter") || message.startsWith("APM:Plane")
                || message.startsWith("APM:Rover")) {
            setFirmwareVersion(message);
        } else {
            //Try parsing as an error.
            if (!getState().parseAutopilotError(message)) {
                //Relay to the connected client.
                logMessage(statusText.severity, message);
            }
        }
    }
}
