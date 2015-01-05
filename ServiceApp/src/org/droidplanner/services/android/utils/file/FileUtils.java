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

	public static File[] getCameraInfoFileList(Context context) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(CAMERA_FILENAME_EXT);
			}
		};
		return getFileList(DirectoryPath.getCameraInfoPath(context), filter);
	}

    public static File[] getTLogFileList(Context context) {
        return getFileList(DirectoryPath.getTLogPath(context).getPath(), new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.contains(TLOG_FILENAME_EXT);
            }
        });
    }

	static public File[] getFileList(String path, FilenameFilter filter) {
		File mPath = new File(path);
        if(!mPath.exists())
            return new File[0];

        return mPath.listFiles(filter);
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
    static public File getTLogFile(Context context, String tlogPrefix) {
        File myDir = DirectoryPath.getTLogPath(context);
        return new File(myDir, tlogPrefix + TLOG_FILENAME_EXT);
    }

    /**
     * Timestamp for logs in the Mission Planner Format
     */
    static public String getTimeStamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        return sdf.format(new Date(timestamp));
    }

    static public String getTimeStamp() {
        return getTimeStamp(System.currentTimeMillis());
    }
}
