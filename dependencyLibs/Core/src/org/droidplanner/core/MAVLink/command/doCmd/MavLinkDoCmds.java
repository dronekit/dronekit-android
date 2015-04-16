package org.droidplanner.core.MAVLink.command.doCmd;

import com.MAVLink.ardupilotmega.msg_digicam_control;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.mission.commands.EpmGripper;
import org.droidplanner.core.model.Drone;

public class MavLinkDoCmds {
    public static void setROI(Drone drone, Coord3D coord) {
        if (drone == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_ROI;

        msg.param5 = (float) coord.getX();
        msg.param6 = (float) coord.getY();
        msg.param7 = (float) coord.getAltitude();

        drone.getMavClient().sendMavPacket(msg.pack());
    }

    public static void resetROI(Drone drone) {
        if (drone == null)
            return;

        setROI(drone, new Coord3D(0, 0, 0));
    }

    public static void triggerCamera(Drone drone) {
        if (drone == null)
            return;

        msg_digicam_control msg = new msg_digicam_control();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.shot = 1;
        drone.getMavClient().sendMavPacket(msg.pack());
    }

    public static void empCommand(Drone drone, boolean release) {
        if (drone == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = EpmGripper.MAV_CMD_DO_GRIPPER;
        msg.param2 = release ? EpmGripper.GRIPPER_ACTION_RELEASE : EpmGripper.GRIPPER_ACTION_GRAB;

        drone.getMavClient().sendMavPacket(msg.pack());
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param drone       target vehicle
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     */
    public static void setRelay(Drone drone, int relayNumber, boolean enabled) {
        if (drone == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_RELAY;
        msg.param1 = relayNumber;
        msg.param2 = enabled ? 1 : 0;

        drone.getMavClient().sendMavPacket(msg.pack());
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param drone   target vehicle
     * @param channel he output channel the servo is attached to
     * @param pwm     PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     */
    public static void setServo(Drone drone, int channel, int pwm) {
        if (drone == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_SERVO;
        msg.param1 = channel;
        msg.param2 = pwm;

        drone.getMavClient().sendMavPacket(msg.pack());
    }

}
