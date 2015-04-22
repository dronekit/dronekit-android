package com.o3dr.android.client.apis.drone;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_CONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_DISCONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.EXTRA_CONNECT_PARAMETER;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class ConnectApi {

    /**
     * Establish connection with the vehicle.
     *
     * @param parameter parameter for the connection.
     * @param drone
     * @param parameter
     */
    public static boolean connect(Drone drone, ConnectionParameter parameter) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_CONNECT_PARAMETER, parameter);
        Action connectAction = new Action(ACTION_CONNECT, params);
        return drone.performAsyncAction(connectAction);
    }

    /**
     * Break connection with the vehicle.
     */
    public static boolean disconnect(Drone drone) {
        return drone.performAsyncAction(new Action(ACTION_DISCONNECT));
    }
}
