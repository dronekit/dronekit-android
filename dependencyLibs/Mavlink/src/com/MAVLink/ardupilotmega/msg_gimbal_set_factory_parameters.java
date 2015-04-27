// MESSAGE GIMBAL_SET_FACTORY_PARAMETERS PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* 
            Set factory configuration parameters (such as assembly date and time, and serial number).  This is only intended to be used
            during manufacture, not by end users, so it is protected by a simple checksum of sorts (this won't stop anybody determined,
            it's mostly just to keep the average user from trying to modify these values.  This will need to be revisited if that isn't
            adequate.
        
*/
public class msg_gimbal_set_factory_parameters extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_GIMBAL_SET_FACTORY_PARAMETERS = 206;
    public static final int MAVLINK_MSG_LENGTH = 33;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GIMBAL_SET_FACTORY_PARAMETERS;


     	
    /**
    * Magic number 1 for validation
    */
    public int magic_1;
     	
    /**
    * Magic number 2 for validation
    */
    public int magic_2;
     	
    /**
    * Magic number 3 for validation
    */
    public int magic_3;
     	
    /**
    * Unit Serial Number Part 1 (part code, design, language/country)
    */
    public int serial_number_pt_1;
     	
    /**
    * Unit Serial Number Part 2 (option, year, month)
    */
    public int serial_number_pt_2;
     	
    /**
    * Unit Serial Number Part 3 (incrementing serial number per month)
    */
    public int serial_number_pt_3;
     	
    /**
    * Assembly Date Year
    */
    public short assembly_year;
     	
    /**
    * System ID
    */
    public byte target_system;
     	
    /**
    * Component ID
    */
    public byte target_component;
     	
    /**
    * Assembly Date Month
    */
    public byte assembly_month;
     	
    /**
    * Assembly Date Day
    */
    public byte assembly_day;
     	
    /**
    * Assembly Time Hour
    */
    public byte assembly_hour;
     	
    /**
    * Assembly Time Minute
    */
    public byte assembly_minute;
     	
    /**
    * Assembly Time Second
    */
    public byte assembly_second;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GIMBAL_SET_FACTORY_PARAMETERS;
        		packet.payload.putInt(magic_1);
        		packet.payload.putInt(magic_2);
        		packet.payload.putInt(magic_3);
        		packet.payload.putInt(serial_number_pt_1);
        		packet.payload.putInt(serial_number_pt_2);
        		packet.payload.putInt(serial_number_pt_3);
        		packet.payload.putShort(assembly_year);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		packet.payload.putByte(assembly_month);
        		packet.payload.putByte(assembly_day);
        		packet.payload.putByte(assembly_hour);
        		packet.payload.putByte(assembly_minute);
        		packet.payload.putByte(assembly_second);
        
        return packet;
    }

    /**
    * Decode a gimbal_set_factory_parameters message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.magic_1 = payload.getInt();
        	    
        this.magic_2 = payload.getInt();
        	    
        this.magic_3 = payload.getInt();
        	    
        this.serial_number_pt_1 = payload.getInt();
        	    
        this.serial_number_pt_2 = payload.getInt();
        	    
        this.serial_number_pt_3 = payload.getInt();
        	    
        this.assembly_year = payload.getShort();
        	    
        this.target_system = payload.getByte();
        	    
        this.target_component = payload.getByte();
        	    
        this.assembly_month = payload.getByte();
        	    
        this.assembly_day = payload.getByte();
        	    
        this.assembly_hour = payload.getByte();
        	    
        this.assembly_minute = payload.getByte();
        	    
        this.assembly_second = payload.getByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_gimbal_set_factory_parameters(){
        msgid = MAVLINK_MSG_ID_GIMBAL_SET_FACTORY_PARAMETERS;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_gimbal_set_factory_parameters(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GIMBAL_SET_FACTORY_PARAMETERS;
        unpack(mavLinkPacket.payload);        
    }

                                
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GIMBAL_SET_FACTORY_PARAMETERS -"+" magic_1:"+magic_1+" magic_2:"+magic_2+" magic_3:"+magic_3+" serial_number_pt_1:"+serial_number_pt_1+" serial_number_pt_2:"+serial_number_pt_2+" serial_number_pt_3:"+serial_number_pt_3+" assembly_year:"+assembly_year+" target_system:"+target_system+" target_component:"+target_component+" assembly_month:"+assembly_month+" assembly_day:"+assembly_day+" assembly_hour:"+assembly_hour+" assembly_minute:"+assembly_minute+" assembly_second:"+assembly_second+"";
    }
}
        