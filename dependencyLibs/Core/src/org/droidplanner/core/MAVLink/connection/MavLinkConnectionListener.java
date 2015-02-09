package org.droidplanner.core.MAVLink.connection;

import com.MAVLink.MAVLinkPacket;

/**
 * Provides updates about the mavlink connection.
 */
public interface MavLinkConnectionListener {

    /**
     * Called when a connection is being established.
     */
    public void onStartingConnection();

	/**
	 * Called when the mavlink connection is established.
	 */
	public void onConnect(long connectionTime);

	/**
	 * Called when data is received via the mavlink connection.
	 * 
	 * @param packet
	 *            received data
	 */
	public void onReceivePacket(MAVLinkPacket packet);

	/**
	 * Called when the mavlink connection is disconnected.
	 */
	public void onDisconnect(long disconnectionTime);

	/**
	 * Provides information about communication error.
	 * 
	 * @param errMsg
	 *            error information
	 */
	public void onComError(String errMsg);

}
