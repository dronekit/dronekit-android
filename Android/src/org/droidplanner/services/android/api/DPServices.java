package org.droidplanner.services.android.api;

import android.os.RemoteException;
import android.util.Log;

import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.coordinate.LatLongAlt;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerApiCallback;
import com.ox3dr.services.android.lib.model.IDroidPlannerServices;

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
    public IDroidPlannerApi connectToDrone(ConnectionParameter params,
                                           IDroidPlannerApiCallback callback) throws RemoteException {
        return getService().connectToApi(params, callback);
    }
}
