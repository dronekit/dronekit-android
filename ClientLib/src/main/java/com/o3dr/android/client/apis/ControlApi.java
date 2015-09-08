package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_DO_GUIDED_TAKEOFF;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_ALTITUDE;

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
     */
    public void takeoff(double altitude) {
        takeoff(altitude, null);
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
     */
    public void pauseAtCurrentLocation() {
        pauseAtCurrentLocation(null);
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
                sendGuidedPoint(gps.getPosition(), true, listener);
            }
        });
    }
}
