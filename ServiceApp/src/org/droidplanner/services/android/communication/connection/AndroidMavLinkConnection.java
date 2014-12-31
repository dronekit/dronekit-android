package org.droidplanner.services.android.communication.connection;

import android.content.Context;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;
import org.droidplanner.core.model.Logger;
import org.droidplanner.services.android.databases.SessionDB;
import org.droidplanner.services.android.utils.AndroidLogger;
import org.droidplanner.services.android.utils.file.FileUtils;

import java.io.File;
import java.util.Date;

public abstract class AndroidMavLinkConnection extends MavLinkConnection {

    private static final String TAG = AndroidMavLinkConnection.class.getSimpleName();

    protected final Context mContext;

    private Date connectionDate;
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
    protected final File getTempTLogFile() {
        return FileUtils.getTLogFile(mContext);
    }

    @Override
    protected void reportConnect() {
        super.reportConnect();

        //log into the database the connection time.
        this.connectionDate = new Date();
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(getConnectionType());
        this.sessionDB.startSession(connectionDate, connectionType);
    }

    @Override
    protected void reportDisconnect() {
        super.reportDisconnect();

        //log into the database the disconnection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(getConnectionType());
        this.sessionDB.endSession(connectionDate, connectionType, new Date());
        this.connectionDate = null;
    }
}
