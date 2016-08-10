package org.droidplanner.services.android.core.drone.autopilot.px4;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.drone.action.ParameterActions;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.communication.model.DataLink;
import org.droidplanner.services.android.core.drone.LogMessageListener;
import org.droidplanner.services.android.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.core.drone.variables.HeartBeat;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;
import org.droidplanner.services.android.utils.CommonApiUtils;

/**
 * Created by Fredia Huya-Kouadio on 9/10/15.
 */
public class Px4Native extends GenericMavLinkDrone {

    public Px4Native(String droneId, Context context, Handler handler, DataLink.DataLinkProvider<MAVLinkMessage> mavClient, AutopilotWarningParser warningParser, LogMessageListener logListener) {
        super(droneId, context, handler, mavClient, warningParser, logListener);
    }

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.PX4_NATIVE;
    }

    @Override
    protected HeartBeat initHeartBeat(Handler handler) {
        return new HeartBeat(this, handler);
    }


    @Override
    public boolean executeAsyncAction(Action action, ICommandListener listener) {
        String type = action.getType();
        Bundle data = action.getData();
        if (data == null) {
            data = new Bundle();
        }
        switch (type) {

            case ParameterActions.ACTION_REFRESH_PARAMETERS:
                CommonApiUtils.refreshParameters(this);
                return true;

            default:
                return super.executeAsyncAction(action, listener);
        }

    }

    @Override
    public void onMavLinkMessageReceived(MAVLinkMessage message) {

        getParameterManager().processMessage(message);

        super.onMavLinkMessageReceived(message);
    }

}
