package org.droidplanner.services.android.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.three_dr.services.android.lib.drone.connection.ConnectionParameter;
import com.three_dr.services.android.lib.model.IDroidPlannerApi;
import com.three_dr.services.android.lib.model.IDroidPlannerApiCallback;
import com.three_dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.DroidPlannerServicesApp;

import java.lang.ref.WeakReference;

/**
 * Created by fhuya on 10/30/14.
 */
public class DroidPlannerService extends Service {

    private static final String TAG = DroidPlannerService.class.getSimpleName();

    private final DPServices dpServices = new DPServices(this);
    private Drone drone;

    @Override
    public IBinder onBind(Intent intent) {
        return dpServices;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        drone = ((DroidPlannerServicesApp)getApplication()).getDrone();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        drone = null;
    }

    private final static class DPServices extends IDroidPlannerServices.Stub {

        private final WeakReference<DroidPlannerService> serviceRef;

        public DPServices(DroidPlannerService service){
            serviceRef = new WeakReference<DroidPlannerService>(service);
        }

        private DroidPlannerService getService(){
            final DroidPlannerService service = serviceRef.get();
            if(service == null)
                throw new IllegalStateException("Lost reference to parent service.");

            return service;
        }

        @Override
        public IDroidPlannerApi connectToDrone(ConnectionParameter params, IDroidPlannerApiCallback callback) throws RemoteException {
            final DroidPlannerService service = getService();
            return new DPApi(service.getApplicationContext(), service.drone, callback);
        }
    }

}
