package org.droidplanner.services.android.api;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.drone.connection.ConnectionType;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerApiCallback;
import com.ox3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.communication.connection.AndroidMavLinkConnection;
import org.droidplanner.services.android.communication.connection.AndroidTcpConnection;
import org.droidplanner.services.android.communication.connection.AndroidUdpConnection;
import org.droidplanner.services.android.communication.connection.BluetoothConnection;
import org.droidplanner.services.android.communication.connection.usb.UsbConnection;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.utils.analytics.GAUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fhuya on 10/30/14.
 */
public class DroidPlannerService extends Service {

    private static final String CLAZZ_NAME = DroidPlannerService.class.getName();
    private static final String TAG = DroidPlannerService.class.getSimpleName();

    public static final String ACTION_DRONE_CREATED = CLAZZ_NAME + ".ACTION_DRONE_CREATED";
    public static final String ACTION_DRONE_DESTROYED = CLAZZ_NAME + ".ACTION_DRONE_DESTROYED";

    private final Handler handler = new Handler();

    private LocalBroadcastManager lbm;

    /**
     * Caches drone manager instances per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, DroneManager> dronePerConnection = new ConcurrentHashMap<ConnectionParameter, DroneManager>();

    /**
     * Caches droidplanner api instances per connection type and client.
     */
    private final ConcurrentHashMap<ConnectionParameter, ConcurrentHashMap<IBinder,
            IDroidPlannerApi>> dpApisCache = new ConcurrentHashMap<ConnectionParameter,
            ConcurrentHashMap<IBinder, IDroidPlannerApi>>();

    /**
     * Caches mavlink connections per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection> mavConnections =
            new ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection>();


    private final DPServices dpServices = new DPServices(this);
    private final DroneAccess droneAccess = new DroneAccess(this);
    private final MavLinkServiceApi mavlinkApi = new MavLinkServiceApi(this);

    IDroidPlannerApi connectToApi(ConnectionParameter connParams, IDroidPlannerApiCallback callback)
            throws RemoteException {

        ConcurrentHashMap<IBinder, IDroidPlannerApi> binderApis = dpApisCache.get(connParams);
        if (binderApis == null) {
            binderApis = new ConcurrentHashMap<IBinder, IDroidPlannerApi>();
            ConcurrentHashMap<IBinder, IDroidPlannerApi> previous = dpApisCache.putIfAbsent
                    (connParams, binderApis);

            if (previous != null)
                binderApis = previous;
        }

        //Check if a droidplanner api was already generated for this binder.
        IBinder callbackBinder = callback.asBinder();
        IDroidPlannerApi dpApi = binderApis.get(callbackBinder);
        if (dpApi == null) {
            dpApi = new DPApi(this, connParams, callback);

            IDroidPlannerApi previous = binderApis.putIfAbsent(callbackBinder, dpApi);

            if (previous != null)
                dpApi = previous;
        }

        return dpApi;
    }

    boolean disconnectFromApi(final ConnectionParameter connParams,
                              IDroidPlannerApiCallback callback) {
        ConcurrentHashMap<IBinder, IDroidPlannerApi> binderApis = dpApisCache.get(connParams);
        if (binderApis == null) {
            return false;
        }

        boolean wasRemoved = binderApis.remove(callback.asBinder()) != null;
        if (binderApis.isEmpty()) {
            dpApisCache.remove(connParams);

            //Remove the cached drone manager as well.
            DroneManager droneMgr = dronePerConnection.remove(connParams);
            droneMgr.destroy();

            lbm.sendBroadcast(new Intent(ACTION_DRONE_DESTROYED));
        }

        return wasRemoved;
    }

    DroneManager getDroneForConnection(final ConnectionParameter params) throws
            ConnectionException {
        DroneManager droneMgr = dronePerConnection.get(params);
        if (droneMgr == null) {
            droneMgr = new DroneManager(getApplicationContext(), handler, mavlinkApi, params);
            DroneManager previous = dronePerConnection.putIfAbsent(params, droneMgr);
            if (previous != null)
                droneMgr = previous;

            lbm.sendBroadcast(new Intent(ACTION_DRONE_CREATED));
        }

        return droneMgr;
    }

    void connectMAVConnection(ConnectionParameter connParams){
        AndroidMavLinkConnection conn = mavConnections.get(connParams);
        if(conn == null){
            //Create a new mavlink connection
            final int connectionType = connParams.getConnectionType();
            final Bundle paramsBundle = connParams.getParamsBundle();
            switch(connectionType){
                case ConnectionType.TYPE_USB:
                    final int baudRate = paramsBundle.getInt(ConnectionType.EXTRA_USB_BAUD_RATE,
                            ConnectionType.DEFAULT_BAUD_RATE);
                    conn = new UsbConnection(getApplicationContext(), baudRate);
                    break;

                case ConnectionType.TYPE_BLUETOOTH:
                    //Retrieve the bluetooth address to connect to
                    final String bluetoothAddress = paramsBundle.getString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS);
                    conn = new BluetoothConnection(getApplicationContext(), bluetoothAddress);
                    break;

                case ConnectionType.TYPE_TCP:
                    //Retrieve the server ip and port
                    final String tcpServerIp = paramsBundle.getString(ConnectionType.EXTRA_TCP_SERVER_IP);
                    final int tcpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_TCP_SERVER_PORT, ConnectionType.DEFAULT_TCP_SERVER_PORT);
                    conn = new AndroidTcpConnection(getApplicationContext(), tcpServerIp,
                            tcpServerPort);
                    break;

                case ConnectionType.TYPE_UDP:
                    final int udpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_UDP_SERVER_PORT, ConnectionType.DEFAULT_UPD_SERVER_PORT);
                    conn = new AndroidUdpConnection(getApplicationContext(), udpServerPort);
                    break;

                default:
                    Log.e(TAG, "Unrecognized connection type: " + connectionType);
                    return;
            }

            mavConnections.put(connParams, conn);
        }

        if(conn.getConnectionStatus() == MavLinkConnection.MAVLINK_DISCONNECTED){
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
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (DroneManager droneMgr : dronePerConnection.values()) {
            final Drone drone = droneMgr.getDrone();
            if (drone.getMavClient().isConnected()) {
                drone.getMavClient().closeConnection();
            }
        }

        for(ConnectionParameter connParams: mavConnections.keySet()){
            disconnectMAVConnection(connParams);
        }

        mavConnections.clear();
    }

}
