package com.o3dr.services.android.lib.drone.companion.solo;

/**
 * Stores all possible drone events.
 * Created by Fredia Huya-Kouadio on 7/31/15.
 */
public class SoloEvents {

    //Private to prevent instantiation
    private SoloEvents() {
    }

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.drone.companion.solo.event";

    /**
     * Broadcasts updates to the GoPro state.
     *
     * @see {@link com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproState}
     */
    public static final String SOLO_GOPRO_STATE_UPDATED = PACKAGE_NAME + ".GOPRO_STATE_UPDATED";
    /**
     * Signals update to the sololink wifi settings
     *
     * @see {@link SoloState}
     */
    public static final String SOLO_WIFI_SETTINGS_UPDATED = PACKAGE_NAME + ".SOLO_WIFI_SETTINGS_UPDATED";
    /**
     * Signals update to the sololink button settings
     *
     * @see {@link SoloState}
     */
    public static final String SOLO_BUTTON_SETTINGS_UPDATED = PACKAGE_NAME + ".SOLO_BUTTON_SETTINGS_UPDATED";
    /**
     * Triggers every time a button event occurs.
     *
     * @see {@link SoloEventExtras#EXTRA_SOLOLINK_BUTTON_EVENT}
     */
    public static final String SOLO_BUTTON_EVENT = PACKAGE_NAME + ".SOLO_BUTTON_EVENT";
    /**
     * Triggers upon receipt of a sololink message.
     *
     * @see {@link SoloEventExtras#EXTRA_SOLOLINK_MESSAGE_DATA}
     */
    public static final String SOLO_MESSAGE_RECEIVED = PACKAGE_NAME + ".SOLO_MESSAGE_RECEIVED";
    /**
     * Triggers upon updates to the EU tx power compliance.
     */
    public static final String SOLO_EU_TX_POWER_COMPLIANCE_UPDATED = PACKAGE_NAME + ".SOLO_EU_TX_POWER_COMPLIANCE_UPDATED";
}
