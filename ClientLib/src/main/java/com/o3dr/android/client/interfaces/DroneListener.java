package com.o3dr.android.client.interfaces;

import android.os.Bundle;

import com.o3dr.services.android.lib.drone.connection.ConnectionResult;

/**
 * Created by fhuya on 11/18/14.
 */
public interface DroneListener {

    /**
     * @deprecated Use {@link #onDroneEvent(String, Bundle)} with event
     * {@link com.o3dr.services.android.lib.gcs.link.LinkEvent#LINK_STATE_UPDATED} instead.
     *
     * @param result
     */
    void onDroneConnectionFailed(ConnectionResult result);

    void onDroneEvent(String event, Bundle extras);

    void onDroneServiceInterrupted(String errorMsg);
}
