package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

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
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_IS_CLOCKWISE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_IS_RELATIVE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_YAW_TARGET_ANGLE;

/**
 * Provides access to the vehicle control functionality.
 * Created by Fredia Huya-Kouadio on 9/7/15.
 */
public class ControlApi extends Api {

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
     * @param turnSpeed Speed during turn in degrees per second
     * @param isClockwise True for clockwise turn, false for counter clockwise turn
     * @param isRelative True is the target angle is relative to the current vehicle attitude, false otherwise if it's absolute.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void turnTo(float targetAngle, float turnSpeed, boolean isClockwise, boolean isRelative, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putFloat(EXTRA_YAW_TARGET_ANGLE, targetAngle);
        params.putFloat(EXTRA_YAW_CHANGE_RATE, turnSpeed);
        params.putBoolean(EXTRA_YAW_IS_CLOCKWISE, isClockwise);
        params.putBoolean(EXTRA_YAW_IS_RELATIVE, isRelative);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_CONDITION_YAW, params), listener);
    }

    /**
     * Set the vehicle velocity vector.
     * @param vx x velocity in meter / s
     * @param vy y velocity in meter / s
     * @param vz z velocity in meter / s
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void setVelocity(float vx, float vy, float vz, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putFloat(EXTRA_VELOCITY_X, vx);
        params.putFloat(EXTRA_VELOCITY_Y, vy);
        params.putFloat(EXTRA_VELOCITY_Z, vz);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VELOCITY, params), listener);
    }
}
