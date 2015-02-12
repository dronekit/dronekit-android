package org.droidplanner.services.android.api;

import android.os.Binder;

import org.droidplanner.services.android.drone.DroneManager;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
* Created by fhuya on 11/3/14.
*/
public final class DroneAccess extends Binder {

    private final DroidPlannerService serviceRef;

    DroneAccess(DroidPlannerService service) {
        serviceRef = service;
    }

    public List<DroneApi> getDroneApiList() {
        return new ArrayList<>(serviceRef.droneApiStore.values());
    }
}
