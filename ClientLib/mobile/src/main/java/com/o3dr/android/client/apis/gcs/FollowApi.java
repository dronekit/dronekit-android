package com.o3dr.android.client.apis.gcs;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_DISABLE_FOLLOW_ME;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_ENABLE_FOLLOW_ME;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_UPDATE_FOLLOW_ME_RADIUS;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.EXTRA_FOLLOW_ME_RADIUS;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.EXTRA_FOLLOW_TYPE;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class FollowApi {

    /**
     * Enables follow-me if disabled.
     *
     * @param followType follow-me mode to use.
     */
    public static void enableFollowMe(Drone drone, FollowType followType) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_FOLLOW_TYPE, followType);
        drone.performAsyncAction(new Action(ACTION_ENABLE_FOLLOW_ME, params));
    }

    /**
     * Sets the follow-me radius.
     *
     * @param radius radius in meters.
     */
    public static void updateFollowMeRadius(Drone drone, double radius) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_FOLLOW_ME_RADIUS, radius);
        drone.performAsyncAction(new Action(ACTION_UPDATE_FOLLOW_ME_RADIUS, params));
    }

    /**
     * Disables follow me is enabled.
     */
    public static void disableFollowMe(Drone drone) {
        drone.performAsyncAction(new Action(ACTION_DISABLE_FOLLOW_ME));
    }
}
