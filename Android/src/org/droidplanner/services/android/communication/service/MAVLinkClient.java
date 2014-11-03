package org.droidplanner.services.android.communication.service;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;
import org.droidplanner.services.android.api.MavLinkServiceApi;

/**
 * Provide a common class for some ease of use functionality
 */
public class MAVLinkClient implements MAVLinkStreams.MAVLinkOutputStream {

    private static final String TAG = MAVLinkClient.class.getSimpleName();

    private final MavLinkConnectionListener mConnectionListener = new MavLinkConnectionListener() {

        @Override
        public void onConnect() {
            listener.notifyConnected();
        }

        @Override
        public void onReceiveMessage(final MAVLinkMessage msg) {
            listener.notifyReceivedData(msg);
        }

        @Override
        public void onDisconnect() {
            listener.notifyDisconnected();
            closeConnection();
        }

        @Override
        public void onComError(final String errMsg) {
            if (errMsg != null) {
                Log.e(TAG, "MAVLink Error: " + errMsg);
            }
        }
    };

    private final ConnectionParameter connParams;
    private final MAVLinkStreams.MavlinkInputStream listener;

    private final MavLinkServiceApi mavLinkApi;

    public MAVLinkClient(MAVLinkStreams.MavlinkInputStream listener, MavLinkServiceApi serviceApi,
                         ConnectionParameter connParams) {
        this.listener = listener;
        this.connParams = connParams;
        this.mavLinkApi = serviceApi;
    }

    @Override
    public void openConnection() {
        if(mavLinkApi.getConnectionStatus(this.connParams) == MavLinkConnection
                .MAVLINK_DISCONNECTED) {
            mavLinkApi.connectMavLink(this.connParams);
            mavLinkApi.addMavLinkConnectionListener(this.connParams, TAG, mConnectionListener);
        }
    }

    @Override
    public void closeConnection() {
        if (mavLinkApi.getConnectionStatus(this.connParams) == MavLinkConnection.MAVLINK_CONNECTED) {
            mavLinkApi.disconnectMavLink(this.connParams);

            mavLinkApi.removeMavLinkConnectionListener(this.connParams, TAG);
            listener.notifyDisconnected();
        }
    }

    @Override
    public void sendMavPacket(MAVLinkPacket pack) {
        if (!isConnected()) {
            return;
        }

        mavLinkApi.sendData(this.connParams, pack);
    }

    @Override
    public boolean isConnected() {
        return mavLinkApi.getConnectionStatus(this.connParams) == MavLinkConnection.MAVLINK_CONNECTED;
    }

    @Override
    public void toggleConnectionState() {
        if (isConnected()) {
            closeConnection();
        } else {
            openConnection();
        }
    }
}
