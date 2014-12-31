package org.droidplanner.services.android.utils.file;

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

	public static String[] getCameraInfoFileList(Context context) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(CAMERA_FILENAME_EXT);
			}
		};
		return getFileList(DirectoryPath.getCameraInfoPath(context), filter);
	}

    public static String[] getTLogFileList(Context context) {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.contains(TLOG_FILENAME_EXT);
            }
        };
        return getFileList(DirectoryPath.getTLogPath(context).getPath(), filter);
    }

	static public String[] getFileList(String path, FilenameFilter filter) {
		File mPath = new File(path);
		try {
			mPath.mkdirs();
			if (mPath.exists()) {
				return mPath.list(filter);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return new String[0];
	}

    public static FileOutputStream getExceptionFileStream(Context context) throws FileNotFoundException {
        File myDir = new File(DirectoryPath.getLogCatPath(context));
        if (!myDir.exists())
            myDir.mkdirs();

        File file = new File(myDir, getTimeStamp() + ".txt");
        if (file.exists())
            file.delete();
        return new FileOutputStream(file);
    }

    /**
     * Return a filename that is suitable for a tlog
     *
     * @return
     * @throws java.io.FileNotFoundException
     */
    static public File getTLogFile(Context context) {
        File myDir = DirectoryPath.getTLogPath(context);
        return new File(myDir, getTimeStamp() + TLOG_FILENAME_EXT);
    }

    /**
     * Timestamp for logs in the Mission Planner Format
     */
    static public String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        return sdf.format(new Date());
    }
}
