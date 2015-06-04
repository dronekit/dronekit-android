package com.o3dr.services.android.lib.util.version;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by fhuya on 11/12/14.
 */
public class VersionUtils {

    public static final int LIB_VERSION = 20214;

    public static int getVersion(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }
}
