package org.droidplanner.services.android.communication.connection;

import android.content.Context;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;
import org.droidplanner.core.model.Logger;
import org.droidplanner.services.android.data.SessionDB;
import org.droidplanner.services.android.utils.AndroidLogger;
import org.droidplanner.services.android.utils.file.FileUtils;

import java.io.File;
import java.util.Date;

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
}
