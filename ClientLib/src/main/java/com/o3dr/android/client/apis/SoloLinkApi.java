package com.o3dr.android.client.apis;

import com.o3dr.android.client.Drone;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides access to the sololink specific functionality
 * Created by Fredia Huya-Kouadio on 7/12/15.
 */
public class SoloLinkApi implements Api {

    private static final ConcurrentHashMap<Drone, SoloLinkApi> soloLinkApiCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a sololink api instance.
     * @param drone target vehicle
     * @return a SoloLinkApi instance.
     */
    public static SoloLinkApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, soloLinkApiCache, new Builder<SoloLinkApi>() {
            @Override
            public SoloLinkApi build() {
                return new SoloLinkApi(drone);
            }
        });
    }

    private final Drone drone;

    private SoloLinkApi(Drone drone){
        this.drone = drone;
    }
}
