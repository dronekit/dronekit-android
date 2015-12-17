package org.droidplanner.services.android.utils.connection;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Used to handle connection with the sololink wifi network.
 */
public class WifiConnectionHandler {

    private static final String PACKAGE_NAME = "org.droidplanner.services.android.utils.connection";

    public static final String ACTION_STARTING_VEHICLE_CONNECTION = PACKAGE_NAME + ".action" +
            ".STARTING_VEHICLE_CONNECTION";

    public static final String ACTION_VEHICLE_CONNECTION_COMPLETED = PACKAGE_NAME + ".action" +
            ".VEHICLE_CONNECTION_COMPLETED";

    public interface WifiConnectionListener {
        void onWifiConnecting();

        void onWifiConnected(String wifiSSID);

        void onWifiDisconnected();

        void onScanResultsUpdate(String connectedWifi, ArrayList<ScanResult> scanResults);
    }

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {

                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                        showVehicleLinksSelector();
                    break;

                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    NetworkInfo.State networkState = netInfo == null
                            ? NetworkInfo.State.DISCONNECTED
                            : netInfo.getState();

                    switch (networkState) {
                        case CONNECTED:
                            final WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                            final String wifiSSID = wifiInfo.getSSID();
                            Timber.i("Connected to " + wifiSSID);

                            final DhcpInfo dhcpInfo = wifiMgr.getDhcpInfo();
                            if(dhcpInfo != null) {
                                Timber.i("Dhcp info: %s", dhcpInfo.toString());
                            }else{
                                Timber.w("Dhcp info is not available.");
                            }

                            if (wifiSSID != null && wifiSSID.startsWith("\"SoloLink")) {
                                //Attempt to connect to the vehicle.
                                if (wifiConnectionListener != null)
                                    wifiConnectionListener.onWifiConnected(wifiSSID);

                                Timber.i("Requesting route to sololink network");
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    NetworkRequest netReq = new NetworkRequest.Builder()
                                            .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                                            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                            .build();
                                    connMgr.requestNetwork(netReq, new ConnectivityManager.NetworkCallback() {

                                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                        private void getNetworkInfo(Network network){
                                            if(network == null){
                                                Timber.i("Network is null.");
                                            }
                                            else{
                                                Timber.i("Network: %s, active : %s", network, connMgr.isDefaultNetworkActive());
                                                LinkProperties linkProps = connMgr.getLinkProperties(network);
                                                Timber.i("Network link properties: %s", linkProps.toString());
                                                Timber.i("Network capabilities: %s", connMgr.getNetworkCapabilities(network));
                                            }
                                        }

                                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                        @Override
                                        public void onAvailable(Network network) {
                                            Timber.i("Network %s is available", network);
                                            getNetworkInfo(network);
                                            if (ConnectivityManager.setProcessDefaultNetwork(network)) {
                                                Timber.i("Bound process to network %s", network);
                                            } else {
                                                Timber.w("Unable to bind process to network %s", network);
                                            }
                                        }

                                        @Override
                                        public void onLosing(Network network, int maxMsToLive) {
                                            Timber.w("Losing network %s", network);
                                        }

                                        @Override
                                        public void onLost(Network network) {
                                            Timber.w("Lost network %s", network);
                                        }

                                    });
                                }
                            }
                            break;

                        case DISCONNECTED:
                            Timber.d("Disconnected from wifi network.");
                            //Maybe disconnect from the vehicle.
                            if (wifiConnectionListener != null)
                                wifiConnectionListener.onWifiDisconnected();
                            break;

                        case CONNECTING:
                            Timber.d("Connecting to wifi network.");
                            break;
                    }
                    break;

                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    break;
            }
        }
    };

    private final Context context;
    private final LocalBroadcastManager lbm;

    private final WifiManager wifiMgr;
    private final ConnectivityManager connMgr;
    private WifiConnectionListener wifiConnectionListener;

    private String targetSSID;

    public WifiConnectionHandler(Context context) {
        this.context = context;
        this.lbm = LocalBroadcastManager.getInstance(this.context);
        this.wifiMgr = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        this.connMgr = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void setWifiConnectionListener(WifiConnectionListener wifiConnectionListener) {
        this.wifiConnectionListener = wifiConnectionListener;
    }

    /**
     * Start the wifi connection handler process.
     * It will start listening for wifi connectivity updates, and will handle them as needed.
     */
    public void start() {
        this.context.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * Stop the wifi connection handler process.
     */
    public void stop() {
        try {
            this.context.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            Timber.d("Receiver was not registered.", e);
        }
    }

    /**
     * Query available wifi networks
     */
    public void refreshWifiAPs() {
        Timber.d("Querying wifi access points.");
        if (wifiMgr == null)
            return;

        if (!wifiMgr.isWifiEnabled() && !wifiMgr.setWifiEnabled(true)) {
            Toast.makeText(context, "Unable to activate Wi-Fi!", Toast.LENGTH_LONG).show();
            return;
        }

        showVehicleLinksSelector();

        if (!wifiMgr.startScan()) {
            Toast.makeText(context, "Unable to scan for Wi-Fi networks!", Toast.LENGTH_LONG).show();
        }
    }

    private void showVehicleLinksSelector() {
        final List<ScanResult> scanResults = wifiMgr.getScanResults();
        final Map<String, ScanResult> soloLinkWifis = new HashMap<>();
        for (ScanResult result : scanResults) {
            if (result.SSID.startsWith("SoloLink")) {
                soloLinkWifis.put(result.SSID, result);
            }
        }

        final int soloLinksCount = soloLinkWifis.size();
        if (soloLinksCount == 0) {
            Toast.makeText(context, "No solo vehicle detected!", Toast.LENGTH_LONG).show();
        } else {
            final WifiInfo connectedWifi = wifiMgr.getConnectionInfo();

            final ArrayList<ScanResult> prunedLinks = new ArrayList<>(soloLinkWifis.values());
            final String connectedSSID = connectedWifi == null ? null : connectedWifi.getSSID()
                    .replace("\"", "");

            if(wifiConnectionListener != null){
                wifiConnectionListener.onScanResultsUpdate(connectedSSID, prunedLinks);
            }
        }
    }

    public boolean connectToWifi(ScanResult scanResult) {
        if (scanResult == null)
            return false;

        Timber.d("Connecting to wifi " + scanResult.SSID);

        //Check if we're already connected to the given network.
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        if (wifiInfo.getSSID().equals("\"" + scanResult.SSID + "\"")) {
            Timber.d("Already connected to " + wifiInfo.getSSID());

            targetSSID = "\"" + scanResult.SSID + "\"";

            if (wifiConnectionListener != null)
                wifiConnectionListener.onWifiConnected(targetSSID);
            return true;
        }

        if(wifiConnectionListener != null)
            wifiConnectionListener.onWifiConnecting();

        Timber.d("Connecting to closed wifi network.");
        if (!connectToClosedWifi(scanResult))
            return false;

        wifiMgr.saveConfiguration();

        WifiConfiguration updatedConf = getWifiConfigs(scanResult.SSID);
        if (updatedConf != null) {
            targetSSID = "\"" + scanResult.SSID + "\"";

            wifiMgr.enableNetwork(updatedConf.networkId, true);
            lbm.sendBroadcast(new Intent(ACTION_STARTING_VEHICLE_CONNECTION));
            return true;
        }
        return false;
    }

    private WifiConfiguration getWifiConfigs(String networkSSID) {
        List<WifiConfiguration> networks = wifiMgr.getConfiguredNetworks();
        for (WifiConfiguration current : networks) {
            if (current.SSID != null && current.SSID.equals("\"" + networkSSID + "\"")) {
                return current;
            }
        }

        return null;
    }

    private boolean connectToClosedWifi(ScanResult scanResult) {
        final WifiConfiguration wifiConf = new WifiConfiguration();
        wifiConf.SSID = "\"" + scanResult.SSID + "\""; //Please note the quotes. String should contain ssid in quotes.
        wifiConf.preSharedKey = "\"" + "solo4Fred" + "\"";

        final int netId = wifiMgr.addNetwork(wifiConf);
        if (netId == -1) {
            Toast.makeText(context, "Unable to connect to Wi-Fi " + scanResult.SSID, Toast.LENGTH_LONG).show();
            Timber.e("Unable to add wifi configuration for " + scanResult.SSID);
            return false;
        }

        return true;
    }
}
