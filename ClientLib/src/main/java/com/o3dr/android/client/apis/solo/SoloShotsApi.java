package com.o3dr.android.client.apis.solo;

import com.o3dr.android.client.Drone;

import java.util.concurrent.ConcurrentHashMap;

/**
 * //TODO: Complete solo shot api. Made invisible to api users until then.
 * Created by Fredia Huya-Kouadio on 7/31/15.
 */
 class SoloShotsApi extends SoloApi {

    private static final ConcurrentHashMap<Drone, SoloShotsApi> soloShotsApiCache = new ConcurrentHashMap<>();
    private static final Builder<SoloShotsApi> apiBuilder = new Builder<SoloShotsApi>() {
        @Override
        public SoloShotsApi build(Drone drone) {
            return new SoloShotsApi(drone);
        }
    };

    public static SoloShotsApi getApi(final Drone drone){
        return getApi(drone, soloShotsApiCache, apiBuilder);
    }

    protected SoloShotsApi(Drone drone) {
        super(drone);
    }
}
