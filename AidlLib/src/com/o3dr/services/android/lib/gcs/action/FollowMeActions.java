package com.o3dr.services.android.lib.gcs.action;

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

}
