package com.o3dr.android.client.apis.drone.camera;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.drone.camera.action.CameraActions.ACTION_START_VIDEO_RECORDING;
import static com.o3dr.services.android.lib.drone.camera.action.CameraActions.ACTION_STOP_VIDEO_RECORDING;

/**
 * Contains builder methods used to take camera specific actions
 * Created by Fredia Huya-Kouadio on 4/7/15.
 */
public class CameraApi {

    /**
     * EXPERIMENTAL
     *
     * Start recording video on the GoPro.
     * After calling this method, listen for {@link com.o3dr.services.android.lib.drone.attribute.AttributeEvent#GOPRO_STATE_UPDATED} for notification of recording state change.
     * @param drone
     * @return
     */
    public static boolean startVideoRecording(Drone drone){
        return drone != null && drone.performAsyncAction(new Action(ACTION_START_VIDEO_RECORDING));
    }

    /**
     * EXPERIMENTAL!
     *
     * Stop recording video on the GoPro.
     * After calling this method, listen for {@link com.o3dr.services.android.lib.drone.attribute.AttributeEvent#GOPRO_STATE_UPDATED} for notification of recording state change.
     * @param drone
     * @return
     */
    public static boolean stopVideoRecording(Drone drone){
        return drone != null && drone.performAsyncAction(new Action(ACTION_STOP_VIDEO_RECORDING));
    }
}
