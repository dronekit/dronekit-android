/** 
* 
*/
package com.MAVLink.enums;

public class GOPRO_HEARTBEAT_STATUS {
	public static final int GOPRO_HEARTBEAT_STATUS_DISCONNECTED = 0; /* No GoPro connected | */
	public static final int GOPRO_HEARTBEAT_STATUS_INCOMPATIBLE = 1; /* The detected GoPro is not HeroBus compatible | */
	public static final int GOPRO_HEARTBEAT_STATUS_CONNECTED = 2; /* A HeroBus compatible GoPro is connected | */
	public static final int GOPRO_HEARTBEAT_STATUS_RECORDING = 3; /* A HeroBus compatible GoPro is connected and recording | */
	public static final int GOPRO_HEARTBEAT_STATUS_ENUM_END = 4; /*  | */
}
            