package org.droidplanner.services.android.utils.connection;

import java.nio.ByteBuffer;

/**
 * Provides updates about the connection.
 */
public interface IpConnectionListener {

    public void onIpConnected();

    public void onIpDisconnected();

    public void onPacketReceived(ByteBuffer packetBuffer);
}
