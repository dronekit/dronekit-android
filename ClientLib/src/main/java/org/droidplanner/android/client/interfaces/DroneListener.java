package org.droidplanner.android.client.interfaces;

import android.os.Bundle;

import org.droidplanner.services.android.lib.drone.connection.ConnectionResult;
import org.droidplanner.services.android.lib.gcs.link.LinkConnectionStatus;

/**
 * Created by fhuya on 11/18/14.
 */
public interface DroneListener {

    /**
     * @deprecated Use {@link LinkListener#onLinkStateUpdated(LinkConnectionStatus)} instead.
     *
     * @param result
     */
    void onDroneConnectionFailed(ConnectionResult result);

    void onDroneEvent(String event, Bundle extras);

    void onDroneServiceInterrupted(String errorMsg);
}
