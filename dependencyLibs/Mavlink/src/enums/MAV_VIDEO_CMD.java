/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

package com.MAVLink.enums;

/** 
 * 
 */
public class MAV_VIDEO_CMD {
   public static final int MAV_CMD_REQUEST_CAMERA_INFORMATION = 521; /* WIP: Request camera information (CAMERA_INFORMATION). |0: No action 1: Request camera capabilities| Reserved (all remaining params)|  */
   public static final int MAV_CMD_REQUEST_CAMERA_SETTINGS = 522; /* WIP: Request camera settings (CAMERA_SETTINGS). |0: No Action 1: Request camera settings| Reserved (all remaining params)|  */
   public static final int MAV_CMD_REQUEST_STORAGE_INFORMATION = 525; /* WIP: Request storage information (STORAGE_INFORMATION). Use the command's target_component to target a specific component's storage. |Storage ID (0 for all, 1 for first, 2 for second, etc.)| 0: No Action 1: Request storage information| Reserved (all remaining params)|  */
   public static final int MAV_CMD_STORAGE_FORMAT = 526; /* WIP: Format a storage medium. Once format is complete, a STORAGE_INFORMATION message is sent. Use the command's target_component to target a specific component's storage. |Storage ID (1 for first, 2 for second, etc.)| 0: No action 1: Format storage| Reserved (all remaining params)|  */
   public static final int MAV_CMD_REQUEST_CAMERA_CAPTURE_STATUS = 527; /* WIP: Request camera capture status (CAMERA_CAPTURE_STATUS) |0: No Action 1: Request camera capture status| Reserved (all remaining params)|  */
   public static final int MAV_CMD_RESET_CAMERA_SETTINGS = 529; /* WIP: Reset all camera settings to Factory Default |0: No Action 1: Reset all settings| Reserved (all remaining params)|  */
   public static final int MAV_CMD_SET_CAMERA_MODE = 530; /* Set camera running mode. Use NAN for reserved values. |Reserved (Set to 0)| Camera mode (see CAMERA_MODE enum)| Reserved (all remaining params)|  */
   public static final int MAV_CMD_IMAGE_START_CAPTURE = 2000; /* Start image capture sequence. Sends CAMERA_IMAGE_CAPTURED after each capture. Use NAN for reserved values. |Reserved (Set to 0)| Duration between two consecutive pictures (in seconds)| Number of images to capture total - 0 for unlimited capture| Reserved (all remaining params)|  */
   public static final int MAV_CMD_IMAGE_STOP_CAPTURE = 2001; /* Stop image capture sequence Use NAN for reserved values. |Reserved (Set to 0)| Reserved (all remaining params)|  */
   public static final int MAV_CMD_REQUEST_CAMERA_IMAGE_CAPTURE = 2002; /* WIP: Re-request a CAMERA_IMAGE_CAPTURE packet. Use NAN for reserved values. |Sequence number for missing CAMERA_IMAGE_CAPTURE packet| Reserved (all remaining params)|  */
   public static final int MAV_CMD_VIDEO_START_STREAMING = 2502; /* Start video streaming |Video Stream ID (0 for all streams, 1 for first, 2 for second, etc.)| Reserved|  */
   public static final int MAV_CMD_VIDEO_STOP_STREAMING = 2503; /* Stop the given video stream |Video Stream ID (0 for all streams, 1 for first, 2 for second, etc.)| Reserved|  */
   public static final int MAV_CMD_REQUEST_VIDEO_STREAM_INFORMATION = 2504; /* Request video stream information (VIDEO_STREAM_INFORMATION) |Video Stream ID (0 for all streams, 1 for first, 2 for second, etc.)| Reserved (all remaining params)|  */
   public static final int MAV_CMD_REQUEST_VIDEO_STREAM_STATUS = 2505; /* Request video stream status (VIDEO_STREAM_STATUS) |Video Stream ID (0 for all streams, 1 for first, 2 for second, etc.)| Reserved (all remaining params)|  */
   public static final int MAV_VIDEO_CMD_ENUM_END = 2506; /*  | */
}
            