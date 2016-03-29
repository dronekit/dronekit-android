package org.droidplanner.services.android.communication.connection;

import android.content.Context;

import org.droidplanner.services.android.utils.connection.WifiConnectionHandler;

import java.io.IOException;

/**
 * Created by fredia on 3/28/16.
 */
public abstract class AndroidIpConnection extends AndroidMavLinkConnection {

    private final WifiConnectionHandler wifiHandler;

    public AndroidIpConnection(Context applicationContext) {
        this(applicationContext, null);
    }

    protected AndroidIpConnection(Context context, WifiConnectionHandler wifiHandler){
        super(context);
        this.wifiHandler = wifiHandler;
    }

    @Override
    protected final void openConnection() throws IOException {
        if(this.wifiHandler != null) {
            this.wifiHandler.start();
        }
        onOpenConnection();
    }

    protected abstract void onOpenConnection() throws IOException;

    @Override
    protected final void closeConnection() throws IOException {
        onCloseConnection();
        if(this.wifiHandler != null) {
            this.wifiHandler.stop();
        }
    }

    protected abstract void onCloseConnection() throws IOException;

}
