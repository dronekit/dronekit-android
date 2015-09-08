package com.o3dr.android.client.apis;

import com.o3dr.android.client.Drone;

import java.util.concurrent.ConcurrentHashMap;

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
}
