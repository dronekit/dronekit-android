package org.droidplanner.services.android.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.three_dr.services.android.lib.drone.connection.ConnectionParameter;
import com.three_dr.services.android.lib.drone.property.Mission;
import com.three_dr.services.android.lib.drone.property.Parameters;
import com.three_dr.services.android.lib.model.IDroidPlannerCallbackApi;
import com.three_dr.services.android.lib.model.IDroidPlannerWriterApi;

import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.DroidPlannerServicesApp;

import java.lang.ref.WeakReference;

/**
 * Created by fhuya on 10/30/14.
 */
public class DPWriterApiService extends Service {

    private final DPWriterApi dpWriterApi = new DPWriterApi(this);
    private Drone drone;

    @Override
    public IBinder onBind(Intent intent) {
        return dpWriterApi;
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

    public static final class DPWriterApi extends IDroidPlannerWriterApi.Stub {

        private final WeakReference<DPWriterApiService> serviceRef;

        public DPWriterApi(DPWriterApiService service){
            serviceRef = new WeakReference<DPWriterApiService>(service);
        }

        private DPWriterApiService getService(){
            final DPWriterApiService service = serviceRef.get();
            if(service == null)
                throw new IllegalStateException("Lost reference to parent service.");

            return service;
        }

        @Override
        public void connectToDrone(ConnectionParameter params, IDroidPlannerCallbackApi callback) throws RemoteException {

        }

        @Override
        public void changeVehicleMode(int droneId, String newModeName) throws RemoteException {

        }

        @Override
        public void disconnectFromDrone(int droneId, IDroidPlannerCallbackApi callback) throws RemoteException {

        }

        @Override
        public void refreshParameters(int droneId) throws RemoteException {

        }

        @Override
        public void writeParameters(int droneId, Parameters parameters) throws RemoteException {

        }

        @Override
        public void sendMission(int droneId, Mission mission) throws RemoteException {

        }
    }
}
