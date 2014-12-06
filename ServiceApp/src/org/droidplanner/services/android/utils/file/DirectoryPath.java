package org.droidplanner.services.android.utils.file;

import java.io.File;

import android.os.Environment;

public class DirectoryPath {

	/**
	 * Main path used to store files related to the program
	 * 
	 * @return Path to DroidPlanner/ folder in external storage
	 */
	static public String get3DRServicesPath() {
		String root = Environment.getExternalStorageDirectory().getPath();
		return (root + "/3DRServices/");
	}

	/**
	 * Storage folder for Parameters
	 */
	static public String getParametersPath() {
		return get3DRServicesPath() + "/Parameters/";
	}

	/**
	 * Storage folder for mission files
	 */
	static public String getWaypointsPath() {
		return get3DRServicesPath() + "/Waypoints/";
	}

	/**
	 * Folder where telemetry log files are stored
	 */
	static public File getTLogPath() {
		File f = new File(get3DRServicesPath() + "/Logs/");
		f.mkdirs();
		return f;
	}

	/**
	 * After tlogs are uploaded they get moved to this directory
	 */
	static public File getSentPath() {
		File f = new File(getTLogPath() + "/Sent/");
		f.mkdirs();
		return f;
	}

	/**
	 * Storage folder for user map tiles
	 */
	static public String getMapsPath() {
		return get3DRServicesPath() + "/Maps/";
	}

	/**
	 * Storage folder for user camera description files
	 */
	public static String getCameraInfoPath() {
		return get3DRServicesPath() + "/CameraInfo/";
	}

	/**
	 * Storage folder for stacktraces
	 */
	public static String getLogCatPath() {
		return get3DRServicesPath() + "/LogCat/";
	}

	/**
	 * Storage folder for SRTM data
	 */
	static public String getSrtmPath() {
		return get3DRServicesPath() + "/Srtm/";
	}

}
