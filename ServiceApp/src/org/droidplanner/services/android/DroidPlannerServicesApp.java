package org.droidplanner.services.android;

import android.app.Application;

import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.services.android.utils.file.IO.ExceptionWriter;

public class DroidPlannerServicesApp extends Application {

    /**
     * Handles dispatching of status bar, and audible notification.
     */
    private Thread.UncaughtExceptionHandler exceptionHandler;

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            new ExceptionWriter(ex).saveStackTraceToSD();
            exceptionHandler.uncaughtException(thread, ex);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);

        GAUtils.initGATracker(this);
        GAUtils.startNewSession(null);
    }
}
