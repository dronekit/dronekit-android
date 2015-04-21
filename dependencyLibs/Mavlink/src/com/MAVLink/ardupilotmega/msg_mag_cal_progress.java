// MESSAGE MAG_CAL_PROGRESS PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Reports progress of compass calibration.
*/
public class msg_mag_cal_progress extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_MAG_CAL_PROGRESS = 191;
    public static final int MAVLINK_MSG_LENGTH = 27;
    private static final long serialVersionUID = MAVLINK_MSG_ID_MAG_CAL_PROGRESS;


     	
    /**
    * Body frame direction vector for display
    */
    public float direction_x;
     	
    /**
    * Body frame direction vector for display
    */
    public float direction_y;
     	
    /**
    * Body frame direction vector for display
    */
    public float direction_z;
     	
    /**
    * Compass being calibrated
    */
    public byte compass_id;
     	
    /**
    * Bitmask of compasses being calibrated
    */
    public byte cal_mask;
     	
    /**
    * Status (see MAG_CAL_STATUS enum)
    */
    public byte cal_status;
     	
    /**
    * Attempt number
    */
    public byte attempt;
     	
    /**
    * Completion percentage
    */
    public byte completion_pct;
     	
    /**
    * Bitmask of sphere sections (see http://en.wikipedia.org/wiki/Geodesic_grid)
    */
    public byte completion_mask[] = new byte[10];
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_MAG_CAL_PROGRESS;
        		packet.payload.putFloat(direction_x);
        		packet.payload.putFloat(direction_y);
        		packet.payload.putFloat(direction_z);
        		packet.payload.putByte(compass_id);
        		packet.payload.putByte(cal_mask);
        		packet.payload.putByte(cal_status);
        		packet.payload.putByte(attempt);
        		packet.payload.putByte(completion_pct);
        		
        for (int i = 0; i < completion_mask.length; i++) {
            packet.payload.putByte(completion_mask[i]);
        }
                    
        
        return packet;
    }

    /**
    * Decode a mag_cal_progress message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    
        this.direction_x = payload.getFloat();
        	    
        this.direction_y = payload.getFloat();
        	    
        this.direction_z = payload.getFloat();
        	    
        this.compass_id = payload.getByte();
        	    
        this.cal_mask = payload.getByte();
        	    
        this.cal_status = payload.getByte();
        	    
        this.attempt = payload.getByte();
        	    
        this.completion_pct = payload.getByte();
        	    
         
        for (int i = 0; i < this.completion_mask.length; i++) {
            this.completion_mask[i] = payload.getByte();
        }
                
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_mag_cal_progress(){
        msgid = MAVLINK_MSG_ID_MAG_CAL_PROGRESS;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_mag_cal_progress(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MAG_CAL_PROGRESS;
        unpack(mavLinkPacket.payload);        
    }

                      
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_MAG_CAL_PROGRESS -"+" direction_x:"+direction_x+" direction_y:"+direction_y+" direction_z:"+direction_z+" compass_id:"+compass_id+" cal_mask:"+cal_mask+" cal_status:"+cal_status+" attempt:"+attempt+" completion_pct:"+completion_pct+" completion_mask:"+completion_mask+"";
    }
}
        