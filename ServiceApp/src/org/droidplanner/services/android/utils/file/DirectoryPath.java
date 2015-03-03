package org.droidplanner.services.android.utils.file;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import org.droidplanner.services.android.R;

public class DirectoryPath {

	/**
	 * Main path used to store private data files related to the program
	 * 
	 * @return Path to 3DR Services private data folder in external storage
	 */
	static public String getPrivateDataPath(Context context) {
        File dataDir = context.getExternalFilesDir(null);
        return dataDir.getAbsolutePath();
	}

    /**
     * Main path used to store public data files related to the app.
     * @param context application context
     * @return Path to 3DR Services public data directory.
     */
    public static String getPublicDataPath(Context context){
        final String root = Environment.getExternalStorageDirectory().getPath();
        return root + "/3DRServices/";
    }

	/**
	 * Folder where telemetry log files are stored
	 */
	static public File getTLogPath(Context context, String appId) {
		File f = new File(getPrivateDataPath(context) + "/tlogs/" + appId);
        if(!f.exists()) {
            f.mkdirs();
        }
		return f;
	}

	/**
	 * After tlogs are uploaded they get moved to this directory
	 */
	static public File getTLogSentPath(Context context, String appId) {
		File f = new File(getTLogPath(context, appId) + "/sent/");
        if(!f.exists()) {
            f.mkdirs();
        }
		return f;
	}

	/**
	 * Storage folder for user camera description files
	 */
	public static String getCameraInfoPath(Context context) {
		return getPublicDataPath(context) + "/CameraInfo/";
	}

	/**
	 * Storage folder for stacktraces
	 */
	public static String getLogCatPath(Context context) {
		return getPrivateDataPath(context) + "/log_cat/";
	}

}
