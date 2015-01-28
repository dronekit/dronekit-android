package org.droidplanner.services.android.communication.service;

import android.content.Context;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;
import org.droidplanner.services.android.api.MavLinkServiceApi;
import org.droidplanner.services.android.data.SessionDB;
import org.droidplanner.services.android.utils.file.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Provide a common class for some ease of use functionality
 */
public class MAVLinkClient implements MAVLinkStreams.MAVLinkOutputStream {

    private final static String TAG = MAVLinkClient.class.getSimpleName();

    /**
     * Maximum possible sequence number for a packet.
     */
    private static final int MAX_PACKET_SEQUENCE = 255;

    private final MavLinkConnectionListener mConnectionListener = new MavLinkConnectionListener() {

        @Override
        public void onConnect(long connectionTime) {
            startLoggingThread(connectionTime);
            listener.notifyConnected();
        }

        @Override
        public void onReceivePacket(final MAVLinkPacket packet) {
            listener.notifyReceivedData(packet);
            queueToLog(packet.encodePacket());
        }

        @Override
        public void onDisconnect(long disconnectTime) {
            listener.notifyDisconnected();
            closeConnection();
        }

        @Override
        public void onComError(final String errMsg) {
            if (errMsg != null) {
                listener.onStreamError(errMsg);
            }
        }
    };

    private static final String TLOG_PREFIX = "log";
    private static final String TEMP_TLOG_EXT = ".tmp";

    /**
     * Queue the set of packets to log. A thread will be blocking on it until
     * there's element(s) available for logging.
     */
    private final LinkedBlockingQueue<byte[]> mPacketsToLog = new LinkedBlockingQueue<>();

    /**
     * Blocks until there's packets to log, then dispatch them.
     */
    private final Runnable mLoggingTask = new Runnable() {
        @Override
        public void run() {
            final File tmpLogFile = getTempTLogFile(connectionTime);
            final ByteBuffer logBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
            logBuffer.order(ByteOrder.BIG_ENDIAN);

            try {
                final BufferedOutputStream logWriter = new BufferedOutputStream(new FileOutputStream(tmpLogFile));
                try {
                    while (isConnected()) {
                        final byte[] packetData = mPacketsToLog.take();

                        logBuffer.clear();
                        logBuffer.putLong(System.currentTimeMillis() * 1000);

                        logWriter.write(logBuffer.array());
                        logWriter.write(packetData);
                    }
                } catch (InterruptedException e) {
                    final String errorMessage = e.getMessage();
                    if(errorMessage != null)
                        Log.v(TAG, errorMessage);
                } finally {
                    logWriter.close();

                    //Rename the file.
                    tmpLogFile.renameTo(getCompleteTLogFile(tmpLogFile));
                }
            } catch (IOException e) {
                final String errorMessage = e.getMessage();
                if(errorMessage != null)
                    Log.e(TAG, errorMessage, e);
            }
        }
    };

    private final File loggingDir;

    private Thread loggingThread;
    private long connectionTime;
    private int packetSeqNumber = 0;

    private ConnectionParameter connParams;
    private final MAVLinkStreams.MavlinkInputStream listener;

    private final MavLinkServiceApi mavLinkApi;
    private final SessionDB sessionDB;

    public MAVLinkClient(Context context, MAVLinkStreams.MavlinkInputStream listener, MavLinkServiceApi serviceApi,
                         File logDir) {
        this.listener = listener;
        this.mavLinkApi = serviceApi;
        this.loggingDir = logDir;
        this.sessionDB = new SessionDB(context);
    }

    public ConnectionParameter getConnectionParameter() {
        return connParams;
    }

    public void setConnectionParameter(ConnectionParameter connParams) {
        boolean isConnected = isConnected();
        if(isConnected)
            closeConnection();

        this.connParams = connParams;

        if(isConnected)
            openConnection();
    }

    @Override
    public void openConnection() {
        if(this.connParams == null)
            return;

        final String tag = toString();
        if(mavLinkApi.getConnectionStatus(this.connParams, tag) == MavLinkConnection.MAVLINK_DISCONNECTED) {
            mavLinkApi.connectMavLink(this.connParams, tag, mConnectionListener);
        }
    }

    @Override
    public void closeConnection() {
        if(this.connParams == null)
            return;

        final String tag = toString();
        if (mavLinkApi.getConnectionStatus(this.connParams, tag) == MavLinkConnection.MAVLINK_CONNECTED) {
            mavLinkApi.disconnectMavLink(this.connParams, tag);
            stopLoggingThread(System.currentTimeMillis());
            listener.notifyDisconnected();
        }
    }

    @Override
    public void sendMavPacket(MAVLinkPacket pack) {
        if (!isConnected()) {
            return;
        }

        pack.seq = packetSeqNumber;
        mavLinkApi.sendData(this.connParams, pack);
        queueToLog(pack.encodePacket());

        packetSeqNumber = (packetSeqNumber + 1) % (MAX_PACKET_SEQUENCE + 1);
    }

    @Override
    public boolean isConnected() {
        return this.connParams != null
                && mavLinkApi.getConnectionStatus(this.connParams, toString()) == MavLinkConnection.MAVLINK_CONNECTED;
    }

    @Override
    public void toggleConnectionState() {
        if (isConnected()) {
            closeConnection();
        } else {
            openConnection();
        }
    }

    private File getTempTLogFile(long connectionTimestamp) {
        return new File(loggingDir, getTLogFilename(connectionTimestamp));
    }

    private File getCompleteTLogFile(File tempFile){
        return new File(tempFile.getParentFile(), tempFile.getName().replace(TEMP_TLOG_EXT, ""));
    }

    private String getTLogFilename(long connectionTimestamp) {
        return TLOG_PREFIX + "_" + MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType()) +
                "_" + FileUtils.getTimeStamp(connectionTimestamp) + FileUtils.TLOG_FILENAME_EXT + TEMP_TLOG_EXT;
    }

    private void startLoggingThread(long startTime){
        this.connectionTime = startTime;

        //log into the database the connection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType());
        this.sessionDB.startSession(new Date(connectionTime), connectionType);

        loggingThread = new Thread(mLoggingTask, "MavLinkClient-Logging Thread");
        loggingThread.start();
    }

    private void stopLoggingThread(long stopTime){
        //log into the database the disconnection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType());
        this.sessionDB.endSession(new Date(stopTime), connectionType, new Date());

        if (loggingThread != null && loggingThread.isAlive()) {
            loggingThread.interrupt();
            loggingThread = null;
        }
    }

    /**
     * Queue a mavlink packet for logging.
     *
     * @param packetData MAVLinkPacket packet data
     * @return true if the packet was queued successfully.
     */
    private boolean queueToLog(byte[] packetData) {
        return mPacketsToLog.offer(packetData);
    }
}
