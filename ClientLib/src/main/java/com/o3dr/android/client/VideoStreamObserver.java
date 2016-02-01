package com.o3dr.android.client;

import android.os.Handler;
import android.util.Log;

import com.o3dr.android.client.utils.connection.IpConnectionListener;
import com.o3dr.android.client.utils.connection.UdpConnection;

import java.nio.ByteBuffer;

/**
 * Observer for vehicle video stream. This class also returns attributes related to the video stream.
 */
public class VideoStreamObserver implements IpConnectionListener {
    private static final String TAG = VideoStreamObserver.class.getSimpleName();

    private static final int UDP_BUFFER_SIZE = 1500;
    private static final long RECONNECT_COUNTDOWN_IN_MILLIS = 1000l;
    private static final int SOLO_STREAM_UDP_PORT = 5600;

    private UdpConnection linkConn;
    private Handler handler;

    private IVideoStreamCallback callback;

    // Video stream properties
    private String aspectRatio;
    private int framerate;  // In Hz.
    private String cameraType;

    public VideoStreamObserver(Handler handler, IVideoStreamCallback callback) {
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

    public void start() {
        if (this.linkConn == null) {
            this.linkConn = new UdpConnection(handler, SOLO_STREAM_UDP_PORT,
                UDP_BUFFER_SIZE, true, 42);
            this.linkConn.setIpConnectionListener(this);
        }

        handler.removeCallbacks(reconnectTask);

        Log.d(TAG, "Connecting to video stream...");
        this.linkConn.connect();

        // TODO: Get video stream attributes.
    }

    public void stop() {
        Log.d(TAG, "Stopping video manager");

        handler.removeCallbacks(reconnectTask);

        if (this.linkConn != null) {
            // Break the link.
            this.linkConn.disconnect();
            this.linkConn = null;
        }
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
        callback.onVideoStreamPacketRecieved(packetBuffer.array(), packetBuffer.limit());
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public int getFramerate() {
        return framerate;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    public String getCameraType() {
        return cameraType;
    }

    public void setCameraType(String cameraType) {
        this.cameraType = cameraType;
    }
}
