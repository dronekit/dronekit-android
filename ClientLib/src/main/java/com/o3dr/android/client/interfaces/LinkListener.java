package com.o3dr.android.client.interfaces;

import com.o3dr.services.android.lib.drone.connection.LinkConnectionStatus;

/**
 * Created by chavi on 2/8/16.
 */
public interface LinkListener {

    void onLinkStateUpdated(LinkConnectionStatus connectionStatus);
}
