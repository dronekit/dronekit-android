package com.o3dr.services.android.lib.drone.companion.solo.tlv;

/**
 * All sololink messages types.
 */
public class TLVMessageTypes {

    public static final int TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT = 0;
    public static final int TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT = 1;
    public static final int TYPE_SOLO_MESSAGE_LOCATION = 2;
    public static final int TYPE_SOLO_MESSAGE_RECORD_POSITION = 3;
    public static final int TYPE_SOLO_CABLE_CAM_OPTIONS = 4;
    public static final int TYPE_SOLO_GET_BUTTON_SETTING = 5;
    public static final int TYPE_SOLO_SET_BUTTON_SETTING = 6;

    public static final int TYPE_SOLO_FOLLOW_OPTIONS = 19;
    public static final int TYPE_SOLO_SHOT_OPTIONS = 20;
    public static final int TYPE_SOLO_SHOT_ERROR = 21;

    public static final int TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR = 1000;
    public static final int TYPE_SOLO_CABLE_CAM_WAYPOINT = 1001;

    public static final int TYPE_ARTOO_INPUT_REPORT_MESSAGE = 2003;

    public static final int TYPE_SOLO_GOPRO_SET_REQUEST = 5001;
    public static final int TYPE_SOLO_GOPRO_RECORD = 5003;

    //Private constructor to prevent instantiation
    private TLVMessageTypes(){}
}
