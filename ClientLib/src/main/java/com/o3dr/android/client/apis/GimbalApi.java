package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.o3dr.services.android.lib.drone.action.GimbalActions.*;

public final class GimbalApi implements Api, DroneListener {

    private static final ConcurrentHashMap<Drone, GimbalApi> gimbalApiCache = new ConcurrentHashMap<>();

    public static GimbalApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, gimbalApiCache, new Builder<GimbalApi>() {
            @Override
            public GimbalApi build() {
                return new GimbalApi(drone);
            }
        });
    }

    private interface GimbalStatusListener {
        /**
         * Called when the gimbal orientation is updated.
         * @param pitch gimbal pitch angle in degree
         * @param roll gimbal roll angle in degree
         * @param yaw gimbal yaw angle in degree
         */
        void onGimbalOrientationUpdate(float pitch, float roll, float yaw);

        /**
         * Indicates errors occurring from attempting to set the gimbal orientation.
         * @param error @see {@link com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError}
         */
        void onGimbalOrientationCommandError(int error);
    }

    private final ConcurrentLinkedQueue<GimbalStatusListener> gimbalListeners = new ConcurrentLinkedQueue<>();

    private final Drone drone;

    private GimbalApi(Drone drone){
        this.drone = drone;
        this.drone.registerDroneListener(this);
    }

    /**
     * Enables control of the gimbal. After calling this method, use {@link GimbalApi#updateGimbalOrientation(double, double, double, com.o3dr.android.client.apis.GimbalApi.GimbalStatusListener)}
     * to update the gimbal orientation.
     * @param listener non-null GimbalStatusListener callback.
     */
    public void startGimbalControl(final GimbalStatusListener listener){
        if(listener == null)
            throw new NullPointerException("Listener can't be null.");

        final Type vehicleType = drone.getAttribute(AttributeType.TYPE);
        if(vehicleType.getDroneType() != Type.TYPE_COPTER){
            drone.post(new Runnable() {
                @Override
                public void run() {
                    listener.onGimbalOrientationCommandError(CommandExecutionError.COMMAND_UNSUPPORTED);
                }
            });
            return;
        }

        gimbalListeners.add(listener);

        //TODO: retrieve the current mount mode (for AC < 3.3)
    }

    /**
     * Disables control of the gimbal. After calling this method, no call to {@link GimbalApi#updateGimbalOrientation(double, double, double, com.o3dr.android.client.apis.GimbalApi.GimbalStatusListener)}
     * will be allowed.
     * @param listener non-null GimbalStatusListener callback.
     */
    public void stopGimbalControl(GimbalStatusListener listener){
        if(listener == null)
            throw new NullPointerException("Listener can't be null.");

        //TODO: reset the gimbal mount to the default.
    }

    /**
     * Set the orientation of a gimbal
     *
     * @param pitch       the desired gimbal pitch in degrees
     * @param roll       the desired gimbal roll in degrees
     * @param yaw       the desired gimbal yaw in degrees
     * @param listener Register a callback to receive update of the command execution state. Must be non-null.
     */
    public void updateGimbalOrientation(double pitch, double roll, double yaw, final GimbalStatusListener listener){
        if(listener == null)
            throw new NullPointerException("Listener must be non-null.");

        if(!gimbalListeners.contains(listener)){
            drone.post(new Runnable() {
                @Override
                public void run() {
                    listener.onGimbalOrientationCommandError(CommandExecutionError.COMMAND_DENIED);
                }
            });
            return;
        }

        Bundle params = new Bundle();
        params.putDouble(GIMBAL_PITCH, pitch);
        params.putDouble(GIMBAL_ROLL, roll);
        params.putDouble(GIMBAL_YAW, yaw);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_GIMBAL_ORIENTATION, params), new SimpleCommandListener(){
            @Override
            public void onTimeout(){
                listener.onGimbalOrientationCommandError(CommandExecutionError.COMMAND_FAILED);
            }

            @Override
            public void onError(int error){
                listener.onGimbalOrientationCommandError(error);
            }
        });
    }

    private void notifyGimbalOrientationUpdated(float pitch, float roll, float yaw){
        if(gimbalListeners.isEmpty())
            return;

        for(GimbalStatusListener listener: gimbalListeners){
            listener.onGimbalOrientationUpdate(pitch, roll, yaw);
        }
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch(event){
            case AttributeEvent.GIMBAL_ORIENTATION_UPDATED:
                final float pitch = extras.getFloat(AttributeEventExtra.EXTRA_GIMBAL_ORIENTATION_PITCH);
                final float roll = extras.getFloat(AttributeEventExtra.EXTRA_GIMBAL_ORIENTATION_ROLL);
                final float yaw = extras.getFloat(AttributeEventExtra.EXTRA_GIMBAL_ORIENTATION_YAW);
                notifyGimbalOrientationUpdated(pitch, roll, yaw);
                break;
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }
}
