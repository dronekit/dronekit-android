package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.MAVLink.enums.MAV_MOUNT_MODE;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.property.Type;
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

    public interface GimbalOrientationListener {
        /**
         * Called when the gimbal orientation is updated.
         * @param orientation GimbalOrientation object
         */
        void onGimbalOrientationUpdate(GimbalOrientation orientation);

        /**
         * Indicates errors occurring from attempting to set the gimbal orientation.
         * @param error @see {@link com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError}
         */
        void onGimbalOrientationCommandError(int error);
    }

    /**
     * Stores the gimbal orientation angles.
     */
    public static class GimbalOrientation {
        private float pitch;
        private float roll;
        private float yaw;

        public float getPitch() {
            return pitch;
        }

        public float getRoll() {
            return roll;
        }

        public float getYaw() {
            return yaw;
        }

        private void updateOrientation(float pitch, float roll, float yaw) {
            this.pitch = pitch;
            this.roll = roll;
            this.yaw = yaw;
        }
    }

    private final ConcurrentLinkedQueue<GimbalOrientationListener> gimbalListeners = new ConcurrentLinkedQueue<>();

    private final Drone drone;
    private final GimbalOrientation gimbalOrientation = new GimbalOrientation();

    private GimbalApi(Drone drone){
        this.drone = drone;
        this.drone.registerDroneListener(this);
    }

    public GimbalOrientation getGimbalOrientation(){
        return gimbalOrientation;
    }

    /**
     * Enables control of the gimbal. After calling this method, use {@link GimbalApi#updateGimbalOrientation(double, double, double, GimbalOrientationListener)}
     * to update the gimbal orientation.
     * @param listener non-null GimbalStatusListener callback.
     */
    public void startGimbalControl(final GimbalOrientationListener listener){
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

        configureGimbalMountMode(listener);
    }

    private void configureGimbalMountMode(final GimbalOrientationListener listener){
        Bundle params = new Bundle(1);
        params.putInt(GIMBAL_MOUNT_MODE, MAV_MOUNT_MODE.MAV_MOUNT_MODE_MAVLINK_TARGETING);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_GIMBAL_MOUNT_MODE, params), new SimpleCommandListener(){
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

    /**
     * Disables control of the gimbal. After calling this method, no call to {@link GimbalApi#updateGimbalOrientation(double, double, double, GimbalOrientationListener)}
     * will be allowed.
     * @param listener non-null GimbalStatusListener callback.
     */
    public void stopGimbalControl(final GimbalOrientationListener listener){
        if(listener == null)
            throw new NullPointerException("Listener can't be null.");

        if(!gimbalListeners.contains(listener)){
            drone.post(new Runnable() {
                @Override
                public void run() {
                    listener.onGimbalOrientationCommandError(CommandExecutionError.COMMAND_DENIED);
                }
            });
            return;
        }

        //Reset the gimbal mount to the default.
        drone.performAsyncActionOnDroneThread(new Action(ACTION_RESET_GIMBAL_MOUNT_MODE, null), new SimpleCommandListener(){
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

    /**
     * Set the orientation of a gimbal
     *
     * @param pitch       the desired gimbal pitch in degrees. 0 is straight forwards, -90 is straight down
     * @param roll       the desired gimbal roll in degrees
     * @param yaw       the desired gimbal yaw in degrees
     * @param listener Register a callback to receive update of the command execution state. Must be non-null.
     */
    public void updateGimbalOrientation(float pitch, float roll, float yaw, final GimbalOrientationListener listener){
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

    private void notifyGimbalOrientationUpdated(GimbalOrientation orientation){
        if(gimbalListeners.isEmpty())
            return;

        for(GimbalOrientationListener listener: gimbalListeners){
            listener.onGimbalOrientationUpdate(orientation);
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
                gimbalOrientation.updateOrientation(pitch, roll, yaw);
                notifyGimbalOrientationUpdated(gimbalOrientation);
                break;
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }
}
