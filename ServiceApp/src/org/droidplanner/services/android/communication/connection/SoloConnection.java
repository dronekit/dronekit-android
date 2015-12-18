package org.droidplanner.services.android.communication.connection;

import android.content.Context;
import android.text.TextUtils;

import org.droidplanner.services.android.utils.connection.WifiConnectionHandler;

import java.io.IOException;

import timber.log.Timber;

/**
 * Abstract the connection to a Solo vehicle.
 * Created by Fredia Huya-Kouadio on 12/17/15.
 */
public class SoloConnection extends AndroidMavLinkConnection {

    private static final int SOLO_UDP_PORT = 14550;

    private final WifiConnectionHandler wifiHandler;
    private final AndroidUdpConnection dataLink;
    private final String soloLinkId;
    private final String soloLinkPassword;

    private final Runnable completeDataLinkConnection = new Runnable() {
        @Override
        public void run() {
            try {
                dataLink.openConnection();
            } catch (IOException e) {
                Timber.e(e, e.getMessage());
                onConnectionFailed(e.getMessage());
            }
        }
    };

    public SoloConnection(Context applicationContext, String soloLinkId, String password) {
        super(applicationContext);
        this.wifiHandler = new WifiConnectionHandler(applicationContext);
        this.soloLinkId = soloLinkId;
        this.soloLinkPassword = password;
        this.dataLink = new AndroidUdpConnection(applicationContext, SOLO_UDP_PORT) {
            @Override
            protected void onConnectionOpened() {
                SoloConnection.this.onConnectionOpened();
            }

            @Override
            protected void onConnectionFailed(String errMsg) {
                SoloConnection.this.onConnectionFailed(errMsg);
            }
        };
    }

    @Override
    protected void openConnection() throws IOException {
        if (TextUtils.isEmpty(soloLinkId) || TextUtils.isEmpty(soloLinkPassword)) {
            throw new IOException("Invalid connection credentials!");
        }

        wifiHandler.start();

        boolean isConnecting = wifiHandler.connectToWifi(soloLinkId, soloLinkPassword, completeDataLinkConnection);
        if (!isConnecting) {
            throw new IOException("Unable to connect to solo wifi " + soloLinkId);
        }
    }

    @Override
    protected int readDataBlock(byte[] buffer) throws IOException {
        return dataLink.readDataBlock(buffer);
    }

    @Override
    protected void sendBuffer(byte[] buffer) throws IOException {
        dataLink.sendBuffer(buffer);
    }

    @Override
    protected void closeConnection() throws IOException {
        wifiHandler.stop();
        dataLink.closeConnection();
    }

    @Override
    protected void loadPreferences() {
        dataLink.loadPreferences();
    }

    @Override
    public int getConnectionType() {
        return dataLink.getConnectionType();
    }
}
