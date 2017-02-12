package org.droidplanner.services.android.impl.msg;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;

/**
 * Created by kellys on 2/12/17.
 */
public class msg_do_change_speed extends MAVLinkMessage {
    public static final int MAVLINK_MSG = 178;
    public static final int MSG_LENGTH = 8;

    public short target_system;
    public short target_component;

    private short speed_type;
    private short target_speed;
    private short throttle_percent;

    public msg_do_change_speed(short speedType, short targetSpeed, short throttlePercent) {
        this.speed_type = speedType;
        this.target_speed = targetSpeed;
        this.throttle_percent = throttlePercent;
    }

    @Override
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket(MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG;
        packet.payload.putUnsignedByte(this.target_system);
        packet.payload.putUnsignedByte(this.target_component);
        packet.payload.putShort(this.speed_type);
        packet.payload.putShort(this.target_speed);
        packet.payload.putShort(this.throttle_percent);

        return packet;
    }

    @Override
    public void unpack(MAVLinkPayload msg) {
        msg.resetIndex();

        this.target_system = msg.getUnsignedByte();
        this.target_component = msg.getUnsignedByte();
        this.speed_type = msg.getShort();
        this.target_speed = msg.getShort();
        this.throttle_percent = msg.getShort();
    }
}
