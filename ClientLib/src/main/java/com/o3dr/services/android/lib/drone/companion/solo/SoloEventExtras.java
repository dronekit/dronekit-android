package com.o3dr.services.android.lib.drone.companion.solo;

import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;

/**
 * Holds handles used to retrieve additional information broadcast along a drone event.
 * Created by Fredia Huya-Kouadio on 7/31/15.
 */
public class SoloEventExtras {

    //Private to prevent instantiation
    private SoloEventExtras(){}

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.drone.companion.solo.event.extra";

    /**
     * Used to retrieve the {@link com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket} object describing the button event.
     */
    public static final String EXTRA_SOLOLINK_BUTTON_EVENT = PACKAGE_NAME + ".EXTRA_SOLOLINK_BUTTON_EVENT";
    /**
     * Used to retrieve the received sololink message data in bytes.
     */
    public static final String EXTRA_SOLOLINK_MESSAGE_DATA = PACKAGE_NAME + ".EXTRA_SOLOLINK_MESSAGE_DATA";
    /**
     * Used to retrieve the boolean value specifying whether the controller is compliant with the EU tx power levels.
     * @see {@link SoloEvents#SOLO_EU_TX_POWER_COMPLIANCE_UPDATED}
     */
    public static final String EXTRA_SOLOLINK_EU_TX_POWER_COMPLIANT = PACKAGE_NAME + ".EXTRA_SOLOLINK_EU_TX_POWER_COMPLIANT";
}
