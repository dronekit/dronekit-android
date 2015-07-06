package com.o3dr.android.client.apis;

import com.o3dr.android.client.Drone;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows to query the capabilities offered by the vehicle.
 * Created by Fredia Huya-Kouadio on 7/5/15.
 */
public class CapabilityApi implements Api {

    private static final ConcurrentHashMap<Drone, CapabilityApi> capabilityApiCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a capability api instance.
     * @param drone target vehicle.
     * @return a CapabilityApi instance.
     */
    public static CapabilityApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, capabilityApiCache, new Builder<CapabilityApi>() {
            @Override
            public CapabilityApi build() {
                return new CapabilityApi(drone);
            }
        });
    }

    private final Drone drone;

    private CapabilityApi(Drone drone){
        this.drone = drone;
    }

}
