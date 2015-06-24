package org.droidplanner.core.MAVLink;

import org.droidplanner.core.model.Drone;

import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;

public class MavLinkArm {

	public static void sendArmMessage(Drone drone, boolean arm, boolean emergencyDisarm) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();

		msg.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM;
		msg.param1 = arm ? 1 : 0;
		msg.param2 = emergencyDisarm ? 21196 : 0;//21196 is a magic number to disarm even if flying
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

}
