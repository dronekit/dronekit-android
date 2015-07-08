package com.o3dr.android.client.apis;

/**
 * Common interface for the drone set of api classes.
 * Created by Fredia Huya-Kouadio on 7/5/15.
 */
interface Api {

    interface Builder<T extends Api> {
        T build();
    }
}
