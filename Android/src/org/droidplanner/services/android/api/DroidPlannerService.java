package org.droidplanner.services.android.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.services.android.communication.connection.AndroidMavLinkConnection;
import org.droidplanner.services.android.communication.connection.AndroidTcpConnection;
import org.droidplanner.services.android.communication.connection.AndroidUdpConnection;
import org.droidplanner.services.android.communication.connection.BluetoothConnection;
import org.droidplanner.services.android.communication.connection.usb.UsbConnection;
import org.droidplanner.services.android.communication.service.UploaderService;
import org.droidplanner.services.android.utils.analytics.GAUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by fhuya on 10/30/14.
 */
public class DroidPlannerService extends Service {

    private static final String CLAZZ_NAME = DroidPlannerService.class.getName();
    private static final String TAG = DroidPlannerService.class.getSimpleName();

    public static final String ACTION_DRONE_CREATED = CLAZZ_NAME + ".ACTION_DRONE_CREATED";
    public static final String ACTION_DRONE_DESTROYED = CLAZZ_NAME + ".ACTION_DRONE_DESTROYED";
    public static final String ACTION_KICK_START_DRONESHARE_UPLOADS = CLAZZ_NAME + ".ACTION_KICK_START_DRONESHARE_UPLOADS";

    private final Handler handler = new Handler();
    private LocalBroadcastManager lbm;

    final ConcurrentLinkedQueue<DPApi> dpApiStore = new ConcurrentLinkedQueue<DPApi>();

    /**
     * Caches mavlink connections per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection> mavConnections =
            new ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection>();

    private DPServices dpServices;
    private DroneAccess droneAccess;
    private MavLinkServiceApi mavlinkApi;

    DPApi acquireDroidPlannerApi() {
        DPApi dpApi = new DPApi(this, handler, mavlinkApi);
        dpApiStore.add(dpApi);
        lbm.sendBroadcast(new Intent(ACTION_DRONE_CREATED));
        return dpApi;
    }

    void releaseDroidPlannerApi(DPApi dpApi) {
        if (dpApi == null)
            return;

        dpApiStore.remove(dpApi);
        dpApi.destroy();
        lbm.sendBroadcast(new Intent(ACTION_DRONE_DESTROYED));
    }

    void kickStartDroneShareUploader(DroneSharePrefs... prefs) {
        final Context context = getApplicationContext();
        for (DroneSharePrefs pref : prefs) {
            UploaderService.kickStart(context, pref);
        }
    }

    void connectMAVConnection(ConnectionParameter connParams) {
        AndroidMavLinkConnection conn = mavConnections.get(connParams);
        if (conn == null) {

            //Create a new mavlink connection
            final int connectionType = connParams.getConnectionType();
            final Bundle paramsBundle = connParams.getParamsBundle();
            final DroneSharePrefs droneSharePrefs = connParams.getDroneSharePrefs();

            switch (connectionType) {
                case ConnectionType.TYPE_USB:
                    final int baudRate = paramsBundle.getInt(ConnectionType.EXTRA_USB_BAUD_RATE,
                            ConnectionType.DEFAULT_USB_BAUD_RATE);
                    conn = new UsbConnection(getApplicationContext(), droneSharePrefs, baudRate);
                    Log.d(TAG, "Connecting over usb.");
                    break;

                case ConnectionType.TYPE_BLUETOOTH:
                    //Retrieve the bluetooth address to connect to
                    final String bluetoothAddress = paramsBundle.getString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS);
                    conn = new BluetoothConnection(getApplicationContext(), droneSharePrefs, bluetoothAddress);
                    Log.d(TAG, "Connecting over bluetooth.");
                    break;

                case ConnectionType.TYPE_TCP:
                    //Retrieve the server ip and port
                    final String tcpServerIp = paramsBundle.getString(ConnectionType.EXTRA_TCP_SERVER_IP);
                    final int tcpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_TCP_SERVER_PORT, ConnectionType.DEFAULT_TCP_SERVER_PORT);
                    conn = new AndroidTcpConnection(getApplicationContext(), droneSharePrefs, tcpServerIp,
                            tcpServerPort);
                    Log.d(TAG, "Connecting over tcp.");
                    break;

                case ConnectionType.TYPE_UDP:
                    final int udpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_UDP_SERVER_PORT, ConnectionType.DEFAULT_UPD_SERVER_PORT);
                    conn = new AndroidUdpConnection(getApplicationContext(), droneSharePrefs, udpServerPort);
                    Log.d(TAG, "Connecting over udp.");
                    break;

                default:
                    Log.e(TAG, "Unrecognized connection type: " + connectionType);
                    return;
            }

            mavConnections.put(connParams, conn);
        }

        if (conn.getConnectionStatus() == MavLinkConnection.MAVLINK_DISCONNECTED) {
            conn.connect();
        }

        // Record which connection type is used.
        GAUtils.sendEvent(new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.MAVLINK_CONNECTION)
                .setAction("MavLink connect")
                .setLabel(connParams.toString()));
    }

    void disconnectMAVConnection(ConnectionParameter connParams) {
        final AndroidMavLinkConnection conn = mavConnections.get(connParams);
        if (conn == null)
            return;

        if (conn.getConnectionStatus() != MavLinkConnection.MAVLINK_DISCONNECTED) {
            Log.d(TAG, "Disconnecting...");
            conn.disconnect();

            GAUtils.sendEvent(new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.MAVLINK_CONNECTION)
                    .setAction("MavLink disconnect")
                    .setLabel(connParams.toString()));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        final String action = intent.getAction();
        if (IDroidPlannerServices.class.getName().equals(action)) {
            // Return binder to ipc client-server interaction.
            return dpServices;
        } else {
            // Return binder to the service.
            return droneAccess;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mavlinkApi = new MavLinkServiceApi(this);
        droneAccess = new DroneAccess(this);
        dpServices = new DPServices(this);
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (DPApi dpApi : dpApiStore) {
            dpApi.destroy();
        }
        dpApiStore.clear();

        for (ConnectionParameter connParams : mavConnections.keySet()) {
            disconnectMAVConnection(connParams);
        }

        mavConnections.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_KICK_START_DRONESHARE_UPLOADS.equals(action)) {
                List<DroneSharePrefs> droneSharePrefsList = new ArrayList<DroneSharePrefs>
                        (mavConnections.size());
                for (ConnectionParameter connParam : mavConnections.keySet()) {
                    DroneSharePrefs sharePrefs = connParam.getDroneSharePrefs();
                    if (sharePrefs != null)
                        droneSharePrefsList.add(sharePrefs);
                }

                kickStartDroneShareUploader(droneSharePrefsList.toArray(new
                        DroneSharePrefs[droneSharePrefsList.size()]));
            }
        }

        return START_REDELIVER_INTENT;
    }

}
