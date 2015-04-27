// MESSAGE AUTOPILOT_VERSION_REQUEST PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Request the autopilot version from the system/component.
*/
public class msg_autopilot_version_request extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_AUTOPILOT_VERSION_REQUEST = 183;
    public static final int MAVLINK_MSG_LENGTH = 2;
    private static final long serialVersionUID = MAVLINK_MSG_ID_AUTOPILOT_VERSION_REQUEST;


     	
    /**
    * System ID
    */
    public byte target_system;
     	
    /**
    * Component ID
    */
    public byte target_component;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_AUTOPILOT_VERSION_REQUEST;
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        
        return packet;
    }

    /**
    * Decode a autopilot_version_request message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.target_system = payload.getByte();
        	    
        this.target_component = payload.getByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_autopilot_version_request(){
        msgid = MAVLINK_MSG_ID_AUTOPILOT_VERSION_REQUEST;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_autopilot_version_request(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_AUTOPILOT_VERSION_REQUEST;
        unpack(mavLinkPacket.payload);        
    }

        
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_AUTOPILOT_VERSION_REQUEST -"+" target_system:"+target_system+" target_component:"+target_component+"";
    }
}
        