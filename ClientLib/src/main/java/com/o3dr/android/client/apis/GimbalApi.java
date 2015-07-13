package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.MAVLink.enums.MAV_MOUNT_MODE;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.GimbalActions.*;

public class GimbalApi implements Api {

    private static final ConcurrentHashMap<Drone, GimbalApi> gimbalApiCache = new ConcurrentHashMap<>();

    public static GimbalApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, gimbalApiCache, new Builder<GimbalApi>() {
            @Override
            public GimbalApi build() {
                return new GimbalApi(drone);
            }
        });
    }

    private final Drone drone;

    private GimbalApi(Drone drone){
        this.drone = drone;
    }

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
     * @param drone       target vehicle
     * @param pitch       the desired gimbal pitch in degrees. 0 is straight forwards, -9000 is straight down.
     * @param roll       the desired gimbal roll in degrees
     * @param yaw       the desired gimbal yaw in degrees
     * @param listener Register a callback to receive update of the command execution state.
     * @return
     */
    public static void setGimbalOrientation(Drone drone, double pitch, double roll, double yaw, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putDouble(GIMBAL_PITCH, pitch);
        params.putDouble(GIMBAL_ROLL, roll);
        params.putDouble(GIMBAL_YAW, yaw);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_GIMBAL_ORIENTATION, params), listener);
    }
}
