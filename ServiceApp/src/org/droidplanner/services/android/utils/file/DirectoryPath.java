package org.droidplanner.services.android.utils.file;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class DirectoryPath {

	/**
	 * Main path used to store files related to the program
	 * 
	 * @return Path to DroidPlanner/ folder in external storage
	 */
	static public String get3DRServicesPath(Context context) {
        File dataDir = context.getExternalFilesDir(null);
        return dataDir.getAbsolutePath();
	}

	/**
	 * Folder where telemetry log files are stored
	 */
	static public File getTLogPath(Context context) {
		File f = new File(get3DRServicesPath(context) + "/tlogs/");
        if(!f.exists()) {
            f.mkdirs();
        }
		return f;
	}

	/**
	 * After tlogs are uploaded they get moved to this directory
	 */
	static public File getSentPath(Context context) {
		File f = new File(getTLogPath(context) + "/sent/");
        if(!f.exists()) {
            f.mkdirs();
        }
		return f;
	}

	/**
	 * Storage folder for user camera description files
	 */
	public static String getCameraInfoPath(Context context) {
		return get3DRServicesPath(context) + "/camera_info/";
	}

	/**
	 * Storage folder for stacktraces
	 */
	public static String getLogCatPath(Context context) {
		return get3DRServicesPath(context) + "/log_cat/";
	}

	/**
	 * Storage folder for SRTM data
	 */
	static public String getSrtmPath(Context context) {
		return get3DRServicesPath(context) + "/srtm/";
	}

}
