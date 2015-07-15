package com.o3dr.services.android.lib.drone.companion.solo.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 7/10/15.
 */
public class SoloLinkActions {

    //Private to prevent instantiation
    private SoloLinkActions(){}

    public static final String ACTION_UPDATE_WIFI_SETTINGS = Utils.PACKAGE_NAME + ".action.UPDATE_WIFI_SETTINGS";

    public static final String EXTRA_WIFI_SSID = "extra_wifi_ssid";
    public static final String EXTRA_WIFI_PASSWORD = "extra_wifi_password";

    public static final String ACTION_UPDATE_BUTTON_SETTINGS = Utils.PACKAGE_NAME + ".action.UPDATE_BUTTON_SETTINGS";

    /**
     * Used to retrieve the button settings to push to the sololink companion computer.
     * @see {@link com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter}
     */
    public static final String EXTRA_BUTTON_SETTINGS = "extra_button_settings";

    public static final String ACTION_SEND_MESSAGE = Utils.PACKAGE_NAME + "sololink.action.SEND_MESSAGE";

    /**
     * TLV message object to send to the sololink companion computer.
     * @see {@link com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket}
     */
    public static final String EXTRA_MESSAGE_DATA = "extra_message_data";

    public static final String ACTION_UPDATE_CONTROLLER_MODE = Utils.PACKAGE_NAME + ".action.UPDATE_CONTROLLER_MODE";

    /**
     * Controller mode to apply.
     * @see {@link com.o3dr.services.android.lib.drone.companion.solo.SoloControllerMode}
     */
    public static final String EXTRA_CONTROLLER_MODE = "extra_controller_mode";

    public static final String ACTION_START_VIDEO_STREAM = Utils.PACKAGE_NAME + ".action.START_VIDEO_STREAM";

    public static final String EXTRA_VIDEO_DISPLAY = "extra_video_display";

    public static final String ACTION_STOP_VIDEO_STREAM = Utils.PACKAGE_NAME + ".action.STOP_VIDEO_STREAM";
}
