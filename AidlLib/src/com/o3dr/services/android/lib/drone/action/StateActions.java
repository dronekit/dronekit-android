package com.o3dr.services.android.lib.drone.action;

import android.os.Bundle;

import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class StateActions {

    public static final String ACTION_ARM = Utils.PACKAGE_NAME + ".action.ARM";
    public static final String EXTRA_ARM = "extra_arm";

    public static final String ACTION_SET_VEHICLE_MODE = Utils.PACKAGE_NAME + ".action.SET_VEHICLE_MODE";
    public static final String EXTRA_VEHICLE_MODE = "extra_vehicle_mode";

    /**
     * Arm or disarm the connected drone.
     * @param arm true to arm, false to disarm.
     */
    public static Action buildArmingSetter(boolean arm){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_ARM, arm);
        return new Action(ACTION_ARM, params);
    }

    /**
     * Change the vehicle mode for the connected drone.
     * @param newMode new vehicle mode.
     */
    public static Action buildVehicleModeSetter(VehicleMode newMode){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_MODE, newMode);
        return new Action(ACTION_SET_VEHICLE_MODE, params);
    }
}
