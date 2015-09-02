package com.o3dr.android.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.o3dr.android.client.apis.ApiAvailability;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fhuya on 11/12/14.
 */
public class ControlTower {

    private static final String TAG = ControlTower.class.getSimpleName();

    private final Intent serviceIntent = new Intent(IDroidPlannerServices.class.getName());

    private final IBinder.DeathRecipient binderDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            notifyTowerDisconnected();
        }
    };

    private final ServiceConnection o3drServicesConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isServiceConnecting.set(false);

            o3drServices = IDroidPlannerServices.Stub.asInterface(service);
            try {
                o3drServices.asBinder().linkToDeath(binderDeathRecipient, 0);
                notifyTowerConnected();
            } catch (RemoteException e) {
                notifyTowerDisconnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceConnecting.set(false);
            notifyTowerDisconnected();
        }
    };

    private final AtomicBoolean isServiceConnecting = new AtomicBoolean(false);

    private final Context context;
    private TowerListener towerListener;
    private IDroidPlannerServices o3drServices;

    public ControlTower(Context context) {
        this.context = context;
    }

    IDroidPlannerServices get3drServices() {
        return o3drServices;
    }

    public boolean isTowerConnected() {
        return o3drServices != null && o3drServices.asBinder().pingBinder();
    }

    void notifyTowerConnected() {
        if (towerListener == null)
            return;

        towerListener.onTowerConnected();
    }

    void notifyTowerDisconnected() {
        if (towerListener == null)
            return;

        towerListener.onTowerDisconnected();
    }

    public Bundle[] getConnectedApps() {
        Bundle[] connectedApps = new Bundle[0];
        if (isTowerConnected()) {
            try {
                connectedApps = o3drServices.getConnectedApps(getApplicationId());
                if (connectedApps != null) {
                    final ClassLoader classLoader = ConnectionParameter.class.getClassLoader();
                    for (Bundle appInfo : connectedApps) {
                        appInfo.setClassLoader(classLoader);
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return connectedApps;
    }

    public void registerDrone(Drone drone, Handler handler) {
        if (drone == null)
            return;

        if (!isTowerConnected())
            throw new IllegalStateException("Control Tower must be connected.");

        drone.init(this, handler);
        drone.start();
    }

    public void unregisterDrone(Drone drone) {
        if (drone != null)
            drone.destroy();
    }

    public void connect(TowerListener listener) {
        if (towerListener != null && (isServiceConnecting.get() || isTowerConnected()))
            return;

        if (listener == null) {
            throw new IllegalArgumentException("ServiceListener argument cannot be null.");
        }

        towerListener = listener;

        if (!isTowerConnected() && !isServiceConnecting.get()) {
            final int apiAvailableResult = ApiAvailability.getInstance().checkApiAvailability(context);

            switch(apiAvailableResult){
                case ApiAvailability.API_AVAILABLE:
                    final ResolveInfo info = context.getPackageManager().resolveService(serviceIntent, 0);
                    if (info != null) {
                        serviceIntent.setClassName(info.serviceInfo.packageName, info.serviceInfo.name);
                        isServiceConnecting.set(context.bindService(serviceIntent, o3drServicesConnection,
                                Context.BIND_AUTO_CREATE));
                    }
                    break;

                default:
                    ApiAvailability.getInstance().showErrorDialog(context, apiAvailableResult);
                    break;
            }
        }
    }

    public void disconnect() {
        if (o3drServices != null) {
            o3drServices.asBinder().unlinkToDeath(binderDeathRecipient, 0);
            o3drServices = null;
        }

        notifyTowerDisconnected();

        towerListener = null;

        try {
            context.unbindService(o3drServicesConnection);
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while unbinding from 3DR Services.");
        }
    }

    String getApplicationId() {
        return context.getPackageName();
    }
}
