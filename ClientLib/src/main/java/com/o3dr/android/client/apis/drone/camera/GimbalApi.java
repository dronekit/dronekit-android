package com.o3dr.android.client.apis.drone.camera;

import android.os.Bundle;

import com.MAVLink.enums.MAV_MOUNT_MODE;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.action.Action;
import static com.o3dr.services.android.lib.drone.action.GimbalActions.*;
public class GimbalApi {

    /**
     * Set the mount_mode of the gimbal.  This decides how the gimbal decides where to point.
     * Should it listen to RC input, should it point at an ROI point, or should it point to
     * a MAVLink direction.
     *
     * @param drone
     * @param mountMode What mode should the gimbal be in?  See MAV_MOUNT_MODE
     * @param stabilizePitch Leave these false unless the gimbal is a servo gimbal.
     * @param stabilizeRoll
     * @param stabilizeYaw
     * @return
     */
    public static boolean configureGimbal(Drone drone, int mountMode, boolean stabilizePitch,
                                          boolean stabilizeRoll, boolean stabilizeYaw){
        Bundle params = new Bundle();
        params.putInt(MOUNT_MODE, mountMode);
        params.putBoolean(GIMBAL_PITCH, stabilizePitch);
        params.putBoolean(GIMBAL_ROLL, stabilizeRoll);
        params.putBoolean(GIMBAL_YAW, stabilizeYaw);
        return drone != null && drone.performAsyncAction(new Action(ACTION_CONFIGURE_GIMBAL, params));
    }

    /**
     * Command the gimbal to point at a certain rotation.
     *
     * Note: This function will only work if the vehicle is configured to listen.  If it isn't,
     * try sending a `configureGimbal(getDrone(),MAV_MOUNT_MODE.MAV_MOUNT_MODE_MAVLINK_TARGETING,
     * false,false,false);` first.
     *
     * @param drone
     * @param pitch 0 is straight forwards, -9000 is straight down.
     * @param roll
     * @param yaw
     * @return
     */
    public static boolean setGimbalOrientation(Drone drone, double pitch, double roll, double yaw){
        Bundle params = new Bundle();
        params.putDouble(GIMBAL_PITCH, pitch);
        params.putDouble(GIMBAL_ROLL, roll);
        params.putDouble(GIMBAL_YAW, yaw);
        return drone != null && drone.performAsyncAction(new Action(ACTION_SET_GIMBAL_ORIENTATION, params));
    }
}
