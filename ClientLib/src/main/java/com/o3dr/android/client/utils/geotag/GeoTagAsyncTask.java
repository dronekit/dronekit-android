package com.o3dr.android.client.utils.geotag;

import android.content.Context;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.o3dr.android.client.utils.data.tlog.TLogParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GeoTagAsyncTask images based on camera mavlink messages.
 */
public abstract class GeoTagAsyncTask extends AsyncTask<Void, Integer, GeoTagAsyncTask.ResultObject> {
    private static final String STORE_PHOTO_DIR_NAME = "GeoTag";
    private final Context context;
    private final List<TLogParser.Event> events;
    private final ArrayList<File> photos;

    /**
     * Asynchronous method to geotag a list of images using a list of Events as coordinate data.
     *
     * Warning: this copies data to external storage
     *
     * @param context  {@link Context}
     * @param events   {@link List<com.o3dr.android.client.utils.data.tlog.TLogParser.Event>} list of events to geotag photos.
     * @param photos   {@link List<File>} list of files of photos to geotag.
     */
    public GeoTagAsyncTask(Context context, List<TLogParser.Event> events, ArrayList<File> photos) {
        this.context = context;
        this.events = events;
        this.photos = photos;
    }

    @Override
    protected ResultObject doInBackground(Void... params) {
        ResultObject resultObject = new ResultObject();

        try {
            File saveDir = new File(getSaveRootDir(context), STORE_PHOTO_DIR_NAME);

            HashMap<File, File> geoTaggedFiles = new HashMap<>();
            HashMap<File, Exception> failedFiles = new HashMap<>();
            resultObject.setResult(geoTaggedFiles, failedFiles);

            if (isCancelled()) {
                return resultObject;
            }

            if (!saveDir.exists() && !saveDir.mkdir()) {
                resultObject.setException(new IllegalStateException("Failed to create directory for images"));
                return resultObject;
            }

            if (isCancelled()) {
                return resultObject;
            }
            GeoTagAlgorithm geoTagAlgorithm = new GeoTagAlgorithmImpl();
            HashMap<TLogParser.Event, File> matchedPhotos = geoTagAlgorithm.match(events, photos);

            if (isCancelled()) {
                return resultObject;
            }
            if (!hasEnoughMemory(saveDir, matchedPhotos.values())) {
                resultObject.setException(new IllegalStateException("Insufficient external storage space."));
                return resultObject;
            }

            int numTotal = matchedPhotos.size();
            int numProcessed = 0;
            for (Map.Entry<TLogParser.Event, File> entry : matchedPhotos.entrySet()) {
                if (isCancelled()) {
                    return resultObject;
                }
                File photo = entry.getValue();

                File newFile = new File(saveDir, photo.getName());
                try {
                    copyFile(photo, newFile);
                    updateExif(entry.getKey(), newFile);
                    geoTaggedFiles.put(photo, newFile);
                } catch (Exception e) {
                    failedFiles.put(photo, e);
                }

                numProcessed++;
                publishProgress(numProcessed, numTotal);
            }

        } catch (Exception e) {
            resultObject.setException(e);
        }

        return resultObject;
    }

    @Override
    protected final void onPostExecute(ResultObject resultObject) {
        if (resultObject.didSucceed()) {
            onResult(resultObject.getGeoTaggedPhotos(), resultObject.getFailedFiles());
        } else {
            onFailed(resultObject.getException());
        }
    }

    @Override
    protected final void onProgressUpdate(Integer... values) {
        onProgress(values[0], values[1]);
    }

    @Override
    protected final void onCancelled(ResultObject resultObject) {
        onResult(resultObject.getGeoTaggedPhotos(), resultObject.getFailedFiles());
    }

    /**
     * Callback for successful geotagging
     *
     * @param geoTaggedPhotos {@link HashMap<File, File>} map of files sent in to the geotagged files.
     * @param failedFiles     {@link HashMap<File, Exception>} map of files sent in to exception that occurred when geotagging.
     */
    public abstract void onResult(HashMap<File, File> geoTaggedPhotos, HashMap<File, Exception> failedFiles);

    /**
     * Callback to notify when as items are processed
     *
     * @param numProcessed number of items that have been processed.
     * @param numTotal     total number of items that will be processed for geotagging
     */
    public abstract void onProgress(int numProcessed, int numTotal);

    /**
     * Callback for exception in geotagging
     *
     * @param e {@link Exception}
     */
    public abstract void onFailed(Exception e);


    protected static class ResultObject {
        private boolean didSucceed;
        private HashMap<File, File> geoTaggedPhotos;
        private HashMap<File, Exception> failedFiles;
        private Exception exception;

        public boolean didSucceed() {
            return didSucceed;
        }

        public void setResult(HashMap<File, File> geoTaggedPhotos, HashMap<File, Exception> failedFiles) {
            didSucceed = true;
            this.geoTaggedPhotos = geoTaggedPhotos;
            this.failedFiles = failedFiles;
        }

        public HashMap<File, File> getGeoTaggedPhotos() {
            return geoTaggedPhotos;
        }

        public HashMap<File, Exception> getFailedFiles() {
            return failedFiles;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            didSucceed = false;
            this.exception = exception;
        }
    }

    private static File getSaveRootDir(Context context) {
        File saveDir = context.getExternalFilesDir(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File dirs[] = context.getExternalFilesDirs(null);
            for (File dir : dirs) {
                // dir can be null if the device contains an external SD card slot but no SD card is present.
                if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                    saveDir = dir;
                    break;
                }
            }
        }
        return saveDir;
    }

    private static boolean hasEnoughMemory(File file, Collection<File> photos) {
        long freeBytes = file.getUsableSpace();
        long bytesNeeded = 0;
        for (File photo : photos) {
            bytesNeeded += photo.length();
        }

        if (bytesNeeded > freeBytes) {
            return false;
        }
        return true;
    }

    private static void copyFile(File inputPath, File outputPath) throws IOException {
        InputStream in = new FileInputStream(inputPath);
        OutputStream out = new FileOutputStream(outputPath);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();

        // write the output file (You have now copied the file)
        out.flush();
        out.close();
    }

    private static void updateExif(TLogParser.Event event, File photoFile) throws IOException {
        msg_camera_feedback msg = ((msg_camera_feedback) event.getMavLinkMessage());
        double lat = (double) msg.lat / 10000000;
        double lng = (double) msg.lng / 10000000;
        String alt = String.valueOf(msg.alt_msl);

        ExifInterface exifInterface = new ExifInterface(photoFile.getPath());
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertLatLngToDMS(lng));
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertLatLngToDMS(lat));
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, lat < 0 ? "S" : "N");
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lng < 0 ? "W" : "E");
        exifInterface.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, alt);
        exifInterface.saveAttributes();
    }

    private static String convertLatLngToDMS(double coord) {
        double dDegree = Math.abs(coord);
        int degree = (int) dDegree;

        double dMinute = (dDegree - degree) * 60;
        int minute = (int) dMinute;

        double dSecond = (dMinute - minute) * 60;
        int second = (int) (dSecond * 1000);

        return String.format("%s/1,%s/1,%s/1000", degree, minute, second);
    }

    protected interface GeoTagAlgorithm {
        HashMap<TLogParser.Event, File> match(List<TLogParser.Event> events, ArrayList<File> photos);
    }
}
