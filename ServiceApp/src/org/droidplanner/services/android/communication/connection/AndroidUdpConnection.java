package org.droidplanner.services.android.communication.connection;

import java.io.File;
import java.io.IOException;

import org.droidplanner.core.MAVLink.connection.UdpConnection;
import org.droidplanner.core.model.Logger;

import android.content.Context;

public class AndroidUdpConnection extends AndroidMavLinkConnection {

	private final UdpConnection mConnectionImpl;
    private final int serverPort;

	public AndroidUdpConnection(Context context, int udpServerPort) {
		super(context);
        this.serverPort = udpServerPort;

		mConnectionImpl = new UdpConnection() {
			@Override
			protected int loadServerPort() {
				return serverPort;
			}

			@Override
			protected Logger initLogger() {
				return AndroidUdpConnection.this.initLogger();
			}

			@Override
			protected File getTempTLogFile() {
				return AndroidUdpConnection.this.getTempTLogFile();
			}

			@Override
			protected void commitTempTLogFile(File tlogFile) {
				AndroidUdpConnection.this.commitTempTLogFile(tlogFile);
			}
		};
	}

	@Override
	protected void closeConnection() throws IOException {
		mConnectionImpl.closeConnection();
	}

	@Override
	protected void loadPreferences() {
		mConnectionImpl.loadPreferences();
	}

	@Override
	protected void openConnection() throws IOException {
		mConnectionImpl.openConnection();
	}

	@Override
	protected int readDataBlock(byte[] buffer) throws IOException {
		return mConnectionImpl.readDataBlock(buffer);
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		mConnectionImpl.sendBuffer(buffer);
	}

	@Override
	public int getConnectionType() {
		return mConnectionImpl.getConnectionType();
	}
}
