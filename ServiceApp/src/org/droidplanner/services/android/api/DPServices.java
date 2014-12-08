package org.droidplanner.services.android.api;

import android.os.RemoteException;
import android.util.Log;

import com.o3dr.services.android.lib.model.IDroidPlannerServices;
import com.o3dr.services.android.lib.model.IDroneApi;

import java.lang.ref.WeakReference;

/**
 * Created by fhuya on 11/3/14.
 */
final class DPServices extends IDroidPlannerServices.Stub {

    private final static String TAG = DPServices.class.getSimpleName();

    private final WeakReference<DroidPlannerService> serviceRef;

    DPServices(DroidPlannerService service) {
        serviceRef = new WeakReference<DroidPlannerService>(service);
    }

    private DroidPlannerService getService() {
        final DroidPlannerService service = serviceRef.get();
        if (service == null)
            throw new IllegalStateException("Lost reference to parent service.");

        return service;
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    @Override
    public IDroneApi acquireDroneApi() throws RemoteException {
        return getService().acquireDroidPlannerApi();
    }

    @Override
    public void releaseDroneApi(IDroneApi dpApi) throws RemoteException {
        Log.d(TAG, "Releasing acquired drone api handle.");
        if(dpApi instanceof DroneApi) {
            getService().releaseDroidPlannerApi((DroneApi) dpApi);
        }
    }
}
