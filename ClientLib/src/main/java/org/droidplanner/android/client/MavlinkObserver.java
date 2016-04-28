package org.droidplanner.android.client;

import org.droidplanner.services.android.lib.mavlink.MavlinkMessageWrapper;
import org.droidplanner.services.android.lib.model.IMavlinkObserver;

/**
 * Allows to register for mavlink message updates.
 */
public abstract class MavlinkObserver extends IMavlinkObserver.Stub {

    @Override
    public abstract void onMavlinkMessageReceived(MavlinkMessageWrapper mavlinkMessageWrapper);
}
