package com.o3dr.android.client.apis;

import com.o3dr.android.client.Drone;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides utility methods for api access and use.
 * Created by Fredia Huya-Kouadio on 7/5/15.
 */
class ApiUtils {

    /**
     * Retrieves the api instance bound to the given Drone object.
     * @param drone Drone object
     * @param apiCache Used to retrieve the api instance if it exists, or store it if it doesn't exist.
     * @param apiBuilder Api instance generator.
     * @param <T> Specific api instance type.
     * @return The matching Api instance.
     */
    static <T extends Api> T getApi(Drone drone, ConcurrentHashMap<Drone, T> apiCache, Api.Builder<T> apiBuilder){
        if(drone == null || apiCache == null)
            return null;

        T apiInstance = apiCache.get(drone);
        if(apiInstance == null && apiBuilder != null){
            apiInstance = apiBuilder.build();
            final T previousInstance = apiCache.putIfAbsent(drone, apiInstance);
            if(previousInstance != null)
                apiInstance = previousInstance;
        }

        return apiInstance;
    }

}
