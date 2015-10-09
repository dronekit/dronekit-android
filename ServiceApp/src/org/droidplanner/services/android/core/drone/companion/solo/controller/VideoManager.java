package org.droidplanner.services.android.core.drone.companion.solo.controller;

import android.content.Context;
import android.os.Handler;
import android.view.Surface;

import org.droidplanner.services.android.core.drone.companion.solo.AbstractLinkManager;
import org.droidplanner.services.android.utils.connection.SshConnection;
import org.droidplanner.services.android.utils.connection.UdpConnection;
import org.droidplanner.services.android.utils.video.DecoderListener;
import org.droidplanner.services.android.utils.video.MediaCodecManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

import timber.log.Timber;

/**
 * Handles the video stream from artoo.
 */
public class VideoManager extends AbstractLinkManager {

    private static final int UDP_BUFFER_SIZE = 1500;

    private final MediaCodecManager mediaCodecManager;

    VideoManager(Context context, Handler handler, ExecutorService asyncExecutor) {
        super(context, new UdpConnection(handler, ControllerLinkManager.ARTOO_UDP_PORT, UDP_BUFFER_SIZE, true, 42), handler, asyncExecutor);
        this.mediaCodecManager = new MediaCodecManager(handler);
    }

    void startDecoding(final Surface surface, final DecoderListener listener) {
        //Stop any in progress decoding.
        Timber.i( "Setting up video stream decoding.");
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
                    Timber.i( "Video decoding set up complete. Starting...");
                    mediaCodecManager.startDecoding(surface, listener);
                } catch (IOException | IllegalStateException e) {
                    Timber.e( "Unable to create media codec.", e);
                    if (listener != null)
                        listener.onDecodingError();
                }
            }
        });
    }

    void stopDecoding(DecoderListener listener) {
        Timber.i( "Aborting video decoding process.");
        mediaCodecManager.stopDecoding(listener);
    }

    @Override
    public void refreshState() {
        //We're good to go for the video stream.
        Timber.d( "Connected to video stream");
    }

    @Override
    protected SshConnection getSshLink() {
        return ControllerLinkManager.sshLink;
    }

    @Override
    public void start(LinkListener listener) {
        Timber.d("Starting video manager");
        super.start(listener);
    }

    @Override
    public void stop() {
        Timber.d( "Stopping video manager");
        super.stop();
    }

    @Override
    public void onIpDisconnected() {
        Timber.d( "Video stream disconnected");

        super.onIpDisconnected();
    }

    @Override
    public void onPacketReceived(ByteBuffer packetBuffer) {
        //Feed this data stream to the decoder.
        mediaCodecManager.onInputDataReceived(packetBuffer.array(), packetBuffer.limit());
    }
}

