package org.droidplanner.services.android.utils.apps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.droidplanner.services.android.ui.fragment.RecommendedAppsFragment;

/**
 * Created by Fredia Huya-Kouadio on 2/5/15.
 */
public class AppsUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RecommendedAppsFragment
                .ACTION_REFRESH_RECOMMENDED_APPS));
    }
}
