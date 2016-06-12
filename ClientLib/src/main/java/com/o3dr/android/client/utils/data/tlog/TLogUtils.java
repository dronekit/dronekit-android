package com.o3dr.android.client.utils.data.tlog;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by fredia on 6/11/16.
 */
public class TLogUtils {

    /**
     * Standard directory in which to place documents that have been created by
     * the user.
     */
    private static final String DIRECTORY_DOCUMENTS = "Documents";

    private static final String DIRECTORY_TLOGS = "tlogs";

    // Private to prevent instantiation
    private TLogUtils(){}

    /**
     * Return the directory where the generated tlogs for the given app id are stored.
     * @param appId Application id (i.e: applicationContext.getPackageName())
     * @return File to the tlogs directory, or null if the app id is invalid or the root directory
     * is not available.
     */
    public static File getTLogsDirectory(String appId){
        if (TextUtils.isEmpty(appId)){
            return null;
        }

        final String directoryType = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT
            ? Environment.DIRECTORY_DOCUMENTS
            : DIRECTORY_DOCUMENTS;
        final File documentsDir = Environment.getExternalStoragePublicDirectory(directoryType);
        if (documentsDir == null) {
            return null;
        }

        return new File(documentsDir, appId + "/" + DIRECTORY_TLOGS);
    }
}
