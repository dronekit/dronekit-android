package org.droidplanner.services.android.lib.util.version;

import android.content.Context;

import org.droidplanner.android.client.R;

/**
 * Created by fhuya on 11/12/14.
 */
public class VersionUtils {

    /**
     * @deprecated
     * @param context
     * @return
     */
    public static int getDeprecatedLibVersion(Context context){
        return context.getResources().getInteger(R.integer.deprecated_lib_version);
    }

    public static int getTowerLibVersion(Context context){
        return context.getResources().getInteger(R.integer.tower_lib_version);
    }

    //Prevent instantiation.
    private VersionUtils(){}
}
