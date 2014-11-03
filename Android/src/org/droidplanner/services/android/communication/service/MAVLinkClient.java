package org.droidplanner.services.android.communication.service;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;
import org.droidplanner.services.android.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;

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
            if(errMsg != null) {
                final String toastMsg = mMavLinkErrorPrefix + " " + errMsg;
                Log.e(TAG, toastMsg);
            }
        }
    };

    /**
     *  Defines callbacks for service binding, passed to bindService()
     *  */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = (MAVLinkService.MavLinkServiceApi)service;
            onConnectedService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            onDisconnectService();
        }
    };

	private final Context parent;
    private final ConnectionParameter connParams;
	private final MAVLinkStreams.MavlinkInputStream listener;
    private final String mMavLinkErrorPrefix;

    private MAVLinkService.MavLinkServiceApi mService;
	private boolean mIsBound;

	public MAVLinkClient(Context context, MAVLinkStreams.MavlinkInputStream listener,
                         ConnectionParameter connParams) {
		parent = context;
		this.listener = listener;
        mMavLinkErrorPrefix = context.getString(R.string.MAVLinkError);
        this.connParams = connParams;
	}

	private void openConnection() {
        if(mIsBound) {
            connectMavLink();
        }
        else{
            parent.bindService(new Intent(parent, MAVLinkService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }
	}

	private void closeConnection() {
		if (mIsBound) {
            if(mService.getConnectionStatus(this.connParams) == MavLinkConnection.MAVLINK_CONNECTED){
                mService.disconnectMavLink(this.connParams);
            }

            mService.removeMavLinkConnectionListener(this.connParams, TAG);

            // Unbinding the service.
            parent.unbindService(mConnection);
            onDisconnectService();
		}
	}

	@Override
	public void sendMavPacket(MAVLinkPacket pack) {
		if (!isConnected()) {
			return;
		}

        mService.sendData(this.connParams, pack);
	}

    private void connectMavLink(){
        mService.connectMavLink(this.connParams);
        mService.addMavLinkConnectionListener(this.connParams, TAG, mConnectionListener);
    }

	private void onConnectedService() {
        mIsBound = true;
        connectMavLink();
	}

	private void onDisconnectService() {
		mIsBound = false;
		listener.notifyDisconnected();
	}

	@Override
	public void queryConnectionState() {
		if (isConnected()) {
			listener.notifyConnected();
		} else {
			listener.notifyDisconnected();
		}
	}

	@Override
	public boolean isConnected() {
		return mIsBound && mService.getConnectionStatus(this.connParams) == MavLinkConnection.MAVLINK_CONNECTED;
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
