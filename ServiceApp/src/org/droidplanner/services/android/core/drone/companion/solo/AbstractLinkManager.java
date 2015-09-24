package org.droidplanner.services.android.core.drone.companion.solo;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;

import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.utils.connection.AbstractIpConnection;
import org.droidplanner.services.android.utils.connection.IpConnectionListener;
import org.droidplanner.services.android.utils.connection.SshConnection;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 2/20/15.
 */
public abstract class AbstractLinkManager<T extends AbstractLinkManager.LinkListener> implements IpConnectionListener {

    protected static final String SOLO_MAC_ADDRESS_COMMAND = "/sbin/ifconfig wlan0 | awk '/HWaddr/ {print $NF}'";

    protected static final long RECONNECT_COUNTDOWN = 1000l; //ms

    public abstract void refreshState();

    public interface LinkListener {
        void onLinkConnected();

        void onLinkDisconnected();

        void onVersionsUpdated();

        void onMacAddressUpdated();
    }

    private final Runnable reconnectTask = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(reconnectTask);
            linkConn.connect();
        }
    };

    private final Runnable macAddressRetriever = new Runnable() {
        @Override
        public void run() {
            try{
                final String response = getSshLink().execute(SOLO_MAC_ADDRESS_COMMAND);
                final String trimmedResponse = TextUtils.isEmpty(response) ? "" : response.trim();

                setMacAddress(trimmedResponse);
            } catch (IOException e) {
                Timber.e(e, "Error occurred while retrieving sololink mac address");
            }
        }
    };

    private final AtomicReference<String> macAddress = new AtomicReference<>("");

    private final ExecutorService asyncExecutor;
    protected final Handler handler;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final AtomicBoolean wasConnected = new AtomicBoolean(false);

    protected final Context context;
    protected final AbstractIpConnection linkConn;

    private T linkListener;

    public AbstractLinkManager(Context context, AbstractIpConnection ipConn, Handler handler, ExecutorService asyncExecutor) {
        this.context = context;
        this.linkConn = ipConn;
        this.linkConn.setIpConnectionListener(this);

        this.handler = handler;
        this.asyncExecutor = asyncExecutor;
    }

    protected void postAsyncTask(Runnable task){
        if(asyncExecutor != null && !asyncExecutor.isShutdown()){
            asyncExecutor.execute(task);
        }
    }

    protected void postSuccessEvent(final ICommandListener listener){
        if(handler != null && listener != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onSuccess();
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    protected void postTimeoutEvent(final ICommandListener listener){
        if(handler != null && listener != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onTimeout();
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    protected void postErrorEvent(final int error, final ICommandListener listener){
        if(handler != null && listener != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onError(error);
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    protected abstract SshConnection getSshLink();

    public boolean isLinkConnected() {
        return this.linkConn.getConnectionStatus() == AbstractIpConnection.STATE_CONNECTED;
    }

    public void start(T listener) {
        handler.removeCallbacks(reconnectTask);

        isStarted.set(true);
        this.linkConn.connect();

        this.linkListener = listener;
    }

    public void stop() {
        handler.removeCallbacks(reconnectTask);

        isStarted.set(false);

        //Break the link
        this.linkConn.disconnect();
    }

    @Override
    public void onIpConnected() {
        handler.removeCallbacks(reconnectTask);
        wasConnected.set(true);

        refreshState();

        if (linkListener != null)
            linkListener.onLinkConnected();
    }

    @Override
    public void onIpDisconnected() {
        if (isStarted.get()) {
            if(shouldReconnect()) {
                //Try to reconnect
                handler.postDelayed(reconnectTask, RECONNECT_COUNTDOWN);
            }

            if (linkListener != null && wasConnected.get())
                linkListener.onLinkDisconnected();

            wasConnected.set(false);
        }
    }

    protected boolean shouldReconnect(){
        return true;
    }

    protected void loadMacAddress(){
        postAsyncTask(macAddressRetriever);
    }

    private void setMacAddress(String trimmedResponse) {
        Timber.i("Retrieved mac address: %s", trimmedResponse);
        macAddress.set(trimmedResponse);
        if(linkListener != null){
            linkListener.onMacAddressUpdated();
        }
    }

    public String getMacAddress(){
        return macAddress.get();
    }
}
