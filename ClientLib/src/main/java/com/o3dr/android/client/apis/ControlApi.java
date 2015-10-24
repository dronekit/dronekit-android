package com.o3dr.android.client.apis;

import android.os.Bundle;
import android.support.annotation.IntDef;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_DO_GUIDED_TAKEOFF;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SEND_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SET_CONDITION_YAW;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SET_GUIDED_ALTITUDE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SET_VELOCITY;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_ALTITUDE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_FORCE_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_X;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_Y;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_VELOCITY_Z;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_CHANGE_RATE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_IS_RELATIVE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_TARGET_ANGLE;

/**
 * Provides access to the vehicle control functionality.
 *
 * Use of this api might required the vehicle to be in a specific flight mode (i.e: GUIDED)
 *
 * Created by Fredia Huya-Kouadio on 9/7/15.
 */
public class ControlApi extends Api {

    public static final int EARTH_NED_COORDINATE_FRAME = 0;
    public static final int VEHICLE_COORDINATE_FRAME = 1;

    @IntDef({EARTH_NED_COORDINATE_FRAME, VEHICLE_COORDINATE_FRAME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CoordinateFrame{}

    private static final ConcurrentHashMap<Drone, ControlApi> apiCache = new ConcurrentHashMap<>();
    private static final Builder<ControlApi> apiBuilder = new Builder<ControlApi>() {
        @Override
        public ControlApi build(Drone drone) {
            return new ControlApi(drone);
        }
    };

    /**
     * Retrieves a control api instance.
     * @param drone
     * @return
     */
    public static ControlApi getApi(final Drone drone){
        return getApi(drone, apiCache, apiBuilder);
    }

    private final Drone drone;

    private ControlApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Perform a guided take off.
     *
     * @param altitude altitude in meters
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void takeoff(double altitude, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_DO_GUIDED_TAKEOFF, params), listener);
    }

    /**
     * Pause the vehicle at its current location.
     *
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void pauseAtCurrentLocation(final AbstractCommandListener listener) {
        drone.getAttributeAsync(AttributeType.GPS, new Drone.AttributeRetrievedListener<Gps>() {
            @Override
            public void onRetrievalSucceed(Gps gps) {
                goTo(gps.getPosition(), true, listener);
            }
        });
    }

    /**
     * Instructs the vehicle to go to the specified location.
     *
     * @param point    target location
     * @param force    true to enable guided mode is required.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void goTo(LatLong point, boolean force, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_FORCE_GUIDED_POINT, force);
        params.putParcelable(EXTRA_GUIDED_POINT, point);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SEND_GUIDED_POINT, params), listener);
    }

    /**
     * Instructs the vehicle to climb to the specified altitude.
     *
     * @param altitude altitude in meters
     */
    public void climbTo(double altitude) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        drone.performAsyncAction(new Action(ACTION_SET_GUIDED_ALTITUDE, params));
    }

    /**
     * Instructs the vehicle to turn to the specified target angle
     * @param targetAngle Target angle in degrees [0-360], with 0 == north.
     * @param turnRate Turning rate normalized to the range [-1.0f, 1.0f]. Positive values for clockwise turns, and negative values for counter-clockwise turns.
     * @param isRelative True is the target angle is relative to the current vehicle attitude, false otherwise if it's absolute.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void turnTo(float targetAngle, float turnRate, boolean isRelative, AbstractCommandListener listener){
        if(!isWithinBounds(targetAngle, 0, 360) || !isWithinBounds(turnRate, -1.0f, 1.0f)){
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        Bundle params = new Bundle();
        params.putFloat(EXTRA_YAW_TARGET_ANGLE, targetAngle);
        params.putFloat(EXTRA_YAW_CHANGE_RATE, turnRate);
        params.putBoolean(EXTRA_YAW_IS_RELATIVE, isRelative);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_CONDITION_YAW, params), listener);
    }

    private void moveAtVelocity(float vx, float vy, float vz, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putFloat(EXTRA_VELOCITY_X, vx);
        params.putFloat(EXTRA_VELOCITY_Y, vy);
        params.putFloat(EXTRA_VELOCITY_Z, vz);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VELOCITY, params), listener);
    }

    /**
     * Move the vehicle along the specified velocity vector.
     *
     * @param referenceFrame Reference frame to use. Can be one of
     *                       {@link #EARTH_NED_COORDINATE_FRAME},
     *                       {@link #VEHICLE_COORDINATE_FRAME}
     *
     * @param vx             x velocity normalized to the range [-1.0f, 1.0f].
     * @param vy             y velocity normalized to the range [-1.0f, 1.0f].
     * @param vz             z velocity normalized to the range [-1.0f, 1.0f].
     * @param listener       Register a callback to receive update of the command execution state.
     */
    public void moveAtVelocity(@CoordinateFrame int referenceFrame, float vx, float vy, float vz, AbstractCommandListener listener){
        if(!isWithinBounds(vx, -1f, 1f) || !isWithinBounds(vy, -1f, 1f) || !isWithinBounds(vz, -1f, 1f)){
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        float projectedX = vx;
        float projectedY = vy;

        if(referenceFrame == VEHICLE_COORDINATE_FRAME) {
            Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
            double attitudeInRad = Math.toRadians(attitude.getYaw());

            final double cosAttitude = Math.cos(attitudeInRad);
            final double sinAttitude = Math.sin(attitudeInRad);

            projectedX = (float) (vx * cosAttitude) - (float) (vy * sinAttitude);
            projectedY = (float) (vx * sinAttitude) + (float) (vy * cosAttitude);
        }

        moveAtVelocity(projectedX, projectedY, vz, listener);
    }

    private static boolean isWithinBounds(float value, float lowerBound, float upperBound){
        return value <= upperBound && value >= lowerBound;
    }
}
