package org.droidplanner.services.android.drone.companion.solo;

import android.content.Context;
import android.os.Handler;

import org.droidplanner.services.android.utils.Utils;
import org.droidplanner.services.android.utils.connection.AbstractIpConnection;
import org.droidplanner.services.android.utils.connection.IpConnectionListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Fredia Huya-Kouadio on 2/20/15.
 */
public abstract class AbstractLinkManager<T extends AbstractLinkManager.LinkListener> implements IpConnectionListener {

    private static final long RECONNECT_COUNTDOWN = 1000l; //ms

    public interface LinkListener {
        void onLinkConnected();

        void onLinkDisconnected();
    }

    private final Runnable reconnectTask = new Runnable() {
        @Override
        public void run() {
            linkConn.connect();
        }
    };

    private final ExecutorService asyncExecutor;
    protected final Handler handler;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    protected final Context context;
    protected final AbstractIpConnection linkConn;

    private int disconnectTracker = 0;
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

    public boolean isLinkConnected() {
        return this.linkConn.getConnectionStatus() == AbstractIpConnection.STATE_CONNECTED;
    }

    public void start(T listener) {
        disconnectTracker = 0;
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
        disconnectTracker = 0;
        handler.removeCallbacks(reconnectTask);
        if (linkListener != null)
            linkListener.onLinkConnected();
    }

    @Override
    public void onIpDisconnected() {
        if (isStarted.get()) {
            if(shouldReconnect()) {
                //Try to reconnect
                handler.postDelayed(reconnectTask, ++disconnectTracker * RECONNECT_COUNTDOWN);
            }

            if (linkListener != null)
                linkListener.onLinkDisconnected();
        }
    }

    protected boolean shouldReconnect(){
        return true;
    }
}
