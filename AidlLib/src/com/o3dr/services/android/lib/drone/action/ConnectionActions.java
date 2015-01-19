package com.o3dr.services.android.lib.drone.action;

import android.os.Bundle;

import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.Utils;

/**
 * Contains builder methods use to generate connect or disconnect actions.
 */
public class ConnectionActions {

    public static final String ACTION_CONNECT = Utils.PACKAGE_NAME + ".action.CONNECT";
    public static final String EXTRA_CONNECT_PARAMETER = "extra_connect_parameter";

    public static final String ACTION_DISCONNECT = Utils.PACKAGE_NAME + ".action.DISCONNECT";

    /**
     * Generate action used to establish connection with the device.
     * @param parameter parameter for the connection.
     */
    public static Action buildConnect(ConnectionParameter parameter){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_CONNECT_PARAMETER, parameter);
        return new Action(ACTION_CONNECT, params);
    }

    /**
     * Generate action used to break connection with the device.
     */
    public static Action buildDisconnect(){
        return new Action(ACTION_DISCONNECT);
    }
}
