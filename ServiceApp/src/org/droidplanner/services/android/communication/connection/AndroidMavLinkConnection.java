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

    protected static final String TLOG_PREFIX = "log";

    protected final Context mContext;
    private final SessionDB sessionDB;

    public AndroidMavLinkConnection(Context applicationContext) {
        this.mContext = applicationContext;
        this.sessionDB = new SessionDB(applicationContext);
    }

    @Override
    protected final Logger initLogger() {
        return AndroidLogger.getLogger();
    }

    @Override
    protected final File getTempTLogFile(long connectionTimestamp) {
        return FileUtils.getTLogFile(mContext, getTLogPrefix() + "_" + FileUtils.getTimeStamp(connectionTimestamp));
    }

    protected String getTLogPrefix() {
        return TLOG_PREFIX + "_" + MavLinkConnectionTypes.getConnectionTypeLabel(getConnectionType());
    }

    @Override
    protected void reportConnect() {
        super.reportConnect();

        //log into the database the connection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(getConnectionType());
        this.sessionDB.startSession(new Date(getConnectionTime()), connectionType);
    }

    @Override
    protected void reportDisconnect() {
        super.reportDisconnect();

        //log into the database the disconnection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(getConnectionType());
        this.sessionDB.endSession(new Date(getConnectionTime()), connectionType, new Date());
    }
}
