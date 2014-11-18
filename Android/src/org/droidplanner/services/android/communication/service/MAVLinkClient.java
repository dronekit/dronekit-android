package org.droidplanner.services.android.communication.service;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;

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
                listener.onStreamError(errMsg);
            }
        }
    };

    private ConnectionParameter connParams;
    private final MAVLinkStreams.MavlinkInputStream listener;

    private final MavLinkServiceApi mavLinkApi;

    public MAVLinkClient(MAVLinkStreams.MavlinkInputStream listener, MavLinkServiceApi serviceApi) {
        this.listener = listener;
        this.mavLinkApi = serviceApi;
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

        if(mavLinkApi.getConnectionStatus(this.connParams) == MavLinkConnection
                .MAVLINK_DISCONNECTED) {
            mavLinkApi.connectMavLink(this.connParams);
            mavLinkApi.addMavLinkConnectionListener(this.connParams, TAG, mConnectionListener);
        }
    }

    @Override
    public void closeConnection() {
        if(this.connParams == null)
            return;

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
        return this.connParams != null && mavLinkApi.getConnectionStatus(this.connParams) ==
                MavLinkConnection.MAVLINK_CONNECTED;
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
