// MESSAGE GOPRO_GET_RESPONSE PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Response from a GOPRO_COMMAND get request
*/
public class msg_gopro_get_response extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_GOPRO_GET_RESPONSE = 217;
    public static final int MAVLINK_MSG_LENGTH = 2;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GOPRO_GET_RESPONSE;


     	
    /**
    * Command ID
    */
    public byte cmd_id;
     	
    /**
    * Value
    */
    public byte value;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GOPRO_GET_RESPONSE;
        		packet.payload.putByte(cmd_id);
        		packet.payload.putByte(value);
        
        return packet;
    }

    /**
    * Decode a gopro_get_response message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.cmd_id = payload.getByte();
        	    
        this.value = payload.getByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_gopro_get_response(){
        msgid = MAVLINK_MSG_ID_GOPRO_GET_RESPONSE;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_gopro_get_response(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GOPRO_GET_RESPONSE;
        unpack(mavLinkPacket.payload);        
    }

        
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GOPRO_GET_RESPONSE -"+" cmd_id:"+cmd_id+" value:"+value+"";
    }
}
        