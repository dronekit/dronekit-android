package org.droidplanner.services.android.communication.connection;

import java.io.File;
import java.io.IOException;

import org.droidplanner.services.android.utils.AndroidLogger;
import org.droidplanner.services.android.utils.file.FileStream;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.model.Logger;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class AndroidMavLinkConnection extends MavLinkConnection {

	private static final String TAG = AndroidMavLinkConnection.class.getSimpleName();

	protected final Context mContext;

	public AndroidMavLinkConnection(Context applicationContext) {
		this.mContext = applicationContext;
	}

	@Override
	protected final Logger initLogger() {
		return AndroidLogger.getLogger();
	}

	@Override
	protected final File getTempTLogFile() {
		return FileStream.getTLogFile();
	}

	@Override
	protected final void commitTempTLogFile(File tlogFile) {
		FileStream.commitFile(tlogFile);
	}
}
