package org.droidplanner.services.android;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.services.android.utils.file.IO.ExceptionWriter;

public class DroidPlannerServicesApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

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
}
