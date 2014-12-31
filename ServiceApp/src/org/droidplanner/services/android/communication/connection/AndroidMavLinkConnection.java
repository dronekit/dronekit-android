package org.droidplanner.services.android.communication.connection;

import android.content.Context;

import com.MAVLink.MAVLinkPacket;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;
import org.droidplanner.core.model.Logger;
import org.droidplanner.services.android.databases.SessionDB;
import org.droidplanner.services.android.databases.TLogDB;
import org.droidplanner.services.android.utils.AndroidLogger;
import org.droidplanner.services.android.utils.file.FileStream;

import java.io.File;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AndroidMavLinkConnection extends MavLinkConnection {

    private static final String TAG = AndroidMavLinkConnection.class.getSimpleName();

    protected final Context mContext;

    /**
     * Blocks until there's packets to log, then dispatch them.
     */
    private final Runnable mLoggingTask = new Runnable() {
        @Override
        public void run() {
            final Logger logger = getLogger();
            final LinkedBlockingQueue<MAVLinkPacket> packetsToLog = getPacketsToLog();
            final TLogDB tlogDb = new TLogDB(mContext, connectionDate);

            try {
                while (true) {
                    final MAVLinkPacket packet = packetsToLog.take();
                    tlogDb.insertTLogData(connectionDate, System.currentTimeMillis(), packet);
                }
            } catch (InterruptedException e) {
                logger.logVerbose(TAG, e.getMessage());
            }
        }
    };

    private Date connectionDate;
    private SessionDB sessionDB;

    public AndroidMavLinkConnection(Context applicationContext) {
        this.mContext = applicationContext;
        this.sessionDB = new SessionDB(applicationContext);
    }

    @Override
    protected final Logger initLogger() {
        return AndroidLogger.getLogger();
    }

    @Override
    protected Runnable getLoggingTask() {
        return mLoggingTask;
    }

    @Override
    protected final File getTempTLogFile() {
        return FileStream.getTLogFile();
    }

    @Override
    protected final void commitTempTLogFile(File tlogFile) {
        FileStream.commitFile(tlogFile);
    }

    @Override
    protected void reportConnect(){
        super.reportConnect();

        //log into the database the connection time.
        this.connectionDate = new Date();
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(getConnectionType());
        this.sessionDB.startSession(connectionDate, connectionType);
    }

    @Override
    protected void reportDisconnect(){
        super.reportDisconnect();

        //log into the database the disconnection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(getConnectionType());
        this.sessionDB.endSession(connectionDate, connectionType, new Date());
        this.connectionDate = null;
    }
}
