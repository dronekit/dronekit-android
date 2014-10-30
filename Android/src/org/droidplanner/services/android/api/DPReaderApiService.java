package org.droidplanner.services.android.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.three_dr.services.android.lib.drone.property.Altitude;
import com.three_dr.services.android.lib.drone.property.Attitude;
import com.three_dr.services.android.lib.drone.property.Battery;
import com.three_dr.services.android.lib.drone.property.Gps;
import com.three_dr.services.android.lib.drone.property.Home;
import com.three_dr.services.android.lib.drone.property.Mission;
import com.three_dr.services.android.lib.drone.property.Parameters;
import com.three_dr.services.android.lib.drone.property.Speed;
import com.three_dr.services.android.lib.drone.property.State;
import com.three_dr.services.android.lib.model.IDroidPlannerReaderApi;

import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.DroidPlannerServicesApp;

import java.lang.ref.WeakReference;

/**
 * Created by fhuya on 10/30/14.
 */
public class DPReaderApiService extends Service {

    private final DPReaderApi dpReaderApi = new DPReaderApi(this);
    private Drone drone;

    @Override
    public IBinder onBind(Intent intent) {
        return dpReaderApi;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        drone = ((DroidPlannerServicesApp) getApplication()).getDrone();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        drone = null;
    }

    public final static class DPReaderApi extends IDroidPlannerReaderApi.Stub {

        private final WeakReference<DPReaderApiService> serviceRef;

        public DPReaderApi(DPReaderApiService service){
            serviceRef = new WeakReference<DPReaderApiService>(service);
        }

        private DPReaderApiService getService(){
            final DPReaderApiService service = serviceRef.get();
            if(service == null)
                throw new IllegalStateException("Lost reference to parent service.");

            return service;
        }

        @Override
        public Gps getGps(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public State getState(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public Parameters getParameters(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public Speed getSpeed(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public Attitude getAttitude(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public Home getHome(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public Battery getBattery(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public Altitude getAltitude(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public Mission getMission(int droneId) throws RemoteException {
            return null;
        }

        @Override
        public boolean isConnected(int droneId) throws RemoteException {
            return false;
        }
    }
}
