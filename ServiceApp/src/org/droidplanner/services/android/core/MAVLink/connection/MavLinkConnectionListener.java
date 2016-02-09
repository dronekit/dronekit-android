package org.droidplanner.services.android.core.MAVLink.connection;

import com.MAVLink.MAVLinkPacket;
import com.o3dr.services.android.lib.drone.connection.LinkConnectionStatus;

/**
 * Provides updates about the mavlink connection.
 */
public interface MavLinkConnectionListener {
    /**
     * Called when data is received via the mavlink connection.
     *
     * @param packet received data
     */
    void onReceivePacket(MAVLinkPacket packet);

    /**
     * Provides information about communication error.
     *
     * @param connectionStatus error information
     */
    void onConnectionStatus(LinkConnectionStatus connectionStatus);

}
