package org.droidplanner.core.drone;

import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.o3dr.services.android.lib.model.ICommandListener;

/**
 * Created by Fredia Huya-Kouadio on 6/24/15.
 */
public interface CommandTracker {

    void onCommandSubmitted(msg_command_long command, ICommandListener listener);

    void onCommandAcknowledged(msg_command_ack ack);
}
