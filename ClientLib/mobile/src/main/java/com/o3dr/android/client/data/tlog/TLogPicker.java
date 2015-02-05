package com.o3dr.android.client.data.tlog;

import android.app.Activity;
import android.content.Intent;

import com.o3dr.services.android.lib.data.ServiceDataContract;

/**
 * Provides utility to select a tlog files.
 */
public class TLogPicker {

    public static void startTLogPicker(Activity activity, int resultCode){
        if(activity == null)
            return;

        final Intent requestIntent = new Intent(ServiceDataContract.ACTION_REQUEST_TLOG)
                .putExtra(ServiceDataContract.EXTRA_REQUEST_TLOG_APP_ID, activity.getPackageName())
                .setType(ServiceDataContract.TLOG_MIME_TYPE);
        activity.startActivityForResult(requestIntent, resultCode);
    }

    //Private constructor to prevent instantiation
    private TLogPicker(){}
}
