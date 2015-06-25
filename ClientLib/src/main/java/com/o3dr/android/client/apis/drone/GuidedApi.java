package com.o3dr.android.client.apis.drone;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.drone.action.GuidedActions.ACTION_DO_GUIDED_TAKEOFF;
import static com.o3dr.services.android.lib.drone.action.GuidedActions.ACTION_SEND_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.GuidedActions.ACTION_SET_GUIDED_ALTITUDE;
import static com.o3dr.services.android.lib.drone.action.GuidedActions.EXTRA_ALTITUDE;
import static com.o3dr.services.android.lib.drone.action.GuidedActions.EXTRA_FORCE_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.GuidedActions.EXTRA_GUIDED_POINT;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class GuidedApi {

    /**
     * Perform a guided take off.
     * @param drone target vehicle
     * @param altitude altitude in meters
     */
    public static void takeoff(Drone drone, double altitude) {
        takeoff(drone, altitude, null);
    }

    /**
     * Perform a guided take off.
     *
     * @param drone    target vehicle
     * @param altitude altitude in meters
     * @param listener Register a callback to receive update of the command execution state.
     */
    public static void takeoff(Drone drone, double altitude, SimpleCommandListener listener) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        drone.performAsyncAction(new Action(ACTION_DO_GUIDED_TAKEOFF, params), listener);
    }

    /**
     * Send a guided point to the connected drone.
     *@param drone target vehicle
     * @param point guided point location
     * @param force true to enable guided mode is required.
     */
    public static void sendGuidedPoint(Drone drone, LatLong point, boolean force) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_FORCE_GUIDED_POINT, force);
        params.putParcelable(EXTRA_GUIDED_POINT, point);
        drone.performAsyncAction(new Action(ACTION_SEND_GUIDED_POINT, params));
    }

    /**
     * Set the altitude for the guided point.
     *@param drone target vehicle
     * @param altitude altitude in meters
     */
    public static void setGuidedAltitude(Drone drone, double altitude) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        drone.performAsyncAction(new Action(ACTION_SET_GUIDED_ALTITUDE, params));
    }

    /**
     *  Pause the vehicle at its current location.
     * @param drone target vehicle
     */
    public static void pauseAtCurrentLocation(final Drone drone){
        drone.getAttributeAsync(AttributeType.GPS, new Drone.AttributeRetrievedListener<Gps>() {
            @Override
            public void onRetrievalSucceed(Gps gps) {
                sendGuidedPoint(drone, gps.getPosition(), true);
            }
        });
    }
}
