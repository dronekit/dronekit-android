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

public class DroidPlannerServicesApp extends Application implements
		MAVLinkStreams.MavlinkInputStream {

	private Drone drone;
	private Follow followMe;
	private MavLinkMsgHandler mavLinkMsgHandler;
	private DroidPlannerPrefs prefs;
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

		final Context context = getApplicationContext();

		MAVLinkClient MAVClient = new MAVLinkClient(this, this);
		Clock clock = new Clock() {
			@Override
			public long elapsedRealtime() {
				return SystemClock.elapsedRealtime();
			}
		};
		Handler handler = new Handler() {
			android.os.Handler handler = new android.os.Handler();

			@Override
			public void removeCallbacks(Runnable thread) {
				handler.removeCallbacks(thread);
			}

			@Override
			public void post(Runnable thread) {
				handler.post(thread);
			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				handler.postDelayed(thread, timeout);
			}
		};

		prefs = new DroidPlannerPrefs(context);
		drone = new DroneImpl(MAVClient, clock, handler, prefs);

		mavLinkMsgHandler = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(getDrone());

		followMe = new Follow(getDrone(), handler, new FusedLocation(context));

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

		// Any time the application is started, do a quick scan to see if we
		// need any uploads
		startService(UploaderService.createIntent(this));
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyConnected() {
		getDrone().notifyDroneEvent(DroneEventsType.CONNECTED);
	}

	@Override
	public void notifyDisconnected() {
		getDrone().notifyDroneEvent(DroneEventsType.DISCONNECTED);
	}

	public DroidPlannerPrefs getPreferences() {
		return prefs;
	}

	public Drone getDrone() {
		return drone;
	}

	public Follow getFollowMe() {
		return followMe;
	}

}
