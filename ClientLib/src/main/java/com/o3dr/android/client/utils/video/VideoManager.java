package com.o3dr.android.client.utils.video;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

import com.o3dr.android.client.utils.connection.AbstractIpConnection;
import com.o3dr.android.client.utils.connection.IpConnectionListener;
import com.o3dr.android.client.utils.connection.UdpConnection;
import com.o3dr.services.android.lib.model.ICommandListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles the video stream from artoo.
 */
public class VideoManager implements IpConnectionListener {

    private static final String TAG = VideoManager.class.getSimpleName();

    protected static final long RECONNECT_COUNTDOWN = 1000l; //ms

    public static final int ARTOO_UDP_PORT = 5600;
    private static final int UDP_BUFFER_SIZE = 1500;

    public interface LinkListener {
        void onLinkConnected();

        void onLinkDisconnected();
    }

    private final Runnable reconnectTask = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(reconnectTask);
            linkConn.connect();
        }
    };

    private LinkListener linkListener;

    protected final Handler handler;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final AtomicBoolean wasConnected = new AtomicBoolean(false);

    protected final AbstractIpConnection linkConn;

    private final MediaCodecManager mediaCodecManager;

    public VideoManager(Handler handler) {
        this.linkConn = new UdpConnection(handler, ARTOO_UDP_PORT, UDP_BUFFER_SIZE, true, 42);
        this.linkConn.setIpConnectionListener(this);

        this.handler = handler;
        this.mediaCodecManager = new MediaCodecManager(handler);
    }

    public void startDecoding(final Surface surface, final DecoderListener listener) {
        final Surface currentSurface = mediaCodecManager.getSurface();
        if (surface == currentSurface) {
            if (listener != null)
                listener.onDecodingStarted();
            return;
        }

        //Stop any in progress decoding.
        Log.i(TAG, "Setting up video stream decoding.");
        mediaCodecManager.stopDecoding(new DecoderListener() {
            @Override
            public void onDecodingStarted() {
            }

            @Override
            public void onDecodingError() {
            }

            @Override
            public void onDecodingEnded() {
                try {
                    Log.i(TAG, "Video decoding set up complete. Starting...");
                    mediaCodecManager.startDecoding(surface, listener);
                } catch (IOException | IllegalStateException e) {
                    Log.e(TAG, "Unable to create media codec.", e);
                    if (listener != null)
                        listener.onDecodingError();
                }
            }
        });
    }

    public void stopDecoding(DecoderListener listener) {
        Log.i(TAG, "Aborting video decoding process.");
        mediaCodecManager.stopDecoding(listener);
    }

    public boolean isLinkConnected() {
        return this.linkConn.getConnectionStatus() == AbstractIpConnection.STATE_CONNECTED;
    }

    public void start(LinkListener listener) {
        Log.d(TAG, "Starting video manager");
        handler.removeCallbacks(reconnectTask);

        isStarted.set(true);
        this.linkConn.connect();

        this.linkListener = listener;
    }

    public void stop() {
        Log.d(TAG, "Stopping video manager");

        handler.removeCallbacks(reconnectTask);

        isStarted.set(false);

        //Break the link
        this.linkConn.disconnect();
    }

    @Override
    public void onIpConnected() {
        Log.d(TAG, "Connected to video stream");

        handler.removeCallbacks(reconnectTask);
        wasConnected.set(true);

        if (linkListener != null)
            linkListener.onLinkConnected();
    }

    @Override
    public void onIpDisconnected() {
        Log.d(TAG, "Video stream disconnected");

        if (isStarted.get()) {
            if (shouldReconnect()) {
                //Try to reconnect
                handler.postDelayed(reconnectTask, RECONNECT_COUNTDOWN);
            }

            if (linkListener != null && wasConnected.get())
                linkListener.onLinkDisconnected();

            wasConnected.set(false);
        }
    }

    @Override
    public void onPacketReceived(ByteBuffer packetBuffer) {
        //Feed this data stream to the decoder.
        mediaCodecManager.onInputDataReceived(packetBuffer.array(), packetBuffer.limit());
    }

    protected void postSuccessEvent(final ICommandListener listener) {
        if (handler != null && listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onSuccess();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        }
    }

    protected void postTimeoutEvent(final ICommandListener listener) {
        if (handler != null && listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onTimeout();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        }
    }

    protected void postErrorEvent(final int error, final ICommandListener listener) {
        if (handler != null && listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onError(error);
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        }
    }

    protected boolean shouldReconnect() {
        return true;
    }
}

