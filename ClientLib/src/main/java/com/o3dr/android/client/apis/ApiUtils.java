package com.o3dr.android.client.apis;

import com.o3dr.android.client.Drone;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Fredia Huya-Kouadio on 7/5/15.
 */
class ApiUtils {

    static <T extends Api> T getApi(Drone drone, ConcurrentHashMap<Drone, T> apiCache, Api.Builder<T> apiBuilder){
        if(drone == null || apiCache == null)
            return null;

        T apiInstance = apiCache.get(drone);
        if(apiInstance == null && apiBuilder != null){
            apiInstance = apiBuilder.build();
            apiInstance = apiCache.putIfAbsent(drone, apiInstance);
        }

        return apiInstance;
    }

}
