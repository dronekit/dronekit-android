        // MESSAGE OPTICAL_FLOW_RAD PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Optical flow from an angular rate flow sensor (e.g. PX4FLOW or mouse sensor)
        */
        public class msg_optical_flow_rad extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_OPTICAL_FLOW_RAD = 106;
        public static final int MAVLINK_MSG_LENGTH = 44;
        private static final long serialVersionUID = MAVLINK_MSG_ID_OPTICAL_FLOW_RAD;
        
        
         	/**
        * Timestamp (microseconds, synced to UNIX time or since system boot)
        */
        public long time_usec;
         	/**
        * Integration time in microseconds. Divide integrated_x and integrated_y by the integration time to obtain average flow. The integration time also indicates the.
        */
        public int integration_time_us;
         	/**
        * Flow in radians around X axis (Sensor RH rotation about the X axis induces a positive flow. Sensor linear motion along the positive Y axis induces a negative flow.)
        */
        public float integrated_x;
         	/**
        * Flow in radians around Y axis (Sensor RH rotation about the Y axis induces a positive flow. Sensor linear motion along the positive X axis induces a positive flow.)
        */
        public float integrated_y;
         	/**
        * RH rotation around X axis (rad)
        */
        public float integrated_xgyro;
         	/**
        * RH rotation around Y axis (rad)
        */
        public float integrated_ygyro;
         	/**
        * RH rotation around Z axis (rad)
        */
        public float integrated_zgyro;
         	/**
        * Time in microseconds since the distance was sampled.
        */
        public int time_delta_distance_us;
         	/**
        * Distance to the center of the flow field in meters. Positive value (including zero): distance known. Negative value: Unknown distance.
        */
        public float distance;
         	/**
        * Temperature * 100 in centi-degrees Celsius
        */
        public short temperature;
         	/**
        * Sensor ID
        */
        public byte sensor_id;
         	/**
        * Optical flow quality / confidence. 0: no valid flow, 255: maximum quality
        */
        public byte quality;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_OPTICAL_FLOW_RAD;
        		packet.payload.putLong(time_usec);
        		packet.payload.putInt(integration_time_us);
        		packet.payload.putFloat(integrated_x);
        		packet.payload.putFloat(integrated_y);
        		packet.payload.putFloat(integrated_xgyro);
        		packet.payload.putFloat(integrated_ygyro);
        		packet.payload.putFloat(integrated_zgyro);
        		packet.payload.putInt(time_delta_distance_us);
        		packet.payload.putFloat(distance);
        		packet.payload.putShort(temperature);
        		packet.payload.putByte(sensor_id);
        		packet.payload.putByte(quality);
        
		return packet;
        }
        
        /**
        * Decode a optical_flow_rad message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_usec = payload.getLong();
        	    this.integration_time_us = payload.getInt();
        	    this.integrated_x = payload.getFloat();
        	    this.integrated_y = payload.getFloat();
        	    this.integrated_xgyro = payload.getFloat();
        	    this.integrated_ygyro = payload.getFloat();
        	    this.integrated_zgyro = payload.getFloat();
        	    this.time_delta_distance_us = payload.getInt();
        	    this.distance = payload.getFloat();
        	    this.temperature = payload.getShort();
        	    this.sensor_id = payload.getByte();
        	    this.quality = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_optical_flow_rad(){
    	msgid = MAVLINK_MSG_ID_OPTICAL_FLOW_RAD;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_optical_flow_rad(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_OPTICAL_FLOW_RAD;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "OPTICAL_FLOW_RAD");
        //Log.d("MAVLINK_MSG_ID_OPTICAL_FLOW_RAD", toString());
        }
        
                                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_OPTICAL_FLOW_RAD -"+" time_usec:"+time_usec+" integration_time_us:"+integration_time_us+" integrated_x:"+integrated_x+" integrated_y:"+integrated_y+" integrated_xgyro:"+integrated_xgyro+" integrated_ygyro:"+integrated_ygyro+" integrated_zgyro:"+integrated_zgyro+" time_delta_distance_us:"+time_delta_distance_us+" distance:"+distance+" temperature:"+temperature+" sensor_id:"+sensor_id+" quality:"+quality+"";
        }
        }
        