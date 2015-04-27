package org.droidplanner.services.android.communication.service;

import android.content.Context;

import com.MAVLink.MAVLinkPacket;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;
import org.droidplanner.services.android.api.MavLinkServiceApi;
import org.droidplanner.services.android.data.SessionDB;
import org.droidplanner.services.android.utils.file.DirectoryPath;
import org.droidplanner.services.android.utils.file.FileUtils;

import java.io.File;
import java.util.Date;

/**
 * Provide a common class for some ease of use functionality
 */
public class MAVLinkClient implements MAVLinkStreams.MAVLinkOutputStream {

    private final static String TAG = MAVLinkClient.class.getSimpleName();

    private static final String TLOG_PREFIX = "log";

    /**
     * Maximum possible sequence number for a packet.
     */
    private static final int MAX_PACKET_SEQUENCE = 255;

    private final MavLinkConnectionListener mConnectionListener = new MavLinkConnectionListener() {

        @Override
        public void onStartingConnection() {
            listener.notifyStartingConnection();
        }

        @Override
        public void onConnect(long connectionTime) {
            startLoggingThread(connectionTime);
            listener.notifyConnected();
        }

        @Override
        public void onReceivePacket(final MAVLinkPacket packet) {
            listener.notifyReceivedData(packet);
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

    private final MAVLinkStreams.MavlinkInputStream listener;
    private final MavLinkServiceApi mavLinkApi;
    private final SessionDB sessionDB;
    private final Context context;

    private int packetSeqNumber = 0;
    private final ConnectionParameter connParams;

    public MAVLinkClient(Context context, MAVLinkStreams.MavlinkInputStream listener,
                         ConnectionParameter connParams, MavLinkServiceApi serviceApi) {
        this.context = context;
        this.listener = listener;
        this.mavLinkApi = serviceApi;
        this.connParams = connParams;
        this.sessionDB = new SessionDB(context);
    }

    @Override
    public void openConnection() {
        if (this.connParams == null)
            return;

        final String tag = toString();
        final int connectionStatus = mavLinkApi.getConnectionStatus(this.connParams, tag);
        if (connectionStatus == MavLinkConnection.MAVLINK_DISCONNECTED
                || connectionStatus == MavLinkConnection.MAVLINK_CONNECTING) {
            mavLinkApi.connectMavLink(this.connParams, tag, mConnectionListener);
        }
    }

    @Override
    public void closeConnection() {
        if (this.connParams == null)
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
        if (this.connParams == null) {
            return;
        }

        pack.seq = packetSeqNumber;

        if(mavLinkApi.sendData(this.connParams, pack)) {
            packetSeqNumber = (packetSeqNumber + 1) % (MAX_PACKET_SEQUENCE + 1);
        }
    }

    @Override
    public boolean isConnected() {
        return this.connParams != null
                && mavLinkApi.getConnectionStatus(this.connParams, toString()) == MavLinkConnection.MAVLINK_CONNECTED;
    }

    public boolean isConnecting(){
        return this.connParams != null && mavLinkApi.getConnectionStatus(this.connParams,
                toString()) == MavLinkConnection.MAVLINK_CONNECTING;
    }

    @Override
    public void toggleConnectionState() {
        if (isConnected()) {
            closeConnection();
        } else {
            openConnection();
        }
    }

    private File getTLogDir(String appId) {
        return DirectoryPath.getTLogPath(this.context, appId);
    }

    private File getTempTLogFile(String appId, long connectionTimestamp) {
        return new File(getTLogDir(appId), getTLogFilename(connectionTimestamp));
    }

    private String getTLogFilename(long connectionTimestamp) {
        return TLOG_PREFIX + "_" + MavLinkConnectionTypes.getConnectionTypeLabel(this.connParams.getConnectionType()) +
                "_" + FileUtils.getTimeStamp(connectionTimestamp) + FileUtils.TLOG_FILENAME_EXT;
    }

    public void addLoggingFile(String appId){
        if(isConnecting() || isConnected()) {
            final File logFile = getTempTLogFile(appId, System.currentTimeMillis());
            mavLinkApi.addLoggingFile(this.connParams, appId, logFile.getAbsolutePath());
        }
    }

    public void removeLoggingFile(String appId){
        if(isConnecting() || isConnected()){
            mavLinkApi.removeLoggingFile(this.connParams, appId);
        }
    }

    private void startLoggingThread(long startTime) {
        //log into the database the connection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType());
        this.sessionDB.startSession(new Date(startTime), connectionType);
    }

    private void stopLoggingThread(long stopTime) {
        //log into the database the disconnection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType());
        this.sessionDB.endSession(new Date(stopTime), connectionType, new Date());
    }
}
