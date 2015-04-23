package com.o3dr.android.client;

import android.os.RemoteException;

import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.util.version.VersionUtils;

/**
 * Created by fhuya on 12/15/14.
 */
public class DroneApiListener extends IApiListener.Stub {

    private final Drone drone;

    public DroneApiListener(Drone drone){
        this.drone = drone;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) throws RemoteException {
        drone.notifyDroneConnectionFailed(connectionResult);
    }

    @Override
    public int getClientVersionCode() throws RemoteException {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public int getApiVersionCode(){
        return VersionUtils.LIB_VERSION;
    }
}
