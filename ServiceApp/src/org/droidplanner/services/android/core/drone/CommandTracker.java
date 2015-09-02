package org.droidplanner.services.android.core.drone;

import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.model.ICommandListener;

/**
 * Created by Fredia Huya-Kouadio on 6/24/15.
 */
public interface CommandTracker {

    void onCommandSubmitted(MAVLinkMessage command, ICommandListener listener);

    void onCommandAck(int commandId, Object ack);
}
