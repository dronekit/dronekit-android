package org.droidplanner.services.android;

import android.app.Application;
import android.content.Context;
import android.os.SystemClock;

import com.MAVLink.Messages.MAVLinkMessage;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.communication.service.MAVLinkClient;
import org.droidplanner.services.android.communication.service.UploaderService;
import org.droidplanner.services.android.location.FusedLocation;
import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.services.android.utils.file.IO.ExceptionWriter;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;

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
		GAUtils.startNewSession(getApplicationContext());

		// Any time the application is started, do a quick scan to see if we
		// need any uploads
		startService(UploaderService.createIntent(this));
	}
}
