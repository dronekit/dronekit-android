package com.o3dr.services.android.lib.drone.companion.solo.action;

import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode;

/**
 * Created by Fredia Huya-Kouadio on 7/31/15.
 */
public class SoloConfigActions {

    //Private to prevent instantiation
    private SoloConfigActions() {
    }

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.drone.companion.solo.action.config";

    public static final String ACTION_UPDATE_WIFI_SETTINGS = PACKAGE_NAME + ".UPDATE_WIFI_SETTINGS";
    public static final String EXTRA_WIFI_SSID = "extra_wifi_ssid";
    public static final String EXTRA_WIFI_PASSWORD = "extra_wifi_password";


    public static final String ACTION_UPDATE_BUTTON_SETTINGS = PACKAGE_NAME + ".UPDATE_BUTTON_SETTINGS";
    /**
     * Used to retrieve the button settings to push to the sololink companion computer.
     *
     * @see {@link com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter}
     */
    public static final String EXTRA_BUTTON_SETTINGS = "extra_button_settings";


    public static final String ACTION_UPDATE_CONTROLLER_MODE = PACKAGE_NAME + ".UPDATE_CONTROLLER_MODE";
    /**
     * Controller mode to apply.
     *
     * @see {@link com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode.ControllerMode}
     */
    public static final String EXTRA_CONTROLLER_MODE = "extra_controller_mode";


    public static final String ACTION_UPDATE_EU_TX_POWER_COMPLIANCE = PACKAGE_NAME + ".UPDATE_EU_TX_POWER_COMPLIANCE";
    /**
     * Boolean value. true if the controller should be made compliant, false otherwise.
     */
    public static final String EXTRA_EU_TX_POWER_COMPLIANT = "extra_eu_tx_power_compliant";

    public static final String ACTION_REFRESH_SOLO_VERSIONS = PACKAGE_NAME + ".REFRESH_SOLO_VERSIONS";

    public static final String ACTION_UPDATE_CONTROLLER_UNIT = PACKAGE_NAME + ".UPDATE_CONTROLLER_UNIT";

    /**
     * Controller unit system to apply.
     * @see {@link com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits.ControllerUnit}
     */
    public static final String EXTRA_CONTROLLER_UNIT = "extra_controller_unit";

}
