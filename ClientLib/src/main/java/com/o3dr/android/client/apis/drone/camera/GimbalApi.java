package com.o3dr.android.client.apis.drone.camera;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.action.Action;
import static com.o3dr.services.android.lib.drone.action.GimbalActions.*;
public class GimbalApi {

    public static boolean setGimbalOrientation(Drone drone, double pitch, double roll, double yaw){
        Bundle params = new Bundle();
        params.putDouble(GIMBAL_PITCH, pitch);
        params.putDouble(GIMBAL_ROLL, roll);
        params.putDouble(GIMBAL_YAW, yaw);
        return drone != null && drone.performAsyncAction(new Action(ACTION_SET_GIMBAL_ORIENTATION, params));
    }
}
