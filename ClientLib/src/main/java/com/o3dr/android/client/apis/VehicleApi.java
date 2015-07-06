package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_CONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_DISCONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.EXTRA_CONNECT_PARAMETER;

/**
 * Provides access to the vehicle specific functionality.
 */
public class VehicleApi implements Api {

    private static final ConcurrentHashMap<Drone, VehicleApi> vehicleApiCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a vehicle api instance.
     * @param drone target vehicle
     * @return a VehicleApi instance.
     */
    public static VehicleApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, vehicleApiCache, new Builder<VehicleApi>() {
            @Override
            public VehicleApi build() {
                return new VehicleApi(drone);
            }
        });
    }

    private final Drone drone;

    private VehicleApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Establish connection with the vehicle.
     *
     * @param parameter parameter for the connection.
     */
    public void connect(ConnectionParameter parameter){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_CONNECT_PARAMETER, parameter);
        Action connectAction = new Action(ACTION_CONNECT, params);
        drone.performAsyncAction(connectAction);
    }

    /**
     * Break connection with the vehicle.
     */
    public void disconnect(){
        drone.performAsyncAction(new Action(ACTION_DISCONNECT));
    }
}
