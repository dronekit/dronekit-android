package org.droidplanner.android.client.apis.solo;

import android.os.Bundle;

import org.droidplanner.android.client.Drone;
import org.droidplanner.android.client.apis.Api;
import org.droidplanner.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import org.droidplanner.services.android.lib.model.AbstractCommandListener;
import org.droidplanner.services.android.lib.model.action.Action;

import static org.droidplanner.services.android.lib.drone.companion.solo.action.SoloActions.ACTION_SEND_MESSAGE;
import static org.droidplanner.services.android.lib.drone.companion.solo.action.SoloActions.EXTRA_MESSAGE_DATA;

/**
 * Created by Fredia Huya-Kouadio on 7/31/15.
 */
public abstract class SoloApi extends Api {

    protected final Drone drone;

    protected SoloApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Sends a message to the solo vehicle.
     * @param messagePacket TLV message data.
     * @param listener Register a callback to receive update of the command execution status.
     */
    protected void sendMessage(TLVPacket messagePacket, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MESSAGE_DATA, messagePacket);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SEND_MESSAGE, params), listener);
    }
}
