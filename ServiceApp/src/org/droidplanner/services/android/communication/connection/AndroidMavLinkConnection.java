package org.droidplanner.services.android.communication.connection;

import android.content.Context;

import com.MAVLink.MAVLinkPacket;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.model.Logger;
import org.droidplanner.services.android.databases.TLogDB;
import org.droidplanner.services.android.utils.AndroidLogger;
import org.droidplanner.services.android.utils.file.FileStream;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

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
            final TLogDB tlogDb = new TLogDB(mContext);

            try {
                while (true) {
                    final MAVLinkPacket packet = packetsToLog.take();
                    tlogDb.insertTLogData(getConnectionTime(), System.currentTimeMillis(), packet);
                }
            } catch (InterruptedException e) {
                logger.logVerbose(TAG, e.getMessage());
            }
        }
    };

    public AndroidMavLinkConnection(Context applicationContext) {
        this.mContext = applicationContext;
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
}
