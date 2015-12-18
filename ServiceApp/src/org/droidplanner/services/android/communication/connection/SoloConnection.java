package org.droidplanner.services.android.communication.connection;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.text.TextUtils;

import org.droidplanner.services.android.utils.connection.WifiConnectionHandler;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * Abstract the connection to a Solo vehicle.
 * Created by Fredia Huya-Kouadio on 12/17/15.
 */
public class SoloConnection extends AndroidMavLinkConnection implements WifiConnectionHandler.WifiConnectionListener {

    private static final int SOLO_UDP_PORT = 14550;

    private final WifiConnectionHandler wifiHandler;
    private final AndroidUdpConnection dataLink;
    private final String soloLinkId;
    private final String soloLinkPassword;

    public SoloConnection(Context applicationContext, String soloLinkId, String password) {
        super(applicationContext);
        this.wifiHandler = new WifiConnectionHandler(applicationContext);
        wifiHandler.setListener(this);

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
        checkScanResults(wifiHandler.getScanResults());
    }

    private void refreshWifiAps() {
        if (!wifiHandler.refreshWifiAPs()) {
            onConnectionFailed("Unable to refresh wifi access points");
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

    @Override
    public void onWifiConnected(String wifiSsid) {
        if (isConnecting()) {
            //Let's see if we're connected to our target wifi
            if (wifiSsid.equalsIgnoreCase(soloLinkId)) {
                //We're good to go
                try {
                    dataLink.openConnection();
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                    onConnectionFailed(e.getMessage());
                }
            }
        }
    }

    @Override
    public void onWifiConnecting() {

    }

    @Override
    public void onWifiDisconnected() {

    }

    @Override
    public void onWifiScanResultsAvailable(List<ScanResult> results) {
        checkScanResults(results);
    }

    private void checkScanResults(List<ScanResult> results) {
        if (!isConnecting())
            return;

        //We're in the connection process, let's see if the wifi we want is available
        ScanResult targetResult = null;
        for (ScanResult result : results) {
            if (result.SSID.equalsIgnoreCase(this.soloLinkId)) {
                //bingo
                targetResult = result;
                break;
            }
        }

        if (targetResult != null) {
            //We're good to go
            try {
                if (!wifiHandler.connectToWifi(targetResult, soloLinkPassword)) {
                    onConnectionFailed("Unable to connect to the target wifi " + soloLinkId);
                }
            } catch (IllegalArgumentException e) {
                Timber.e(e, e.getMessage());
                onConnectionFailed(e.getMessage());
            }
        } else {
            //Let's try again
            refreshWifiAps();
        }
    }

    private boolean isConnecting() {
        return getConnectionStatus() == MAVLINK_CONNECTING;
    }
}
