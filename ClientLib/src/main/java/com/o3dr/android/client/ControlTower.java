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

import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.android.client.utils.InstallServiceDialog;
import com.o3dr.android.client.utils.UpdateServiceDialog;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;
import com.o3dr.services.android.lib.util.version.VersionUtils;

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
                final int libVersionCode = o3drServices.getApiVersionCode();
                if (libVersionCode < VersionUtils.LIB_VERSION) {
                    //Prompt the user to update the 3DR Services app.
                    o3drServices = null;
                    promptFor3DRServicesUpdate();
                    context.unbindService(o3drServicesConnection);
                } else {
                    o3drServices.asBinder().linkToDeath(binderDeathRecipient, 0);
                    notifyTowerConnected();
                }
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
                if(connectedApps != null){
                    for(Bundle appInfo: connectedApps){
                        appInfo.setClassLoader(ConnectionParameter.class.getClassLoader());
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return connectedApps;
    }

    public void registerDrone(Drone drone, Handler handler) {
        if(drone == null)
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
            if (is3DRServicesInstalled()) {
                isServiceConnecting.set(context.bindService(serviceIntent, o3drServicesConnection,
                        Context.BIND_AUTO_CREATE));
            } else
                promptFor3DRServicesInstall();
        }
    }

    public void disconnect() {
        if (o3drServices != null) {
            o3drServices.asBinder().unlinkToDeath(binderDeathRecipient, 0);
            o3drServices = null;
        }

        towerListener = null;

        try {
            context.unbindService(o3drServicesConnection);
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while unbinding from 3DR Services.", e);
        }
    }

    String getApplicationId() {
        return context.getPackageName();
    }

    private boolean is3DRServicesInstalled() {
        final ResolveInfo info = context.getPackageManager().resolveService(serviceIntent, 0);
        if (info == null)
            return false;

        this.serviceIntent.setClassName(info.serviceInfo.packageName, info.serviceInfo.name);
        return true;
    }

    private void promptFor3DRServicesInstall() {
        context.startActivity(new Intent(context, InstallServiceDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void promptFor3DRServicesUpdate() {
        context.startActivity(new Intent(context, UpdateServiceDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
