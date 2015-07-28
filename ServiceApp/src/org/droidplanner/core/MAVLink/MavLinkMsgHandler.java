package org.droidplanner.core.MAVLink;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.enums.MAV_TYPE;

import org.droidplanner.core.firmware.FirmwareType;
import org.droidplanner.services.android.drone.DroneManager;

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
                handleHeartbeatType(msg_heart.type);
                break;

            case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
                msg_statustext msg_statustext = (msg_statustext) msg;
                handleStatusText(msg_statustext.getText());
                break;
            default:
                break;
        }
    }

    private void handleHeartbeatType(int type) {
        switch (type) {

            case MAV_TYPE.MAV_TYPE_FIXED_WING:
                droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_PLANE);
                break;

            case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
            case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
                droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_ROVER);
                break;
        }
    }

    private void handleStatusText(String message) {
        if (message.startsWith("ArduCopter") || message.startsWith("APM:Copter")) {
            droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_COPTER);
        } else if (message.startsWith("ArduPlane") || message.startsWith("APM:Plane")) {
            droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_PLANE);
        } else if (message.startsWith("Solo")) {
            droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_SOLO);
        } else if (message.startsWith("ArduRover") || message.startsWith("APM:Rover")) {
            droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_ROVER);
        }
    }
}
