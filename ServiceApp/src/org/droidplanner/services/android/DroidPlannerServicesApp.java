package org.droidplanner.services.android;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import org.droidplanner.services.android.utils.LogToFileTree;
import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.services.android.utils.file.IO.ExceptionWriter;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class DroidPlannerServicesApp extends Application {

    private LogToFileTree logToFileTree;

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.ENABLE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }

        if (BuildConfig.WRITE_LOG_FILE) {
            logToFileTree = new LogToFileTree();
            Timber.plant(logToFileTree);
        } else if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        final ExceptionWriter exceptionWriter = new ExceptionWriter(getApplicationContext());
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        final Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                exceptionWriter.saveStackTraceToSD(ex);
                defaultHandler.uncaughtException(thread, ex);
            }
        };

        Thread.setDefaultUncaughtExceptionHandler(handler);

        GAUtils.initGATracker(this);
        GAUtils.startNewSession(null);
    }

    public void createFileStartLogging() {
        if (logToFileTree != null) {
            logToFileTree.createFileStartLogging(getApplicationContext());
        }
    }

    public void closeLogFile() {
        if(logToFileTree != null) {
            logToFileTree.stopLoggingThread();
        }
    }
}
