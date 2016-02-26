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
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;

import org.droidplanner.services.android.utils.NetworkUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Used to handle connection with the sololink wifi network.
 */
public class WifiConnectionHandler {

    public interface WifiConnectionListener {
        void onWifiConnected(String wifiSsid);

        void onWifiConnecting();

        void onWifiDisconnected(String prevConnectedSsid);

        void onWifiScanResultsAvailable(List<ScanResult> results);

        void onWifiConnectionFailed(LinkConnectionStatus connectionStatus);
    }

    public static final String SOLO_LINK_WIFI_PREFIX = "SoloLink_";

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {

                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    notifyWifiScanResultsAvailable(wifiMgr.getScanResults());
                    break;

                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState supState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    String ssid = NetworkUtils.getCurrentWifiLink(context);

                    int supplicationError = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    Timber.d("Supplicant state changed error %s with state %s and ssid %s", supplicationError, supState, ssid);
                    if (supplicationError == WifiManager.ERROR_AUTHENTICATING) {
                        if (NetworkUtils.isSoloNetwork(ssid)) {
                            notifyWifiConnectionFailed();
                            WifiConfiguration wifiConfig = getWifiConfigs(ssid);
                            if (wifiConfig != null) {
                                wifiMgr.removeNetwork(wifiConfig.networkId);
                            }
                        }
                    }
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
                            if (dhcpInfo != null) {
                                Timber.i("Dhcp info: %s", dhcpInfo.toString());
                            } else {
                                Timber.w("Dhcp info is not available.");
                            }

                            if (wifiSSID != null) {
                                setDefaultNetworkIfNecessary(wifiSSID);
                            }
                            break;

                        case DISCONNECTED:
                            Timber.i("Disconnected from wifi network.");
                            notifyWifiDisconnected();
                            break;

                        case CONNECTING:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                NetworkInfo.DetailedState detailedState = netInfo.getDetailedState();
                                if (detailedState != null && detailedState == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                                    String connectingSsid = NetworkUtils.getCurrentWifiLink(context);
                                    setDefaultNetworkIfNecessary(connectingSsid);
                                }
                            }
                            Timber.d("Connecting to wifi network.");
                            notifyWifiConnecting();
                            break;
                    }
                    break;

                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    break;
            }
        }
    };

    private final Object netReq;
    private final Object netReqCb;

    private final AtomicReference<String> connectedWifi = new AtomicReference<>("");

    private final Context context;

    private final WifiManager wifiMgr;
    private final ConnectivityManager connMgr;

    private WifiConnectionListener listener;

    public WifiConnectionHandler(Context context) {
        this.context = context;
        this.wifiMgr = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        this.connMgr = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            netReq = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

            netReqCb = new ConnectivityManager.NetworkCallback() {

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                private void getNetworkInfo(Network network) {
                    if (network == null) {
                        Timber.i("Network is null.");
                    } else {
                        Timber.i("Network: %s, active : %s", network, connMgr.isDefaultNetworkActive());
                        LinkProperties linkProps = connMgr.getLinkProperties(network);
                        Timber.i("Network link properties: %s", linkProps.toString());
                        Timber.i("Network capabilities: %s", connMgr.getNetworkCapabilities(network));
                    }
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAvailable(Network network) {
                    //Check if we're still connected to solo. If not, unregister the callbacks
                    final String currentWifi = getCurrentWifiLink();
                    if (!isSoloWifi(currentWifi)) {
                        resetNetworkBindings(this);
                        return;
                    }

                    Timber.i("Network %s is available", network);
                    getNetworkInfo(network);

                    final boolean wasBound;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        wasBound = connMgr.bindProcessToNetwork(network);
                    } else {
                        wasBound = ConnectivityManager.setProcessDefaultNetwork(network);
                    }

                    if (wasBound) {
                        Timber.i("Bound process to network %s", network);
                        notifyWifiConnected(currentWifi);
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

            };
        } else {
            netReq = null;
            netReqCb = null;
        }
    }

    public void setListener(WifiConnectionListener listener) {
        this.listener = listener;
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
            Timber.w(e, "Receiver was not registered.");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resetNetworkBindings((ConnectivityManager.NetworkCallback) netReqCb);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void resetNetworkBindings(ConnectivityManager.NetworkCallback netCb) {
        Timber.i("Unregistering network callbacks.");

        connectedWifi.set(NetworkUtils.getCurrentWifiLink(context));
        try {
            connMgr.unregisterNetworkCallback(netCb);
        } catch (IllegalArgumentException e) {
            Timber.w(e, "Network callback was not registered.");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connMgr.bindProcessToNetwork(null);
        } else {
            ConnectivityManager.setProcessDefaultNetwork(null);
        }
    }

    /**
     * Query available wifi networks
     */
    public boolean refreshWifiAPs() {
        Timber.d("Querying wifi access points.");
        if (wifiMgr == null) {
            return false;
        }

        if (!wifiMgr.isWifiEnabled() && !wifiMgr.setWifiEnabled(true)) {
            Toast.makeText(context, "Unable to activate Wi-Fi!", Toast.LENGTH_LONG).show();
            return false;
        }

        return wifiMgr.startScan();
    }

    public boolean isOnNetwork(String wifiSsid) {
        if (TextUtils.isEmpty(wifiSsid)) {
            throw new IllegalArgumentException("Invalid wifi ssid " + wifiSsid);
        }

        return wifiSsid.equalsIgnoreCase(getCurrentWifiLink());
    }

    public boolean isConnected(String wifiSSID) {
        if (!isOnNetwork(wifiSSID)) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network network;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                network = connMgr.getBoundNetworkForProcess();
            } else {
                network = ConnectivityManager.getProcessDefaultNetwork();
            }

            if (network == null) {
                return false;
            }

            NetworkCapabilities netCapabilities = connMgr.getNetworkCapabilities(network);
            return netCapabilities != null && netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            return true;
        }
    }

    public List<ScanResult> getScanResults() {
        return wifiMgr.getScanResults();
    }

    public int connectToWifi(String soloLinkId, String password) {
        if (TextUtils.isEmpty(soloLinkId)) {
            return LinkConnectionStatus.INVALID_CREDENTIALS;
        }

        ScanResult targetScanResult = null;
        final List<ScanResult> scanResults = wifiMgr.getScanResults();
        for (ScanResult result : scanResults) {
            if (result.SSID.equalsIgnoreCase(soloLinkId)) {
                targetScanResult = result;
                break;
            }
        }

        if (targetScanResult == null) {
            Timber.i("No matching scan result was found for id %s", soloLinkId);
            return LinkConnectionStatus.LINK_UNAVAILABLE;
        }

        return connectToWifi(targetScanResult, password);
    }

    public int connectToWifi(ScanResult scanResult, String password) {
        if (scanResult == null) {
            return LinkConnectionStatus.LINK_UNAVAILABLE;
        }

        Timber.d("Connecting to wifi " + scanResult.SSID);


        //Check if we're already connected to the given network.
        if (isConnected(scanResult.SSID)) {
            Timber.d("Already connected to " + scanResult.SSID);

            notifyWifiConnected(scanResult.SSID);
            return 0;
        } else if (isOnNetwork(scanResult.SSID)) {
            setDefaultNetworkIfNecessary(scanResult.SSID);
            return 0;

        }

        WifiConfiguration wifiConfig = getWifiConfigs(scanResult.SSID);

        //Network is not configured and needs a password to connect
        if (wifiConfig == null) {
            Timber.d("Connecting to closed wifi network.");
            if (TextUtils.isEmpty(password)) {
                return LinkConnectionStatus.INVALID_CREDENTIALS;
            }

            if (!connectToClosedWifi(scanResult, password)) {
                return LinkConnectionStatus.UNKNOWN;
            }

            wifiMgr.saveConfiguration();
            wifiConfig = getWifiConfigs(scanResult.SSID);
        }

        if (wifiConfig != null) {
            wifiMgr.enableNetwork(wifiConfig.networkId, true);
            return 0;
        }
        return LinkConnectionStatus.UNKNOWN;
    }

    private WifiConfiguration getWifiConfigs(String networkSSID) {
        List<WifiConfiguration> networks = wifiMgr.getConfiguredNetworks();
        if (networks == null) {
            return null;
        }

        for (WifiConfiguration current : networks) {
            if (current.SSID != null && current.SSID.equals("\"" + networkSSID + "\"")) {
                return current;
            }
        }

        return null;
    }

    private boolean connectToClosedWifi(ScanResult scanResult, String password) {
        final WifiConfiguration wifiConf = new WifiConfiguration();
        wifiConf.SSID = "\"" + scanResult.SSID + "\""; //Please note the quotes. String should contain ssid in quotes.
        wifiConf.preSharedKey = "\"" + password + "\"";

        final int netId = wifiMgr.addNetwork(wifiConf);
        if (netId == -1) {
            Timber.e("Unable to add wifi configuration for %s", scanResult.SSID);
            return false;
        }

        return true;
    }

    private static String trimWifiSsid(String wifiSsid) {
        if (TextUtils.isEmpty(wifiSsid)) {
            return "";
        }

        return wifiSsid.replace("\"", "");
    }

    private String getCurrentWifiLink() {
        return getCurrentWifiLink(wifiMgr);
    }

    public static String getCurrentWifiLink(WifiManager wifiMgr) {
        final WifiInfo connectedWifi = wifiMgr.getConnectionInfo();
        final String connectedSSID = connectedWifi == null ? null : connectedWifi.getSSID();
        return trimWifiSsid(connectedSSID);
    }

    public static boolean isSoloWifi(String wifiSsid) {
        return !TextUtils.isEmpty(wifiSsid) && wifiSsid.startsWith(SOLO_LINK_WIFI_PREFIX);
    }

    private void setDefaultNetworkIfNecessary(String wifiSsid) {
        final String trimmedSsid = trimWifiSsid(wifiSsid);

        if (!trimmedSsid.equals(connectedWifi.get())) {
            connectedWifi.set(trimmedSsid);

            if (isConnected(wifiSsid)) {
                notifyWifiConnected(wifiSsid);
                return;
            }

            if (isSoloWifi(trimmedSsid)) {
                //Attempt to connect to the vehicle.
                Timber.i("Requesting route to sololink network");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    connMgr.requestNetwork((NetworkRequest) netReq, (ConnectivityManager.NetworkCallback) netReqCb);
                } else {
                    notifyWifiConnected(trimmedSsid);

                }
            } else {
                notifyWifiConnected(trimmedSsid);
            }
        }
    }

    private void notifyWifiConnected(String wifiSsid) {
        if (listener != null) {
            listener.onWifiConnected(wifiSsid);
        }
    }

    private void notifyWifiConnecting() {
        if (listener != null) {
            listener.onWifiConnecting();
        }
    }

    private void notifyWifiDisconnected() {
        if (listener != null) {
            listener.onWifiDisconnected(connectedWifi.get());
        }
        connectedWifi.set("");
    }

    private void notifyWifiScanResultsAvailable(List<ScanResult> results) {
        if (listener != null) {
            listener.onWifiScanResultsAvailable(results);
        }
    }

    private void notifyWifiConnectionFailed() {
        if (listener != null) {
            LinkConnectionStatus linkConnectionStatus = LinkConnectionStatus
                .newFailedConnectionStatus(LinkConnectionStatus.INVALID_CREDENTIALS, null);
            listener.onWifiConnectionFailed(linkConnectionStatus);
        }
    }
}
