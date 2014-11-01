package org.droidplanner.services.android.communication.service;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import org.droidplanner.services.android.communication.connection.AndroidMavLinkConnection;
import org.droidplanner.services.android.communication.connection.AndroidTcpConnection;
import org.droidplanner.services.android.communication.connection.AndroidUdpConnection;
import org.droidplanner.services.android.communication.connection.BluetoothConnection;
import org.droidplanner.services.android.communication.connection.usb.UsbConnection;
import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkPacket;
import com.google.android.gms.analytics.HitBuilders;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.drone.connection.ConnectionType;

/**
 * Connects to the drone through a mavlink connection, and takes care of sending
 * and/or receiving messages to/from the drone.
 * 
 */
public class MAVLinkService extends Service {

	private static final String TAG = MAVLinkService.class.getSimpleName();

    private final ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection> mavConnections =
            new ConcurrentHashMap<ConnectionParameter, AndroidMavLinkConnection>();

	private final MavLinkServiceApi mServiceApi = new MavLinkServiceApi(this);

	@Override
	public IBinder onBind(Intent intent) {
		return mServiceApi;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

        for(ConnectionParameter connParams: mavConnections.keySet()){
            disconnectMAVConnection(connParams);
        }

        mavConnections.clear();
	}

    private void connectMAVConnection(ConnectionParameter connParams){
        AndroidMavLinkConnection conn = mavConnections.get(connParams);
        if(conn == null){
            //Create a new mavlink connection
            final int connectionType = connParams.getConnectionType();
            switch(connectionType){
                case ConnectionType.TYPE_USB:
                    conn = new UsbConnection(getApplicationContext());
                    break;

                case ConnectionType.TYPE_BLUETOOTH:
                    //Retrieve the bluetooth address to connect to
                    final String bluetoothAddress = connParams.getParamsBundle().getString
                            (ConnectionType.EXTRA_BLUETOOTH_ADDRESS);
                    conn = new BluetoothConnection(getApplicationContext(), bluetoothAddress);
                    break;

                case ConnectionType.TYPE_TCP:
                    //Retrieve the server ip and port
                    final Bundle paramsBundle = connParams.getParamsBundle();
                    final String tcpServerIp = paramsBundle.getString(ConnectionType
                            .EXTRA_TCP_SERVER_IP);
                    final int tcpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_TCP_SERVER_PORT, ConnectionType.DEFAULT_TCP_SERVER_PORT);
                    conn = new AndroidTcpConnection(getApplicationContext(), tcpServerIp,
                            tcpServerPort);
                    break;

                case ConnectionType.TYPE_UDP:
                    final int udpServerPort = connParams.getParamsBundle().getInt(ConnectionType
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

	private void disconnectMAVConnection(ConnectionParameter connParams) {
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

	/**
	 * MavLinkService app api.
	 */
	public static class MavLinkServiceApi extends Binder {

		private final WeakReference<MAVLinkService> mServiceRef;

		MavLinkServiceApi(MAVLinkService service) {
			mServiceRef = new WeakReference<MAVLinkService>(service);
		}

        private MAVLinkService getService(){
            MAVLinkService service = mServiceRef.get();
            if(service == null)
                throw new IllegalStateException("Lost reference to parent service.");

            return service;
        }

		public void sendData(ConnectionParameter connParams, MAVLinkPacket packet) {
            final AndroidMavLinkConnection mavConnection = getService().mavConnections.get
                    (connParams);
            if(mavConnection == null) return;

			if (mavConnection.getConnectionStatus() != MavLinkConnection.MAVLINK_DISCONNECTED) {
				mavConnection.sendMavPacket(packet);
			}
		}

		public int getConnectionStatus(ConnectionParameter connParams) {
            final AndroidMavLinkConnection mavConnection = getService().mavConnections.get
                    (connParams);
            if(mavConnection == null){
				return MavLinkConnection.MAVLINK_DISCONNECTED;
			}

			return mavConnection.getConnectionStatus();
		}

		public void connectMavLink(ConnectionParameter connParams) {
			getService().connectMAVConnection(connParams);
		}

		public void disconnectMavLink(ConnectionParameter connParams) {
			getService().disconnectMAVConnection(connParams);
		}

		public void addMavLinkConnectionListener(ConnectionParameter connParams, String tag,
                                                 MavLinkConnectionListener listener) {
            final AndroidMavLinkConnection mavConnection = getService().mavConnections.get
                    (connParams);
            if(mavConnection == null) return;

			mavConnection.addMavLinkConnectionListener(tag, listener);
		}

		public void removeMavLinkConnectionListener(ConnectionParameter connParams, String tag) {
            final AndroidMavLinkConnection mavConnection = getService().mavConnections.get
                    (connParams);
            if(mavConnection == null) return;

			mavConnection.removeMavLinkConnectionListener(tag);
		}
	}

}
