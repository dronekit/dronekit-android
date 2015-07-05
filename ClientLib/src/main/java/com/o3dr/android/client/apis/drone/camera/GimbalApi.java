package com.o3dr.android.client.apis.drone.camera;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;
import static com.o3dr.services.android.lib.drone.action.GimbalActions.*;

public class GimbalApi {

    /**
     * Set the orientation of a gimbal
     *
     * @param drone       target vehicle
     * @param pitch       the desired gimbal pitch in degrees
     * @param roll       the desired gimbal roll in degrees
     * @param yaw       the desired gimbal yaw in degrees
     * @param listener Register a callback to receive update of the command execution state.
     */
    public static void setGimbalOrientation(Drone drone, double pitch, double roll, double yaw,
                                               AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putDouble(GIMBAL_PITCH, pitch);
        params.putDouble(GIMBAL_ROLL, roll);
        params.putDouble(GIMBAL_YAW, yaw);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_GIMBAL_ORIENTATION, params), listener);
    }
}
