package com.o3dr.android.client.apis.drone;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_ARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_SET_VEHICLE_MODE;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_ARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_VEHICLE_MODE;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class DroneStateApi {

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm true to arm, false to disarm.
     */
    public static void arm(Drone drone, boolean arm) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_ARM, arm);
        drone.performAsyncAction(new Action(ACTION_ARM, params));
    }

    /**
     * Change the vehicle mode for the connected drone.
     *
     * @param newMode new vehicle mode.
     */
    public static void setVehicleMode(Drone drone, VehicleMode newMode) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_MODE, newMode);
        drone.performAsyncAction(new Action(ACTION_SET_VEHICLE_MODE, params));
    }
}
