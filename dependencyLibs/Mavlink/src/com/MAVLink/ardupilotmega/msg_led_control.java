// MESSAGE LED_CONTROL PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Control vehicle LEDs
*/
public class msg_led_control extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_LED_CONTROL = 186;
    public static final int MAVLINK_MSG_LENGTH = 29;
    private static final long serialVersionUID = MAVLINK_MSG_ID_LED_CONTROL;


     	
    /**
    * System ID
    */
    public byte target_system;
     	
    /**
    * Component ID
    */
    public byte target_component;
     	
    /**
    * Instance (LED instance to control or 255 for all LEDs)
    */
    public byte instance;
     	
    /**
    * Pattern (see LED_PATTERN_ENUM)
    */
    public byte pattern;
     	
    /**
    * Custom Byte Length
    */
    public byte custom_len;
     	
    /**
    * Custom Bytes
    */
    public byte custom_bytes[] = new byte[24];
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_LED_CONTROL;
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		packet.payload.putByte(instance);
        		packet.payload.putByte(pattern);
        		packet.payload.putByte(custom_len);
        		
        for (int i = 0; i < custom_bytes.length; i++) {
            packet.payload.putByte(custom_bytes[i]);
        }
                    
        
        return packet;
    }

    /**
    * Decode a led_control message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.target_system = payload.getByte();
        	    
        this.target_component = payload.getByte();
        	    
        this.instance = payload.getByte();
        	    
        this.pattern = payload.getByte();
        	    
        this.custom_len = payload.getByte();
        	    
         
        for (int i = 0; i < this.custom_bytes.length; i++) {
            this.custom_bytes[i] = payload.getByte();
        }
                
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_led_control(){
        msgid = MAVLINK_MSG_ID_LED_CONTROL;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_led_control(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LED_CONTROL;
        unpack(mavLinkPacket.payload);        
    }

                
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_LED_CONTROL -"+" target_system:"+target_system+" target_component:"+target_component+" instance:"+instance+" pattern:"+pattern+" custom_len:"+custom_len+" custom_bytes:"+custom_bytes+"";
    }
}
        