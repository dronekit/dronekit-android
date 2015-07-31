package com.o3dr.services.android.lib.drone.companion.solo;

/**
 * Stores the set of solo attribute types.
 * Created by Fredia Huya-Kouadio on 7/31/15.
 */
public class SoloAttributes {

    //Private to prevent instantiation
    private SoloAttributes(){}

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.drone.companion.solo.attribute";

    /**
     * Used to access the sololink state.
     */
    public static final String SOLO_STATE = PACKAGE_NAME + ".SOLO_STATE";

    /**
     * Used to access the sololink gopro state.
     */
    public static final String SOLO_GOPRO_STATE = PACKAGE_NAME + ".SOLO_GOPRO_STATE";
}
