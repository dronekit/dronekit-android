package com.o3dr.services.android.lib.util.version;

import android.content.Context;
import android.content.pm.PackageManager;

import com.o3dr.android.client.R;

/**
 * Created by fhuya on 11/12/14.
 */
public class VersionUtils {

    public static int getCoreLibVersion(Context context){
        return context.getResources().getInteger(R.integer.core_lib_version);
    }

    //Prevent instantiation.
    private VersionUtils(){}
}
