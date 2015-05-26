package org.droidplanner.core.MAVLink;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.ardupilotmega.msg_gopro_get_response;
import com.MAVLink.ardupilotmega.msg_gopro_heartbeat;
import com.MAVLink.ardupilotmega.msg_gopro_set_response;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.ardupilotmega.msg_mount_status;
import com.MAVLink.ardupilotmega.msg_radio;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_mission_item;
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
import com.MAVLink.enums.MAV_SEVERITY;
import com.MAVLink.enums.MAV_STATE;
import com.MAVLink.enums.MAV_SYS_STATUS_SENSOR;

import org.droidplanner.core.drone.variables.Home;
import org.droidplanner.core.model.Drone;

/**
 * Parse the received mavlink messages, and update the drone state appropriately.
 */
public class MavLinkMsgHandler {

    private Drone drone;

    public MavLinkMsgHandler(Drone drone) {
        this.drone = drone;
    }

    public void receiveData(MAVLinkMessage msg) {
        if (drone.getParameters().processMessage(msg)) {
            return;
        }

        drone.getWaypointManager().processMessage(msg);
        drone.getCalibrationSetup().processMessage(msg);

        switch (msg.msgid) {
            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
                msg_attitude m_att = (msg_attitude) msg;
                drone.getOrientation().setRollPitchYaw(m_att.roll * 180.0 / Math.PI,
                        m_att.pitch * 180.0 / Math.PI, m_att.yaw * 180.0 / Math.PI);
                break;

            case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
                msg_vfr_hud m_hud = (msg_vfr_hud) msg;
                drone.setAltitudeGroundAndAirSpeeds(m_hud.alt, m_hud.groundspeed, m_hud.airspeed, m_hud.climb);
                break;

            case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
                drone.getMissionStats().setWpno(((msg_mission_current) msg).seq);
                break;

            case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
                msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
                drone.setDisttowpAndSpeedAltErrors(m_nav.wp_dist, m_nav.alt_error, m_nav.aspd_error);
                drone.getNavigation().setNavPitchRollYaw(m_nav.nav_pitch, m_nav.nav_roll, m_nav.nav_bearing);
                break;

            case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
                msg_raw_imu msg_imu = (msg_raw_imu) msg;
                drone.getMagnetometer().newData(msg_imu);
                break;

            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                msg_heartbeat msg_heart = (msg_heartbeat) msg;
                drone.setType(msg_heart.type);
                checkIfFlying(msg_heart);
                processState(msg_heart);
                ApmModes newMode = ApmModes.getMode(msg_heart.custom_mode, drone.getType());
                drone.getState().setMode(newMode);
                drone.onHeartbeat(msg_heart);
                break;

            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
                drone.getGps().setPosition(((msg_global_position_int) msg).lat / 1E7,
                                ((msg_global_position_int) msg).lon / 1E7);
                break;

            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
                msg_sys_status m_sys = (msg_sys_status) msg;
                drone.getBattery().setBatteryState(m_sys.voltage_battery / 1000.0,
                        m_sys.battery_remaining, m_sys.current_battery / 100.0);
                checkControlSensorsHealth(m_sys);
                break;

            case msg_radio.MAVLINK_MSG_ID_RADIO:
                msg_radio m_radio = (msg_radio) msg;
                drone.getRadio().setRadioState(m_radio.rxerrors, m_radio.fixed, m_radio.rssi,
                        m_radio.remrssi, m_radio.txbuf, m_radio.noise, m_radio.remnoise);
                break;

            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                msg_radio_status m_radio_status = (msg_radio_status) msg;
                drone.getRadio().setRadioState(m_radio_status.rxerrors, m_radio_status.fixed, m_radio_status.rssi,
                        m_radio_status.remrssi, m_radio_status.txbuf, m_radio_status.noise, m_radio_status.remnoise);
                break;

            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
                drone.getGps().setGpsState(((msg_gps_raw_int) msg).fix_type,
                        ((msg_gps_raw_int) msg).satellites_visible, ((msg_gps_raw_int) msg).eph);
                break;

            case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
                drone.getRC().setRcInputValues((msg_rc_channels_raw) msg);
                break;

            case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
                drone.getRC().setRcOutputValues((msg_servo_output_raw) msg);
                break;

            case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
                // These are any warnings sent from APM:Copter with
                // gcs_send_text_P()
                // This includes important thing like arm fails, prearm fails, low
                // battery, etc.
                // also less important things like "erasing logs" and
                // "calibrating barometer"
                msg_statustext msg_statustext = (msg_statustext) msg;
                processStatusText(msg_statustext);
                break;

            case msg_camera_feedback.MAVLINK_MSG_ID_CAMERA_FEEDBACK:
                drone.getCamera().newImageLocation((msg_camera_feedback) msg);
                break;

            case msg_mount_status.MAVLINK_MSG_ID_MOUNT_STATUS:
                drone.getCamera().updateMountOrientation(((msg_mount_status) msg));
                break;

            case msg_named_value_int.MAVLINK_MSG_ID_NAMED_VALUE_INT:
                processNamedValueInt((msg_named_value_int) msg);
                break;

            //*************** GoPro messages handling **************//
            case msg_gopro_heartbeat.MAVLINK_MSG_ID_GOPRO_HEARTBEAT:
                drone.getGoProImpl().onHeartBeat((msg_gopro_heartbeat) msg);
                break;

            case msg_gopro_set_response.MAVLINK_MSG_ID_GOPRO_SET_RESPONSE:
                drone.getGoProImpl().onResponseReceived((msg_gopro_set_response) msg);
                break;

            case msg_gopro_get_response.MAVLINK_MSG_ID_GOPRO_GET_RESPONSE:
                drone.getGoProImpl().onResponseReceived((msg_gopro_get_response)msg);
                break;

            //*************** Magnetometer calibration messages handling *************//
            case msg_mag_cal_progress.MAVLINK_MSG_ID_MAG_CAL_PROGRESS:
            case msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT:
                drone.getMagnetometerCalibration().processCalibrationMessage(msg);
                break;

            //*************** EKF State handling ******************//
            case msg_ekf_status_report.MAVLINK_MSG_ID_EKF_STATUS_REPORT:
                drone.getState().setEkfStatus((msg_ekf_status_report) msg);
                break;

            case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM:
                msg_mission_item missionItem = (msg_mission_item) msg;
                if(missionItem.seq == Home.HOME_WAYPOINT_INDEX){
                    drone.getHome().setHome(missionItem);
                }
                break;

            //**************** Command long acknowledgement ******************//
            case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
                final msg_command_ack commandAck = (msg_command_ack) msg;
                handleCommandAck(commandAck);
                break;

            default:
                break;
        }
    }

    private void handleCommandAck(msg_command_ack ack){
        if(ack != null){
            System.out.println(ack.toString());
        }
    }

    private void processNamedValueInt(msg_named_value_int message){
        if(message == null)
            return;

        switch (message.getName()) {
            case "ARMMASK":
                //Give information about the vehicle's ability to arm successfully.
                final ApmModes vehicleMode = drone.getState().getMode();
                if (ApmModes.isCopter(vehicleMode.getType())) {
                    final int value = message.value;
                    final boolean isReadyToArm = (value & (1 << vehicleMode.getNumber())) != 0;
                    final String armReadinessMsg = isReadyToArm ? "READY TO ARM" : "UNREADY FOR ARMING";
                    drone.logMessage(MAV_SEVERITY.MAV_SEVERITY_NOTICE, armReadinessMsg);
                }
                break;
        }
    }

    private void checkIfFlying(msg_heartbeat msg_heart) {
        final byte systemStatus = msg_heart.system_status;
        final boolean wasFlying = drone.getState().isFlying();

        final boolean isFlying = systemStatus == MAV_STATE.MAV_STATE_ACTIVE
                || (wasFlying
                && (systemStatus == MAV_STATE.MAV_STATE_CRITICAL || systemStatus == MAV_STATE.MAV_STATE_EMERGENCY));

        drone.getState().setIsFlying(isFlying);
    }

    private void processState(msg_heartbeat msg_heart) {
        checkArmState(msg_heart);
        checkFailsafe(msg_heart);
    }

    private void checkFailsafe(msg_heartbeat msg_heart) {
        boolean failsafe2 = msg_heart.system_status == (byte) MAV_STATE.MAV_STATE_CRITICAL
                || msg_heart.system_status == MAV_STATE.MAV_STATE_EMERGENCY;

        if (failsafe2) {
            drone.getState().repeatWarning();
        }
    }

    private void checkControlSensorsHealth(msg_sys_status sysStatus){
        boolean isRCFailsafe = (sysStatus.onboard_control_sensors_health & MAV_SYS_STATUS_SENSOR
                .MAV_SYS_STATUS_SENSOR_RC_RECEIVER) == 0;
        if(isRCFailsafe){
            drone.getState().parseAutopilotError("RC FAILSAFE");
        }
    }

    private void checkArmState(msg_heartbeat msg_heart) {
        drone.getState().setArmed(
                (msg_heart.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED);
    }

    private void processStatusText(msg_statustext statusText) {
        String message = statusText.getText();

        switch (message) {
            case "ArduCopter":
            case "ArduPlane":
            case "ArduRover":
                drone.setFirmwareVersion(message);
                break;

            default:
                //Try parsing as an error.
                if(!drone.getState().parseAutopilotError(message)) {
                    //Relay to the connected client.
                    drone.logMessage(statusText.severity, message);
                }
                break;
        }
    }
}
