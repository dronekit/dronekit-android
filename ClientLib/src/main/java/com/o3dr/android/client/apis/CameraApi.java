package com.o3dr.android.client.apis;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.camera.action.CameraActions.ACTION_START_VIDEO_RECORDING;
import static com.o3dr.services.android.lib.drone.camera.action.CameraActions.ACTION_STOP_VIDEO_RECORDING;

/**
 * Contains builder methods used to take camera specific actions
 * Created by Fredia Huya-Kouadio on 4/7/15.
 */
public class CameraApi implements Api {

    private static final ConcurrentHashMap<Drone, CameraApi> cameraApiCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a CameraApi instance.
     *
     * @param drone target vehicle
     * @return a CameraApi instance.
     */
    public static CameraApi getApi(final Drone drone) {
        return ApiUtils.getApi(drone, cameraApiCache, new Builder<CameraApi>() {
            @Override
            public CameraApi build() {
                return new CameraApi(drone);
            }
        });
    }

    private final Drone drone;

    private CameraApi(Drone drone) {
        this.drone = drone;
    }

    /**
     * EXPERIMENTAL
     * <p/>
     * Start recording video on the GoPro.
     * After calling this method, listen for {@link com.o3dr.services.android.lib.drone.attribute.AttributeEvent#GOPRO_STATE_UPDATED} for notification of recording state change.
     *
     * @return
     */
    public boolean startVideoRecording() {
        return drone.performAsyncAction(new Action(ACTION_START_VIDEO_RECORDING));
    }

    /**
     * EXPERIMENTAL!
     * <p/>
     * Stop recording video on the GoPro.
     * After calling this method, listen for {@link com.o3dr.services.android.lib.drone.attribute.AttributeEvent#GOPRO_STATE_UPDATED} for notification of recording state change.
     *
     * @return
     */
    public boolean stopVideoRecording() {
        return drone.performAsyncAction(new Action(ACTION_STOP_VIDEO_RECORDING));
    }
}
