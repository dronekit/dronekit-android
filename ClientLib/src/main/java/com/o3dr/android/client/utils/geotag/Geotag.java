package com.o3dr.android.client.utils.geotag;

import android.content.Context;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;

import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.o3dr.android.client.data.tlog.TLogParser;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Geotag images based on camera mavlink messages.
 */
public class Geotag {
    private Geotag() {
    }

    private static ExecutorService getInstance() {
        return InitializeExecutorService.executorService;
    }

    private static class InitializeExecutorService {
        private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Asynchronous method to geotag a list of images using a list of Events as coordinate data.
     *
     * Warning: this copies data to external storage
     *
     * @param context  {@link Context}
     * @param handler  {@link Handler} Handler to specify what thread to callback on. This cannot be null.
     * @param events   {@link List<com.o3dr.android.client.data.tlog.TLogParser.Event>} list of events to geotag photos.
     * @param photos   {@link List<File>} list of files of photos to geotag.
     * @param callback {@link Geotag.GeoTagCallback} callback to send results to.
     */
    public static void geoTagImagesAsync(final Context context, final Handler handler, final List<TLogParser.Event> events,
                                         final ArrayList<File> photos, final GeoTagCallback callback) {

        getInstance().execute(new Runnable() {
            @Override
            public void run() {
                File saveDir = new File(getSaveRootDir(context).getPath(), "GeoTag");

                if (!saveDir.exists() && !saveDir.mkdir()) {
                    sendFailed(handler, callback, new IllegalStateException("Failed to create directory for images"));
                    return;
                }

                GeoTagAlgorithm geoTagAlgorithm = new GeoTagAlgorithmImpl();
                HashMap<TLogParser.Event, File> matchedPhotos = geoTagAlgorithm.match(events, photos);

                if (!hasEnoughMemory(saveDir.getPath(), matchedPhotos.values())) {
                    sendFailed(handler, callback, new IllegalStateException("Insufficient external storage space."));
                    return;
                }

                HashMap<File, File> geoTaggedFiles = new HashMap<>();
                HashMap<File, Exception> failedFiles = new HashMap<>();

                for (Map.Entry<TLogParser.Event, File> entry : matchedPhotos.entrySet()) {
                    File photo = entry.getValue();

                    File newFile = new File(saveDir.getPath(), photo.getName());
                    try {
                        copyFile(photo.getPath(), newFile.getPath());
                        updateExif(entry.getKey(), newFile);
                        geoTaggedFiles.put(photo, newFile);
                    } catch (Exception e) {
                        failedFiles.put(photo, e);
                    }
                }

                sendResult(handler, callback, geoTaggedFiles, failedFiles);
            }
        });
    }

    private static File getSaveRootDir(Context context) {
        File saveDir = context.getExternalFilesDir(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File dirs[] = context.getExternalFilesDirs(null);
            for (File dir : dirs) {
                if (Environment.isExternalStorageRemovable(dir)) {
                    saveDir = dir;
                    break;
                }
            }
        }
        return saveDir;
    }

    private static boolean hasEnoughMemory(String filePath, Collection<File> photos) {
        StatFs stat = new StatFs(filePath);
        long freeBytes = stat.getAvailableBytes();
        long bytesNeeded = 0;
        for (File photo : photos) {
            bytesNeeded += photo.length();
        }

        if (bytesNeeded > freeBytes) {
            return false;
        }
        return true;
    }

    private static void copyFile(String inputPath, String outputPath) throws IOException {
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

    private static void sendResult(Handler handler, final GeoTagCallback geoTagCallback, final HashMap<File, File> geoTaggedPhotos,
                                   final HashMap<File, Exception> failedFiles) {
        if (geoTagCallback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    geoTagCallback.onResult(geoTaggedPhotos, failedFiles);
                }
            });
        }
    }

    private static void sendFailed(Handler handler, final GeoTagCallback geoTagCallback, final Exception exception) {
        if (geoTagCallback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    geoTagCallback.onFailed(exception);
                }
            });
        }
    }

    private static void updateExif(TLogParser.Event event, File photoFile) throws IOException {
        msg_camera_feedback msg = ((msg_camera_feedback) event.getMavLinkMessage());
        double lat = (double) msg.lat / 10000000;
        double lng = (double) msg.lng / 10000000;
        String alt = String.valueOf(msg.alt_msl);

        ExifInterface newExif = new ExifInterface(photoFile.getPath());
        newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertLatLngToDMS(lng));
        newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertLatLngToDMS(lat));
        newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, lat < 0 ? "S" : "N");
        newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lng < 0 ? "W" : "E");
        newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, alt);
        newExif.saveAttributes();
    }

    private static final String convertLatLngToDMS(double coord) {

        coord = Math.abs(coord);
        int degree = (int) coord;
        coord *= 60;
        coord -= (degree * 60.0d);
        int minute = (int) coord;
        coord *= 60;
        coord -= (minute * 60.0d);
        int second = (int) (coord * 1000.0d);

        return String.format("%s/1,%s/1,%s/1000", degree, minute, second);
    }


    private interface GeoTagAlgorithm {
        HashMap<TLogParser.Event, File> match(List<TLogParser.Event> events, ArrayList<File> photos);
    }

    /**
     * Callback for asynchronous geotagging using {@link Geotag#geoTagImagesAsync(Context, Handler, List, ArrayList, GeoTagCallback)}
     */
    public interface GeoTagCallback {
        /**
         * Callback for successful geotagging
         *
         * @param geoTaggedPhotos {@link HashMap<File, File>} map of files sent in to the geotagged files.
         * @param failedFiles     {@link HashMap<File, Exception>} map of files sent in to exception that occurred when geotagging.
         */
        void onResult(HashMap<File, File> geoTaggedPhotos, HashMap<File, Exception> failedFiles);

        /**
         * Callback for failure in geotagging
         *
         * @param e {@link Exception}
         */
        void onFailed(Exception e);
    }

    private static class GeoTagAlgorithmImpl implements GeoTagAlgorithm {
        @Override
        public HashMap<TLogParser.Event, File> match(List<TLogParser.Event> events, ArrayList<File> photos) {
            HashMap<TLogParser.Event, File> matchedMap = new HashMap<>();

            int eventsSize = events.size();
            int photosSize = photos.size();

            for (int i = eventsSize - 1, j = photosSize - 1; i >= 0 && j >= 0; i--, j--) {
                matchedMap.put(events.get(i), photos.get(j));
            }

            return matchedMap;
        }
    }
}
