package com.o3dr.android.client.apis;

import android.os.Bundle;
import android.view.Surface;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.CameraActions.ACTION_START_VIDEO_STREAM;
import static com.o3dr.services.android.lib.drone.action.CameraActions.ACTION_STOP_VIDEO_STREAM;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_DISPLAY;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_TAG;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_UDP_PORT;

/**
 * Created by Fredia Huya-Kouadio on 10/11/15.
 */
public class CameraApi extends Api{

    private static final ConcurrentHashMap<Drone, CameraApi> apiCache = new ConcurrentHashMap<>();
    private static final Builder<CameraApi> apiBuilder = new Builder<CameraApi>() {
        @Override
        public CameraApi build(Drone drone) {
            return new CameraApi(drone);
        }
    };

    /**
     * Retrieves a camera api instance
     * @param drone
     * @return
     */
    public static CameraApi getApi(final Drone drone){
        return getApi(drone, apiCache, apiBuilder);
    }

    private final Drone drone;

    private CameraApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Attempt to grab ownership and start the video stream from the connected drone. Can fail if
     * the video stream is already owned by another client.
     *
     * @param udpPort Udp port from which to expect the video stream
     * @param surface  Surface object onto which the video is decoded.
     * @param tag      Video tag.
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void startVideoStream(final int udpPort, final Surface surface, final String tag, final AbstractCommandListener listener) {
        if(surface == null){
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        final Bundle params = new Bundle();
        params.putParcelable(EXTRA_VIDEO_DISPLAY, surface);
        params.putString(EXTRA_VIDEO_TAG, tag);
        params.putInt(EXTRA_VIDEO_UDP_PORT, udpPort);

        drone.performAsyncActionOnDroneThread(new Action(ACTION_START_VIDEO_STREAM, params), listener);
    }

    /**
     * Stop the video stream from the connected drone, and release ownership.
     *
     * @param tag      Video tag.
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void stopVideoStream(final String tag, final AbstractCommandListener listener) {
        final Bundle params = new Bundle();
        params.putString(EXTRA_VIDEO_TAG, tag);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_STOP_VIDEO_STREAM, params), listener);
    }
}
