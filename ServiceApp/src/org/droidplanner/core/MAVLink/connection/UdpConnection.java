package org.droidplanner.core.MAVLink.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides support for mavlink connection via udp.
 */
public abstract class UdpConnection extends MavLinkConnection {

	private AtomicReference<DatagramSocket> socketRef = new AtomicReference<>();
	private int serverPort;

	private int hostPort;
	private InetAddress hostAdd;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;

	private void getUdpStream() throws IOException {
		final DatagramSocket socket = new DatagramSocket(serverPort);
		socket.setBroadcast(true);
		socket.setReuseAddress(true);
        socketRef.set(socket);
	}

	@Override
	public final void closeConnection() throws IOException {
        final DatagramSocket socket = socketRef.get();
		if (socket != null)
			socket.close();
	}

	@Override
	public final void openConnection() throws IOException {
		getUdpStream();
        onConnectionOpened();
	}

	@Override
	public final void sendBuffer(byte[] buffer) throws IOException {
        final DatagramSocket socket = socketRef.get();
        if(socket == null)
            return;

		try {
			if (hostAdd != null) { // We can't send to our sister until they
				// have connected to us
				if(sendPacket == null)
					sendPacket  = new DatagramPacket(buffer, buffer.length, hostAdd, hostPort);
				else{
					sendPacket.setData(buffer, 0, buffer.length);
					sendPacket.setAddress(hostAdd);
					sendPacket.setPort(hostPort);
				}
				socket.send(sendPacket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void sendBuffer(InetAddress targetAddr, int targetPort, byte[] buffer) throws IOException {
        final DatagramSocket socket = socketRef.get();
        if(socket == null || targetAddr == null || buffer == null)
            return;

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, targetAddr, targetPort);
        socket.send(packet);
    }

	@Override
	public final int readDataBlock(byte[] readData) throws IOException {
		final DatagramSocket socket = socketRef.get();
		if (socket == null)
			return 0;

		if (receivePacket == null)
			receivePacket = new DatagramPacket(readData, readData.length);
		else
			receivePacket.setData(readData);

		socket.receive(receivePacket);
		hostAdd = receivePacket.getAddress();
		hostPort = receivePacket.getPort();
		return receivePacket.getLength();
	}

	@Override
	public final void loadPreferences() {
		serverPort = loadServerPort();
	}

	@Override
	public final int getConnectionType() {
		return MavLinkConnectionTypes.MAVLINK_CONNECTION_UDP;
	}

	protected abstract int loadServerPort();
}
