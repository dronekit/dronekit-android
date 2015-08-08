package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_DISABLE_FOLLOW_ME;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_ENABLE_FOLLOW_ME;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_UPDATE_FOLLOW_PARAMS;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.EXTRA_FOLLOW_TYPE;

/**
 * Provides access to the Follow me api.
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class FollowApi extends Api {

    private static final ConcurrentHashMap<Drone, FollowApi> followApiCache = new ConcurrentHashMap<>();
    private static final Builder<FollowApi> apiBuilder = new Builder<FollowApi>() {
        @Override
        public FollowApi build(Drone drone) {
            return new FollowApi(drone);
        }
    };

    /**
     * Retrieves a FollowApi instance.
     *
     * @param drone target vehicle
     * @return a FollowApi instance.
     */
    public static FollowApi getApi(final Drone drone) {
        return getApi(drone, followApiCache, apiBuilder);
    }

    private final Drone drone;

    private FollowApi(Drone drone) {
        this.drone = drone;
    }

    /**
     * Enables follow-me if disabled.
     *
     * @param followType follow-me mode to use.
     */
    public void enableFollowMe(FollowType followType) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_FOLLOW_TYPE, followType);
        drone.performAsyncAction(new Action(ACTION_ENABLE_FOLLOW_ME, params));
    }

    /**
     * Updates the parameters for the currently enabled follow me mode.
     *
     * @param params Set of parameters for the current follow me mode.
     */
    public void updateFollowParams(Bundle params) {
        drone.performAsyncAction(new Action(ACTION_UPDATE_FOLLOW_PARAMS, params));
    }

    /**
     * Disables follow me is enabled.
     */
    public void disableFollowMe() {
        drone.performAsyncAction(new Action(ACTION_DISABLE_FOLLOW_ME));
    }
}
