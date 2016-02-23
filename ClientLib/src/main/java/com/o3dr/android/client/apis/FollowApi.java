package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.Utils;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_DISABLE_FOLLOW_ME;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_ENABLE_FOLLOW_ME;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_UPDATE_FOLLOW_PARAMS;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_USE_EXTERNAL_LOCATIONS;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.EXTRA_USE_EXTERNAL_PROVIDER;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.EXTRA_FOLLOW_TYPE;

/**
 * Provides access to the Follow me api.
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class FollowApi extends Api {

    public static final String EVT_EXTERNAL_LOCATION = Utils.PACKAGE_NAME + ".EXTERNAL_LOCATION";

    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LNG = "lng";
    public static final String EXTRA_ALTITUDE = "altitude";
    public static final String EXTRA_HEADING = "heading";
    public static final String EXTRA_SPEED = "speed";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_ACCURACY = "accuracy";

    public static final String EVT_LOCATION_AVAILABILITY = Utils.PACKAGE_NAME + ".LOCATION_AVAILABILITY";
    public static final String EXTRA_AVAILABLE = "available";

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

    /**
     * Specify whether or not to use externally-supplied locations (vs. on-device GPS)
     */
    public void useExternalLocations(boolean use) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_USE_EXTERNAL_PROVIDER, use);
        drone.performAsyncAction(new Action(ACTION_USE_EXTERNAL_LOCATIONS, params));
    }
}
