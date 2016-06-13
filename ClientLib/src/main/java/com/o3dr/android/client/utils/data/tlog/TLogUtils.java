package com.o3dr.android.client.utils.data.tlog;

import android.os.Environment;
import android.text.TextUtils;

import com.o3dr.android.client.utils.FileUtils;

import java.io.File;
import java.text.ParsePosition;
import java.util.Date;

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
    private static final String TLOG_FILENAME_EXT = ".tlog";
    private static final String TLOG_PREFIX = "log";

    private static final String PART_SEPARATOR = "_";

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

    /**
     * Generate a tlog filename based on the given parameters
     * @param connectionTypeLabel Label describing the connection type (i.e: usb, tcp,...)
     * @param connectionTimestamp Timestamp when the connection was established
     * @return Filename for a tlog file
     */
    public static String getTLogFilename(String connectionTypeLabel, long connectionTimestamp){
        return TLOG_PREFIX
                + "_" + connectionTypeLabel
                + "_" + FileUtils.getTimeStamp(connectionTimestamp)
                + TLOG_FILENAME_EXT;
    }

    /**
     * Parse the given tlog filename, and extract its connection timestamp
     * @param tlogFilename Filename to parse
     * @return Date object if correctly parsed, null otherwise
     */
    public static Date parseTLogConnectionTimestamp(String tlogFilename){
        if(TextUtils.isEmpty(tlogFilename))
            return null;

        // Check if this is the filename for a tlog file
        if(!tlogFilename.endsWith(TLOG_FILENAME_EXT))
            return null;

        int dateStartIndex = tlogFilename.indexOf("_", TLOG_PREFIX.length() + 1) + 1;
        return FileUtils.timestampFormatter.parse(tlogFilename, new ParsePosition(dateStartIndex));
    }
}
