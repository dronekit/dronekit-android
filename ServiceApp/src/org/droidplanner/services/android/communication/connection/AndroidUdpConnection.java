package org.droidplanner.services.android.communication.connection;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.droidplanner.core.MAVLink.connection.UdpConnection;
import org.droidplanner.core.model.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedList;

public class AndroidUdpConnection extends AndroidMavLinkConnection {

    private static final String TAG = AndroidUdpConnection.class.getSimpleName();

    private final HashSet<PingTask> pingTasks = new HashSet<>();

    private final UdpConnection mConnectionImpl;
    private final int serverPort;

    private Handler pingHandler;

    public AndroidUdpConnection(Context context, int udpServerPort, Handler pingHandler) {
        super(context);
        this.serverPort = udpServerPort;
        this.pingHandler = pingHandler;

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
            protected void onConnectionOpened() {
                AndroidUdpConnection.this.onConnectionOpened();
            }

            @Override
            protected void onConnectionFailed(String errMsg) {
                AndroidUdpConnection.this.onConnectionFailed(errMsg);
            }

        };
    }

    public void addPingTarget(final InetAddress address, final int port, final long period, final byte[] payload) {
        if (pingHandler == null || address == null || payload == null || period <= 0)
            return;

        final PingTask pingTask = new PingTask(address, port, period, payload);

        pingTasks.add(pingTask);

        if (getConnectionStatus() == AndroidMavLinkConnection.MAVLINK_CONNECTED)
            pingHandler.postDelayed(pingTask, period);
    }

    @Override
    protected void closeConnection() throws IOException {
        if (pingHandler != null) {
            for (PingTask pingTask : pingTasks)
                pingHandler.removeCallbacks(pingTask);
        }

        mConnectionImpl.closeConnection();
    }

    @Override
    protected void loadPreferences() {
        mConnectionImpl.loadPreferences();
    }

    @Override
    protected void openConnection() throws IOException {
        mConnectionImpl.openConnection();

        if (pingHandler != null) {
            for (PingTask pingTask : pingTasks)
                pingHandler.postDelayed(pingTask, pingTask.period);
        }
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

    private class PingTask implements Runnable {

        private final InetAddress address;
        private final int port;
        private final long period;
        private final byte[] payload;

        private PingTask(InetAddress address, int port, long period, byte[] payload) {
            this.address = address;
            this.port = port;
            this.period = period;
            this.payload = payload;
        }

        @Override
        public boolean equals(Object other){
            if(this == other)
                return true;

            if(!(other instanceof AndroidUdpConnection))
                return false;

            PingTask that = (PingTask) other;
            return this.address.equals(that.address) && this.port == that.port && this.period == that.period;
        }

        @Override
        public void run() {
            try {
                mConnectionImpl.sendBuffer(address, port, payload);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred while sending ping message.", e);
            }

            if (getConnectionStatus() == AndroidMavLinkConnection.MAVLINK_CONNECTED)
                pingHandler.postDelayed(this, period);
        }
    }
}
