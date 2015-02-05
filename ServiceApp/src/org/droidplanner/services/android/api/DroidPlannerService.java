package org.droidplanner.services.android.api;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;
import org.droidplanner.services.android.R;
import org.droidplanner.services.android.communication.connection.AndroidMavLinkConnection;
import org.droidplanner.services.android.communication.connection.AndroidTcpConnection;
import org.droidplanner.services.android.communication.connection.AndroidUdpConnection;
import org.droidplanner.services.android.communication.connection.BluetoothConnection;
import org.droidplanner.services.android.communication.connection.usb.UsbConnection;
import org.droidplanner.services.android.ui.activity.MainActivity;
import org.droidplanner.services.android.utils.Utils;
import org.droidplanner.services.android.utils.analytics.GAUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by fhuya on 10/30/14.
 */
public class DroidPlannerService extends Service {

    private static final String TAG = DroidPlannerService.class.getSimpleName();

    private static final int FOREGROUND_ID = 101;

    public static final String ACTION_DRONE_CREATED = Utils.PACKAGE_NAME + ".ACTION_DRONE_CREATED";
    public static final String ACTION_DRONE_DESTROYED = Utils.PACKAGE_NAME + ".ACTION_DRONE_DESTROYED";
    public static final String ACTION_KICK_START_DRONESHARE_UPLOADS = Utils.PACKAGE_NAME + ".ACTION_KICK_START_DRONESHARE_UPLOADS";

    private final Handler handler = new Handler();
    private LocalBroadcastManager lbm;

    final ConcurrentLinkedQueue<DroneApi> droneApiStore = new ConcurrentLinkedQueue<DroneApi>();

    /**
     * Caches mavlink connections per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection> mavConnections = new ConcurrentHashMap<>();

    private DPServices dpServices;
    private DroneAccess droneAccess;
    private MavLinkServiceApi mavlinkApi;

    DroneApi registerDroneApi(IApiListener listener, String appId) {
        if (listener == null)
            return null;

        DroneApi droneApi = new DroneApi(this, handler, mavlinkApi, listener, appId);
        droneApiStore.add(droneApi);
        lbm.sendBroadcast(new Intent(ACTION_DRONE_CREATED));
        return droneApi;
    }

    void releaseDroneApi(DroneApi droneApi) {
        if (droneApi == null)
            return;

        droneApiStore.remove(droneApi);
        droneApi.destroy();
        lbm.sendBroadcast(new Intent(ACTION_DRONE_DESTROYED));
    }

    void connectMAVConnection(ConnectionParameter connParams, String listenerTag,
                              MavLinkConnectionListener listener) {
        AndroidMavLinkConnection conn = mavConnections.get(connParams);
        if (conn == null) {

            //Create a new mavlink connection
            final int connectionType = connParams.getConnectionType();
            final Bundle paramsBundle = connParams.getParamsBundle();

            switch (connectionType) {
                case ConnectionType.TYPE_USB:
                    final int baudRate = paramsBundle.getInt(ConnectionType.EXTRA_USB_BAUD_RATE,
                            ConnectionType.DEFAULT_USB_BAUD_RATE);
                    conn = new UsbConnection(getApplicationContext(), baudRate);
                    Log.d(TAG, "Connecting over usb.");
                    break;

                case ConnectionType.TYPE_BLUETOOTH:
                    //Retrieve the bluetooth address to connect to
                    final String bluetoothAddress = paramsBundle.getString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS);
                    conn = new BluetoothConnection(getApplicationContext(), bluetoothAddress);
                    Log.d(TAG, "Connecting over bluetooth.");
                    break;

                case ConnectionType.TYPE_TCP:
                    //Retrieve the server ip and port
                    final String tcpServerIp = paramsBundle.getString(ConnectionType.EXTRA_TCP_SERVER_IP);
                    final int tcpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_TCP_SERVER_PORT, ConnectionType.DEFAULT_TCP_SERVER_PORT);
                    conn = new AndroidTcpConnection(getApplicationContext(), tcpServerIp, tcpServerPort);
                    Log.d(TAG, "Connecting over tcp.");
                    break;

                case ConnectionType.TYPE_UDP:
                    final int udpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_UDP_SERVER_PORT, ConnectionType.DEFAULT_UPD_SERVER_PORT);
                    conn = new AndroidUdpConnection(getApplicationContext(), udpServerPort);
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

            // Record which connection type is used.
            GAUtils.sendEvent(new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.MAVLINK_CONNECTION)
                    .setAction("MavLink connect")
                    .setLabel(connParams.toString()));
        }

        conn.addMavLinkConnectionListener(listenerTag, listener);
    }

    void disconnectMAVConnection(ConnectionParameter connParams, String listenerTag) {
        final AndroidMavLinkConnection conn = mavConnections.get(connParams);
        if (conn == null)
            return;

        conn.removeMavLinkConnectionListener(listenerTag);

        if (conn.getMavLinkConnectionListenersCount() == 0 && conn.getConnectionStatus() !=
                MavLinkConnection.MAVLINK_DISCONNECTED) {
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
        Log.d(TAG, "Binding intent: " + intent);
        final String action = intent.getAction();
        if (IDroidPlannerServices.class.getName().equals(action)) {
            // Return binder to ipc client-server interaction.
            return dpServices;
        } else {
            // Return binder to the service.
            return droneAccess;
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating 3DR Services.");

        final Context context = getApplicationContext();

        mavlinkApi = new MavLinkServiceApi(this);
        droneAccess = new DroneAccess(this);
        dpServices = new DPServices(this);
        lbm = LocalBroadcastManager.getInstance(context);

        //Put the service in the foreground
        final Notification.Builder notifBuilder = new Notification.Builder(context)
                .setContentTitle("3DR Services")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context,
                        MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0));

        final Notification notification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                ? notifBuilder.build()
                : notifBuilder.getNotification();
        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying 3DR Services.");

        for (DroneApi droneApi : droneApiStore) {
            droneApi.destroy();
        }
        droneApiStore.clear();

        for (AndroidMavLinkConnection conn : mavConnections.values()) {
            conn.disconnect();
            conn.removeAllMavLinkConnectionListeners();
        }

        mavConnections.clear();

        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_KICK_START_DRONESHARE_UPLOADS.equals(action)) {
                for (DroneApi droneApi : droneApiStore) {
                    droneApi.getDroneManager().kickStartDroneShareUpload();
                }
            }
        }

        stopSelf();
        return START_NOT_STICKY;
    }

}
