package com.o3dr.services.android.lib.drone.action;

import android.os.Bundle;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.model.action.Action;
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


    /**
     * Perform a guided take off.
     * @param altitude altitude in meters
     */
    public static Action buildGuidedTakeoff(double altitude){
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        return new Action(ACTION_DO_GUIDED_TAKEOFF, params);
    }

    /**
     * Send a guided point to the connected drone.
     * @param point guided point location
     * @param force true to enable guided mode is required.
     */
    public static Action buildGuidedPointSender(LatLong point, boolean force){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_FORCE_GUIDED_POINT, force);
        params.putParcelable(EXTRA_GUIDED_POINT, point);
        return new Action(ACTION_SEND_GUIDED_POINT, params);
    }

    /**
     * Set the altitude for the guided point.
     * @param altitude altitude in meters
     */
    public static Action buildGuidedAltitudeSetter(double altitude){
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        return new Action(ACTION_SET_GUIDED_ALTITUDE, params);
    }
}
