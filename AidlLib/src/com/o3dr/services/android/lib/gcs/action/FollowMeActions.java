package com.o3dr.services.android.lib.gcs.action;

import android.os.Bundle;

import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class FollowMeActions {

    public static final String ACTION_ENABLE_FOLLOW_ME = Utils.PACKAGE_NAME + ".action.ENABLE_FOLLOW_ME";
    public static final String EXTRA_FOLLOW_TYPE = "extra_follow_type";

    public static final String ACTION_UPDATE_FOLLOW_ME_RADIUS = Utils.PACKAGE_NAME + ".action.UPDATE_FOLLOW_ME_RADIUS";
    public static final String EXTRA_FOLLOW_ME_RADIUS = "extra_follow_me_radius";

    public static final String ACTION_DISABLE_FOLLOW_ME = Utils.PACKAGE_NAME + ".action.DISABLE_FOLLOW_ME";

    /**
     * Enables follow-me if disabled.
     * @param followType follow-me mode to use.
     */
    public static Action buildEnableFollowMe(FollowType followType){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_FOLLOW_TYPE, followType);
        return new Action(ACTION_ENABLE_FOLLOW_ME, params);
    }

    /**
     * Sets the follow-me radius.
     * @param radius radius in meters.
     */
    public static Action buildUpdateFollowMeRadius(double radius){
        Bundle params = new Bundle();
        params.putDouble(EXTRA_FOLLOW_ME_RADIUS, radius);
        return new Action(ACTION_UPDATE_FOLLOW_ME_RADIUS, params);
    }

    /**
     * Disables follow me is enabled.
     */
    public static Action buildDisableFollowMe(){
        return new Action(ACTION_DISABLE_FOLLOW_ME);
    }
}
