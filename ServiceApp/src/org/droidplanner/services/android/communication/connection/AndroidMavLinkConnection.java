package org.droidplanner.services.android.communication.connection;

import android.content.Context;

import org.droidplanner.services.android.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.services.android.core.model.Logger;
import org.droidplanner.services.android.utils.AndroidLogger;

public abstract class AndroidMavLinkConnection extends MavLinkConnection {

    protected final Context mContext;

    public AndroidMavLinkConnection(Context applicationContext) {
        this.mContext = applicationContext;
    }

    @Override
    protected final Logger initLogger() {
        return AndroidLogger.getLogger();
    }
}
