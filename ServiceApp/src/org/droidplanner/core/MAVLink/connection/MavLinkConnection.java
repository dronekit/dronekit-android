package org.droidplanner.core.MAVLink.connection;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

import org.droidplanner.core.model.Logger;
import org.droidplanner.core.util.Pair;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base for mavlink connection implementations.
 */
public abstract class MavLinkConnection {

    private static final String TAG = MavLinkConnection.class.getSimpleName();

    /*
     * MavLink connection states
     */
    public static final int MAVLINK_DISCONNECTED = 0;
    public static final int MAVLINK_CONNECTING = 1;
    public static final int MAVLINK_CONNECTED = 2;

    /**
     * Size of the buffer used to read messages from the mavlink connection.
     */
    private static final int READ_BUFFER_SIZE = 4096;

    /**
     * Set of listeners subscribed to this mavlink connection. We're using a
     * ConcurrentSkipListSet because the object will be accessed from multiple
     * threads concurrently.
     */
    private final ConcurrentHashMap<String, MavLinkConnectionListener> mListeners = new ConcurrentHashMap<>();

    /**
     * Stores the list of log files to be written to.
     */
    private final ConcurrentHashMap<String, Pair<String, BufferedOutputStream>> loggingOutStreams = new
            ConcurrentHashMap<>();

    /**
     * Queue the set of packets to send via the mavlink connection. A thread
     * will be blocking on it until there's element(s) available to send.
     */
    private final LinkedBlockingQueue<byte[]> mPacketsToSend = new LinkedBlockingQueue<>();

    /**
     * Queue the set of packets to log. A thread will be blocking on it until
     * there's element(s) available for logging.
     */
    private final LinkedBlockingQueue<byte[]> mPacketsToLog = new LinkedBlockingQueue<>();

    private final AtomicInteger mConnectionStatus = new AtomicInteger(MAVLINK_DISCONNECTED);
    private final AtomicLong mConnectionTime = new AtomicLong(-1);

    /**
     * Start the connection process.
     */
    private final Runnable mConnectingTask = new Runnable() {
        @Override
        public void run() {
            // Load the connection specific preferences
            loadPreferences();
            // Open the connection
            try {
                openConnection();
            } catch (IOException e) {
                // Ignore errors while shutting down
                if (mConnectionStatus.get() != MAVLINK_DISCONNECTED) {
                    reportComError(e.getMessage());
                    mLogger.logErr(TAG, e);
                }

                disconnect();
            }

            mLogger.logInfo(TAG, "Exiting connecting thread.");
        }
    };

    /**
     * Manages the receiving and sending of messages.
     */
    private final Runnable mManagerTask = new Runnable() {

        @Override
        public void run() {
            Thread sendingThread = null;
            Thread loggingThread = null;

            try {
                final long connectionTime = System.currentTimeMillis();
                mConnectionTime.set(connectionTime);
                reportConnect(connectionTime);

                // Launch the 'Sending' thread
                mLogger.logInfo(TAG, "Starting sender thread.");
                sendingThread = new Thread(mSendingTask, "MavLinkConnection-Sending Thread");
                sendingThread.start();

                //Launch the 'Logging' thread
                mLogger.logInfo(TAG, "Starting logging thread.");
                loggingThread = new Thread(mLoggingTask, "MavLinkConnection-Logging Thread");
                loggingThread.start();

                final Parser parser = new Parser();
                parser.stats.mavlinkResetStats();

                final byte[] readBuffer = new byte[READ_BUFFER_SIZE];

                while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
                    int bufferSize = readDataBlock(readBuffer);
                    handleData(parser, bufferSize, readBuffer);
                }
            } catch (IOException e) {
                // Ignore errors while shutting down
                if (mConnectionStatus.get() != MAVLINK_DISCONNECTED) {
                    reportComError(e.getMessage());
                    mLogger.logErr(TAG, e);
                }
            } finally {
                if (sendingThread != null && sendingThread.isAlive()) {
                    sendingThread.interrupt();
                }

                if (loggingThread != null && loggingThread.isAlive()) {
                    loggingThread.interrupt();
                }

                disconnect();
                mLogger.logInfo(TAG, "Exiting manager thread.");
            }
        }

        private void handleData(Parser parser, int bufferSize, byte[] buffer) {
            if (bufferSize < 1) {
                return;
            }

            for (int i = 0; i < bufferSize; i++) {
                MAVLinkPacket receivedPacket = parser.mavlink_parse_char(buffer[i] & 0x00ff);
                if (receivedPacket != null) {
                    queueToLog(receivedPacket);
                    reportReceivedPacket(receivedPacket);
                }
            }
        }
    };

    /**
     * Blocks until there's packet(s) to send, then dispatch them.
     */
    private final Runnable mSendingTask = new Runnable() {
        @Override
        public void run() {
            try {
                while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
                    byte[] buffer = mPacketsToSend.take();

                    try {
                        sendBuffer(buffer);
                        queueToLog(buffer);
                    } catch (IOException e) {
                        reportComError(e.getMessage());
                        mLogger.logErr(TAG, e);
                    }
                }
            } catch (InterruptedException e) {
                mLogger.logVerbose(TAG, e.getMessage());
            } finally {
                disconnect();
            }
        }
    };

    /**
     * Blocks until there's packets to log, then dispatch them.
     */
    private final Runnable mLoggingTask = new Runnable() {

        @Override
        public void run() {
            final ByteBuffer logBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
            logBuffer.order(ByteOrder.BIG_ENDIAN);

            try {
                while (mConnectionStatus.get() == MAVLINK_CONNECTED) {

                    final byte[] packetData = mPacketsToLog.take();

                    logBuffer.clear();
                    logBuffer.putLong(System.currentTimeMillis() * 1000);

                    for (Map.Entry<String, Pair<String, BufferedOutputStream>> entry : loggingOutStreams
                            .entrySet()) {
                        final Pair<String, BufferedOutputStream> logInfo = entry.getValue();
                        final String loggingFilePath = logInfo.first;
                        try {
                            BufferedOutputStream logWriter = logInfo.second;
                            if (logWriter == null) {
                                logWriter = new BufferedOutputStream(new FileOutputStream(loggingFilePath));
                                loggingOutStreams.put(entry.getKey(), Pair.create(loggingFilePath, logWriter));
                            }

                            logWriter.write(logBuffer.array());
                            logWriter.write(packetData);
                        } catch (IOException e) {
                            mLogger.logErr(TAG, "IO Exception while writing to " + loggingFilePath, e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                final String errorMessage = e.getMessage();
                if (errorMessage != null)
                    mLogger.logVerbose(TAG, errorMessage);
            } finally {
                for (Pair<String, BufferedOutputStream> entry : loggingOutStreams.values()) {
                    final String loggingFilePath = entry.first;
                    try {
                        if (entry.second != null)
                            entry.second.close();
                    } catch (IOException e) {
                        mLogger.logErr(TAG, "IO Exception while closing " + loggingFilePath, e);
                    }
                }

                loggingOutStreams.clear();
            }
        }
    };

    protected final Logger mLogger = initLogger();

    private Thread mConnectThread;
    private Thread mTaskThread;

    /**
     * Establish a mavlink connection. If the connection is successful, it will
     * be reported through the MavLinkConnectionListener interface.
     */
    public void connect() {
        if (mConnectionStatus.compareAndSet(MAVLINK_DISCONNECTED, MAVLINK_CONNECTING)) {
            mLogger.logInfo(TAG, "Starting connection thread.");
            mConnectThread = new Thread(mConnectingTask, "MavLinkConnection-Connecting Thread");
            mConnectThread.start();
            reportConnecting();
        }
    }

    protected void onConnectionOpened() {
        if (mConnectionStatus.compareAndSet(MAVLINK_CONNECTING, MAVLINK_CONNECTED)) {
            mLogger.logInfo(TAG, "Starting manager thread.");
            mTaskThread = new Thread(mManagerTask, "MavLinkConnection-Manager Thread");
            mTaskThread.start();
        }
    }

    protected void onConnectionFailed(String errMsg) {
        mLogger.logInfo(TAG, "Unable to establish connection: " + errMsg);
        reportComError(errMsg);
        disconnect();
    }

    /**
     * Disconnect a mavlink connection. If the operation is successful, it will
     * be reported through the MavLinkConnectionListener interface.
     */
    public void disconnect() {
        if (mConnectionStatus.get() == MAVLINK_DISCONNECTED || (mConnectThread == null && mTaskThread == null)) {
            return;
        }

        try {
            final long disconnectTime = System.currentTimeMillis();

            mConnectionStatus.set(MAVLINK_DISCONNECTED);
            mConnectionTime.set(-1);

            if (mConnectThread != null && mConnectThread.isAlive() && !mConnectThread.isInterrupted()) {
                mConnectThread.interrupt();
            }

            if (mTaskThread != null && mTaskThread.isAlive() && !mTaskThread.isInterrupted()) {
                mTaskThread.interrupt();
            }

            closeConnection();
            reportDisconnect(disconnectTime);
        } catch (IOException e) {
            mLogger.logErr(TAG, e);
            reportComError(e.getMessage());
        }
    }

    public int getConnectionStatus() {
        return mConnectionStatus.get();
    }

    public void sendMavPacket(MAVLinkPacket packet) {
        final byte[] packetData = packet.encodePacket();
        if (!mPacketsToSend.offer(packetData)) {
            mLogger.logErr(TAG, "Unable to send mavlink packet. Packet queue is full!");
        }
    }

    private void queueToLog(MAVLinkPacket packet) {
        if (packet != null)
            queueToLog(packet.encodePacket());
    }

    private void queueToLog(byte[] packetData) {
        if (packetData != null) {
            if (!mPacketsToLog.offer(packetData)) {
                mLogger.logErr(TAG, "Unable to log mavlink packet. Queue is full!");
            }
        }
    }

    public void addLoggingPath(String tag, String loggingPath) {
        if (tag == null || tag.length() == 0 || loggingPath == null || loggingPath.length() == 0)
            return;

        if (!loggingOutStreams.contains(tag))
            loggingOutStreams.put(tag, Pair.<String, BufferedOutputStream>create(loggingPath, null));
    }

    public void removeLoggingPath(String tag) {
        if (tag == null || tag.length() == 0)
            return;

        Pair<String, BufferedOutputStream> logInfo = loggingOutStreams.remove(tag);
        if (logInfo != null) {
            BufferedOutputStream outStream = logInfo.second;
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    mLogger.logErr(TAG, "IO Exception while closing " + logInfo.first, e);
                }
            }
        }
    }

    /**
     * Adds a listener to the mavlink connection.
     *
     * @param listener
     * @param tag      Listener tag
     */
    public void addMavLinkConnectionListener(String tag, MavLinkConnectionListener listener) {
        mListeners.put(tag, listener);

        if (getConnectionStatus() == MAVLINK_CONNECTED) {
            listener.onConnect(mConnectionTime.get());
        }
    }

    /**
     * @return the count of connection listeners.
     */
    public int getMavLinkConnectionListenersCount() {
        return mListeners.size();
    }

    /**
     * Used to query the presence of a connection listener.
     *
     * @param tag connection listener tag
     * @return true if the tag is present in the listeners list.
     */
    public boolean hasMavLinkConnectionListener(String tag) {
        return mListeners.containsKey(tag);
    }

    /**
     * Removes the specified listener.
     *
     * @param tag Listener tag
     */
    public void removeMavLinkConnectionListener(String tag) {
        mListeners.remove(tag);
    }

    /**
     * Removes all the connection listeners.
     */
    public void removeAllMavLinkConnectionListeners() {
        mListeners.clear();
    }

    protected abstract Logger initLogger();

    protected abstract void openConnection() throws IOException;

    protected abstract int readDataBlock(byte[] buffer) throws IOException;

    protected abstract void sendBuffer(byte[] buffer) throws IOException;

    protected abstract void closeConnection() throws IOException;

    protected abstract void loadPreferences();

    /**
     * @return The type of this mavlink connection.
     */
    public abstract int getConnectionType();

    protected Logger getLogger() {
        return mLogger;
    }

    /**
     * Utility method to notify the mavlink listeners about communication
     * errors.
     *
     * @param errMsg
     */
    protected void reportComError(String errMsg) {
        if (mListeners.isEmpty())
            return;

        for (MavLinkConnectionListener listener : mListeners.values()) {
            listener.onComError(errMsg);
        }
    }

    protected void reportConnecting() {
        for (MavLinkConnectionListener listener : mListeners.values()) {
            listener.onStartingConnection();
        }
    }

    /**
     * Utility method to notify the mavlink listeners about a successful
     * connection.
     */
    protected void reportConnect(long connectionTime) {
        for (MavLinkConnectionListener listener : mListeners.values()) {
            listener.onConnect(connectionTime);
        }
    }

    /**
     * Utility method to notify the mavlink listeners about a connection
     * disconnect.
     */
    protected void reportDisconnect(long disconnectTime) {
        if (mListeners.isEmpty())
            return;

        for (MavLinkConnectionListener listener : mListeners.values()) {
            listener.onDisconnect(disconnectTime);
        }
    }

    /**
     * Utility method to notify the mavlink listeners about received messages.
     *
     * @param packet received mavlink packet
     */
    private void reportReceivedPacket(MAVLinkPacket packet) {
        if (mListeners.isEmpty())
            return;

        for (MavLinkConnectionListener listener : mListeners.values()) {
            listener.onReceivePacket(packet);
        }
    }

}
