package org.droidplanner.services.android.core.MAVLink;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_TYPE;

import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.drone.DroneManager;

/**
 * Parse the received mavlink messages, and update the drone state appropriately.
 */
public class MavLinkMsgHandler {

    public static final int AUTOPILOT_COMPONENT_ID = 1;

    private final DroneManager droneMgr;

    public MavLinkMsgHandler(DroneManager droneMgr) {
        this.droneMgr = droneMgr;
    }

    public void receiveData(MAVLinkMessage msg) {
        if (msg.compid != AUTOPILOT_COMPONENT_ID) {
            return;
        }

        switch (msg.msgid) {
            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                msg_heartbeat msg_heart = (msg_heartbeat) msg;
                handleHeartbeat(msg_heart);
                break;

            default:
                break;
        }
    }

    private void handleHeartbeat(msg_heartbeat heartbeat) {
        switch (heartbeat.autopilot) {
            case MAV_AUTOPILOT.MAV_AUTOPILOT_PIXHAWK:
            case MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA:
                switch (heartbeat.type) {

                    case MAV_TYPE.MAV_TYPE_FIXED_WING:
                        droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_PLANE);
                        break;

                    case MAV_TYPE.MAV_TYPE_GENERIC:
                    case MAV_TYPE.MAV_TYPE_QUADROTOR:
                    case MAV_TYPE.MAV_TYPE_COAXIAL:
                    case MAV_TYPE.MAV_TYPE_HELICOPTER:
                    case MAV_TYPE.MAV_TYPE_HEXAROTOR:
                    case MAV_TYPE.MAV_TYPE_OCTOROTOR:
                    case MAV_TYPE.MAV_TYPE_TRICOPTER:
                        droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_COPTER);
                        break;

                    case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
                    case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
                        droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_ROVER);
                        break;
                }
                break;

            case MAV_AUTOPILOT.MAV_AUTOPILOT_PX4:
                droneMgr.onVehicleTypeReceived(FirmwareType.PX4_NATIVE);
                break;
        }

    }
}
