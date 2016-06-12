package org.droidplanner.services.android.impl.utils.file;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {

    public static final String CAMERA_FILENAME_EXT = ".xml";

    public static final String TLOG_FILENAME_EXT = ".tlog";

	public static File[] getCameraInfoFileList(Context context) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(CAMERA_FILENAME_EXT);
			}
		};
		return getFileList(DirectoryPath.getCameraInfoPath(context), filter);
	}

	private static File[] getFileList(String path, FilenameFilter filter) {
		File mPath = new File(path);
        if(!mPath.exists())
            return new File[0];

        return mPath.listFiles(filter);
	}

    public static FileOutputStream getExceptionFileStream(Context context) throws FileNotFoundException {
        File myDir = new File(DirectoryPath.getCrashLogPath(context));
        if (!myDir.exists())
            myDir.mkdirs();

        File file = new File(myDir, getTimeStamp() + ".log");
        if (file.exists())
            file.delete();
        return new FileOutputStream(file);
    }

    /**
     * Timestamp for logs in the Mission Planner Format
     */
    static public String getTimeStamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        return sdf.format(new Date(timestamp));
    }

    private static String getTimeStamp() {
        return getTimeStamp(System.currentTimeMillis());
    }
}
