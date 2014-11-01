package org.droidplanner.services.android.api;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerApiCallback;
import com.ox3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.drone.DroneManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fhuya on 10/30/14.
 */
public class DroidPlannerService extends Service {

	private static final String TAG = DroidPlannerService.class.getSimpleName();

	/**
	 * Stores drone instances per connection type.
	 */
	private final ConcurrentHashMap<ConnectionParameter, DroneManager> dronePerConnection = new ConcurrentHashMap<ConnectionParameter, DroneManager>();

	private final DPServices dpServices = new DPServices(this);
	private final DroneAccess droneAccess = new DroneAccess(this);

	private Drone getDroneForConnection(ConnectionParameter params) {
		DroneManager droneMgr = dronePerConnection.get(params);
		if (droneMgr == null) {
			// Create new drone manager
			droneMgr = new DroneManager(getApplicationContext(), params);
			dronePerConnection.put(params, droneMgr);
		}

		return droneMgr.getDrone();
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
			final DroidPlannerService service = getService();
			Drone drone = service.getDroneForConnection(params);
			return new DPApi(service.getApplicationContext(), drone, callback);
		}
	}
}
