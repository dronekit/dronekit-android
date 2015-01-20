package com.o3dr.android.client.apis.drone;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.drone.property.Parameters;

import static com.o3dr.services.android.lib.drone.action.ParameterActions.*;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class ParameterApi {

    /**
     * Generate action used to refresh the parameters for the connected drone.
     * @param drone
     */
    public static void refreshParameters(Drone drone){
        drone.performAsyncAction(new Action(ACTION_REFRESH_PARAMETERS));
    }

    /**
     * Generate action used to write the given parameters to the connected drone.
     * @param parameters parameters to write to the drone.
     * @return
     */
    public static void writeParameters(Drone drone, Parameters parameters){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_PARAMETERS, parameters);
        drone.performAsyncAction(new Action(ACTION_WRITE_PARAMETERS, params));
    }
}
