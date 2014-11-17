package org.droidplanner.services.android.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.model.IDroidPlannerApi;
import com.o3dr.services.android.lib.model.IDroidPlannerApiCallback;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.communication.connection.AndroidMavLinkConnection;
import org.droidplanner.services.android.communication.connection.AndroidTcpConnection;
import org.droidplanner.services.android.communication.connection.AndroidUdpConnection;
import org.droidplanner.services.android.communication.connection.BluetoothConnection;
import org.droidplanner.services.android.communication.connection.usb.UsbConnection;
import org.droidplanner.services.android.communication.service.UploaderService;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
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
    public static final String ACTION_KICK_START_DRONESHARE_UPLOADS = CLAZZ_NAME + ".ACTION_KICK_START_DRONESHARE_UPLOADS";

    private final Handler handler = new Handler();
    private LocalBroadcastManager lbm;

    /**
     * Caches drone manager instances per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, DroneManager> dronePerConnection = new ConcurrentHashMap<ConnectionParameter, DroneManager>();

    /**
     * Caches mavlink connections per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection> mavConnections =
            new ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection>();

    private DPServices dpServices;
    private DroneAccess droneAccess;
    private MavLinkServiceApi mavlinkApi;

    void releaseDroneManager(DroneManager droneManager, DroneEventsListener listener){
        if(droneManager == null)
            return;

        droneManager.removeDroneEventsListener(listener);
        if(droneManager.getListenersCount() == 0) {
            dronePerConnection.remove(droneManager.getConnectionParameter());
            droneManager.destroy();
            lbm.sendBroadcast(new Intent(ACTION_DRONE_DESTROYED));
        }
    }

    DroneManager getDroneForConnection(final ConnectionParameter params,
                                       DroneEventsListener listener) {
        DroneManager droneMgr = dronePerConnection.get(params);
        if (droneMgr == null) {
            droneMgr = new DroneManager(getApplicationContext(), handler, mavlinkApi, params);
            DroneManager previous = dronePerConnection.putIfAbsent(params, droneMgr);
            if (previous != null)
                droneMgr = previous;

            lbm.sendBroadcast(new Intent(ACTION_DRONE_CREATED));

            // Do a quick scan to see if we need any droneshare uploads
            if(params != null) {
                kickStartDroneShareUploader(params.getDroneSharePrefs());
            }
        }

        droneMgr.addDroneEventsListener(listener);

        return droneMgr;
    }

    private void kickStartDroneShareUploader(DroneSharePrefs... prefs){
        final Context context = getApplicationContext();
        for(DroneSharePrefs pref: prefs){
            UploaderService.kickStart(context, pref);
        }
    }

    void connectMAVConnection(ConnectionParameter connParams){
        AndroidMavLinkConnection conn = mavConnections.get(connParams);
        if(conn == null){

            //Create a new mavlink connection
            final int connectionType = connParams.getConnectionType();
            final Bundle paramsBundle = connParams.getParamsBundle();
            final DroneSharePrefs droneSharePrefs = connParams.getDroneSharePrefs();

            switch(connectionType){
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_KICK_START_DRONESHARE_UPLOADS.equals(action)){
                kickStartDroneShareUploader(dronePerConnection.keySet().toArray(new
                        DroneSharePrefs[dronePerConnection.size()]));
            }
        }

        return START_REDELIVER_INTENT;
    }

}
