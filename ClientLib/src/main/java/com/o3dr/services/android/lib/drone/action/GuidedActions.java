package com.o3dr.services.android.lib.drone.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class GuidedActions {

    public static final String ACTION_DO_GUIDED_TAKEOFF = Utils.PACKAGE_NAME + ".action.DO_GUIDED_TAKEOFF";
    public static final String EXTRA_ALTITUDE = "extra_altitude";

    public static final String ACTION_SEND_GUIDED_POINT = Utils.PACKAGE_NAME + ".action.SEND_GUIDED_POINT";
    public static final String EXTRA_GUIDED_POINT = "extra_guided_point";
    public static final String EXTRA_FORCE_GUIDED_POINT = "extra_force_guided_point";

    public static final String ACTION_SET_GUIDED_ALTITUDE = Utils.PACKAGE_NAME + ".action.SET_GUIDED_ALTITUDE";
}
