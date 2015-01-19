package com.o3dr.services.android.lib.drone.action;

import android.os.Bundle;

import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class ExperimentalActions {

    public static final String ACTION_TRIGGER_CAMERA = Utils.PACKAGE_NAME + ".action.TRIGGER_CAMERA";

    public static final String ACTION_EPM_COMMAND = Utils.PACKAGE_NAME + ".action.EPM_COMMAND";
    public static final String EXTRA_EPM_RELEASE = Utils.PACKAGE_NAME + "extra_epm_release";

    public static final String ACTION_SEND_MAVLINK_MESSAGE = Utils.PACKAGE_NAME + ".action.SEND_MAVLINK_MESSAGE";
    public static final String EXTRA_MAVLINK_MESSAGE = "extra_mavlink_message";

    public static Action buildCameraTrigger(){
        return new Action(ACTION_TRIGGER_CAMERA);
    }

    public static Action buildEpmCommand(boolean release){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_EPM_RELEASE, release);
        return new Action(ACTION_EPM_COMMAND, params);
    }

    /**
     * This is an advanced/low-level method to send raw mavlink to the vehicle.
     *
     * This method is included as an ‘escape hatch’ to allow developers to make progress if we’ve
     * somehow missed providing some essentential operation in the rest of this API. Callers do
     * not need to populate sysId/componentId/crc in the packet, this method will take care of that
     * before sending.
     *
     * If you find yourself needing to use this mathod please contact the drone-platform google
     * group and we’ll see if we can support the operation you needed in some future revision of
     * the API.
     *
     * @param messageWrapper A MAVLinkMessage wrapper instance. No need to fill in
     *                       sysId/compId/seqNum - the API will take care of that.
     */
    public static Action buildMavlinkMessage(MavlinkMessageWrapper messageWrapper){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MAVLINK_MESSAGE, messageWrapper);
        return new Action(ACTION_SEND_MAVLINK_MESSAGE, params);
    }
}
