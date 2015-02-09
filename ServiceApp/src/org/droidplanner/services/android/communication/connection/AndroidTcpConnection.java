package org.droidplanner.services.android.communication.connection;

import java.io.File;
import java.io.IOException;

import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;
import org.droidplanner.core.MAVLink.connection.TcpConnection;
import org.droidplanner.core.model.Logger;

import android.content.Context;

public class AndroidTcpConnection extends AndroidMavLinkConnection {

	private final TcpConnection mConnectionImpl;
    private final String serverIp;
    private final int serverPort;

	public AndroidTcpConnection(Context context, String tcpServerIp, int tcpServerPort) {
		super(context);
        this.serverIp = tcpServerIp;
        this.serverPort = tcpServerPort;

		mConnectionImpl = new TcpConnection() {
			@Override
			protected int loadServerPort() {
				return serverPort;
			}

			@Override
			protected String loadServerIP() {
				return serverIp;
			}

			@Override
			protected Logger initLogger() {
				return AndroidTcpConnection.this.initLogger();
			}

            @Override
            protected void onConnectionOpened(){
                AndroidTcpConnection.this.onConnectionOpened();
            }

            @Override
            protected void onConnectionFailed(String errMsg){
                AndroidTcpConnection.this.onConnectionFailed(errMsg);
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
