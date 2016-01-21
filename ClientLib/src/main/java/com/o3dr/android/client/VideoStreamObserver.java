package com.o3dr.android.client;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.o3dr.android.client.utils.connection.IpConnectionListener;
import com.o3dr.android.client.utils.connection.UdpConnection;

import java.nio.ByteBuffer;

/**
 * TODO
 */
public class VideoStreamObserver implements IpConnectionListener {
    private static final String TAG = VideoStreamObserver.class.getSimpleName();

    private static final int UDP_BUFFER_SIZE = 1500;
    private static final long RECONNECT_COUNTDOWN_IN_MILLIS = 1000l;

    private Context context;
    private UdpConnection linkConn;
    private Handler handler;

    private int linkPort = -1;

    private IVideoStreamCallback callback;

    public VideoStreamObserver(Context context, Handler handler, IVideoStreamCallback callback) {
        this.context = context;
        this.handler = handler;
        this.callback = callback;
    }

    private final Runnable reconnectTask = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(reconnectTask);
            if(linkConn != null)
                linkConn.connect();
        }
    };

    private void start(int udpPort) {
        if (this.linkConn == null || udpPort != this.linkPort){
            this.linkConn = new UdpConnection(handler, udpPort, UDP_BUFFER_SIZE, true, 42);
            this.linkConn.setIpConnectionListener(this);
            this.linkPort = udpPort;
        }

        handler.removeCallbacks(reconnectTask);

        Log.d(TAG, "Connecting to video stream...");
        this.linkConn.connect();
    }

    private void stop() {
        Log.d(TAG, "Stopping video manager");

        handler.removeCallbacks(reconnectTask);

        if (this.linkConn != null) {
            // Break the link
            this.linkConn.disconnect();
            this.linkConn = null;
        }

        this.linkPort = -1;
    }

    @Override
    public void onIpConnected() {
        Log.d(TAG, "Connected to video stream");

        handler.removeCallbacks(reconnectTask);
    }

    @Override
    public void onIpDisconnected() {
        Log.d(TAG, "Video stream disconnected");

        handler.postDelayed(reconnectTask, RECONNECT_COUNTDOWN_IN_MILLIS);
    }

    @Override
    public void onPacketReceived(ByteBuffer packetBuffer) {
        callback.getVideoStreamPackets(packetBuffer.array());
    }
}
