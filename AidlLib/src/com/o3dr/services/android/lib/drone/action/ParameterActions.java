package com.o3dr.services.android.lib.drone.action;

import android.os.Bundle;

import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.Utils;
import com.o3dr.services.android.lib.drone.property.Parameters;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class ParameterActions {

    public static final String ACTION_REFRESH_PARAMETERS = Utils.PACKAGE_NAME + ".action.REFRESH_PARAMETERS";

    public static final String ACTION_WRITE_PARAMETERS = Utils.PACKAGE_NAME + ".action.WRITE_PARAMETERS";
    public static final String EXTRA_PARAMETERS = "extra_parameters";

    /**
     * Generate action used to refresh the parameters for the connected drone.
     */
    public static Action buildParametersRefresh(){
        return new Action(ACTION_REFRESH_PARAMETERS);
    }

    /**
     * Generate action used to write the given parameters to the connected drone.
     * @param parameters parameters to write to the drone.
     * @return
     */
    public static Action buildParametersWrite(Parameters parameters){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_PARAMETERS, parameters);
        return new Action(ACTION_WRITE_PARAMETERS, params);
    }
}
