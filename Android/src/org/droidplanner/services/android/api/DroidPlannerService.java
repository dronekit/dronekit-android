package org.droidplanner.services.android.api;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerApiCallback;
import com.ox3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.exception.ConnectionException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fhuya on 10/30/14.
 */
public class DroidPlannerService extends Service {

    private static final String CLAZZ_NAME = DroidPlannerService.class.getName();
	private static final String TAG = DroidPlannerService.class.getSimpleName();

	/**
	 * Stores drone instances per connection type.
	 */
	private final ConcurrentHashMap<ConnectionParameter, DroneManager> dronePerConnection = new ConcurrentHashMap<ConnectionParameter, DroneManager>();

    private final ConcurrentHashMap<ConnectionParameter, ConcurrentHashMap<IBinder,
            IDroidPlannerApi>> dpApisCache = new ConcurrentHashMap<ConnectionParameter,
            ConcurrentHashMap<IBinder, IDroidPlannerApi>>();

	private final DPServices dpServices = new DPServices(this);
	private final DroneAccess droneAccess = new DroneAccess(this);

    IDroidPlannerApi connectToApi(ConnectionParameter connParams, IDroidPlannerApiCallback callback)
            throws RemoteException {

        ConcurrentHashMap<IBinder, IDroidPlannerApi> binderApis = dpApisCache.putIfAbsent
                (connParams, new ConcurrentHashMap<IBinder, IDroidPlannerApi>());

        //Check if a droidplanner api was already generated for this binder.
        return binderApis.putIfAbsent(callback.asBinder(), new DPApi(this, connParams, callback));
    }

    boolean disconnectFromApi(ConnectionParameter connParams, IDroidPlannerApiCallback callback){
        ConcurrentHashMap<IBinder, IDroidPlannerApi> binderApis = dpApisCache.get(connParams);
        if(binderApis == null)
            return false;

        boolean wasRemoved = binderApis.remove(callback.asBinder()) != null;
        if(binderApis.isEmpty()) {
            dpApisCache.remove(connParams);

            //Remove the cached drone manager as well.
            DroneManager droneMgr = dronePerConnection.remove(connParams);
            droneMgr.destroy();
        }

        return wasRemoved;
    }

    DroneManager getDroneForConnection(ConnectionParameter params) throws ConnectionException {
		return dronePerConnection.putIfAbsent(params, new DroneManager(getApplicationContext(),
                params));
	}

	@Override
	public IBinder onBind(Intent intent) {
		final String action = intent.getAction();
		if (IDroidPlannerServices.class.getName().equals(action)) {
			// Return binder to ipc client-server interaction.
			return dpServices;
		} else {
			// Return binder to the service.
			return droneAccess;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		for (DroneManager droneMgr : dronePerConnection.values()) {
			final Drone drone = droneMgr.getDrone();
			if (drone.getMavClient().isConnected()) {
				drone.getMavClient().toggleConnectionState();
			}
		}
	}

	public static final class DroneAccess extends Binder {

		private final WeakReference<DroidPlannerService> serviceRef;

		private DroneAccess(DroidPlannerService service) {
			serviceRef = new WeakReference<DroidPlannerService>(service);
		}

        private DroidPlannerService getService() {
            final DroidPlannerService service = serviceRef.get();
            if (service == null)
                throw new IllegalStateException("Lost reference to parent service.");

            return service;
        }

        public List<DroneManager> getDroneManagerList(){
            return new ArrayList<DroneManager>(getService().dronePerConnection.values());
        }
	}

	private final static class DPServices extends IDroidPlannerServices.Stub {

		private final WeakReference<DroidPlannerService> serviceRef;

		private DPServices(DroidPlannerService service) {
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
}
