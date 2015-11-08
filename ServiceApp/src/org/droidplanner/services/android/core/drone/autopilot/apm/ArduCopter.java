package org.droidplanner.services.android.core.drone.autopilot.apm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.o3dr.services.android.lib.drone.action.ControlActions;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.drone.LogMessageListener;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.drone.variables.ApmModes;
import org.droidplanner.services.android.core.drone.variables.State;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;
import org.droidplanner.services.android.utils.CommonApiUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduCopter extends ArduPilot {

    private final ConcurrentHashMap<String, ICommandListener> manualControlStateListeners = new ConcurrentHashMap<>();

    public ArduCopter(Context context, MAVLinkStreams.MAVLinkOutputStream mavClient, Handler handler, AutopilotWarningParser warningParser, LogMessageListener logListener, DroneInterfaces.AttributeEventListener listener) {
        super(context, mavClient, handler, warningParser, logListener, listener);
    }

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.ARDU_COPTER;
    }

    @Override
    protected boolean enableManualControl(Bundle data, ICommandListener listener){
        boolean enable = data.getBoolean(ControlActions.EXTRA_DO_ENABLE);
        String appId = data.getString(DroneManager.EXTRA_CLIENT_APP_ID);

        State state = getState();
        ApmModes vehicleMode = state.getMode();
        if(enable){
            if(vehicleMode == ApmModes.ROTOR_GUIDED){
                CommonApiUtils.postSuccessEvent(listener);
            }
            else{
                state.changeFlightMode(ApmModes.ROTOR_GUIDED, listener);
            }

            if(listener != null) {
                manualControlStateListeners.put(appId, listener);
            }
        }
        else{
            manualControlStateListeners.remove(appId);

            if(vehicleMode != ApmModes.ROTOR_GUIDED){
                CommonApiUtils.postSuccessEvent(listener);
            }
            else{
                state.changeFlightMode(ApmModes.ROTOR_LOITER, listener);
            }
        }

        return true;
    }

    @Override
    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event){
        switch(event){
            case MODE:
                //Listen for vehicle mode updates, and update the manual control state listeners appropriately
                ApmModes currentMode = getState().getMode();
                for(ICommandListener listener: manualControlStateListeners.values()) {
                    if (currentMode == ApmModes.ROTOR_GUIDED) {
                        CommonApiUtils.postSuccessEvent(listener);
                    } else {
                        CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                    }
                }
                break;
        }

        super.notifyDroneEvent(event);
    }
}
