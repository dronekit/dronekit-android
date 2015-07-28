package org.droidplanner.services.android.core.gcs.roi;

import android.os.Handler;

import org.droidplanner.services.android.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.coordinates.Coord3D;
import org.droidplanner.services.android.core.helpers.geoTools.GeoTools;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses location data from Android's FusedLocation LocationManager at 1Hz and
 * calculates new points at 10Hz based on Last Location and Last Velocity.
 */
public class ROIEstimator implements LocationReceiver {

    private static final int TIMEOUT = 100;
    protected Location realLocation;
    protected long timeOfLastLocation;

    protected final MavLinkDrone drone;
    protected Handler watchdog;
    protected Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            updateROI();
        }

    };

    protected final AtomicBoolean isFollowEnabled = new AtomicBoolean(false);

    public ROIEstimator(MavLinkDrone drone, Handler handler) {
        this.watchdog = handler;
        this.drone = drone;
    }

    public void enableFollow() {
        MavLinkDoCmds.resetROI(drone, null);
        isFollowEnabled.set(true);
    }

    public void disableFollow() {
        if (isFollowEnabled.compareAndSet(true, false)) {
            realLocation = null;
            MavLinkDoCmds.resetROI(drone, null);
            disableWatchdog();
        }
    }

    @Override
    public final void onLocationUpdate(Location location) {
        if (!isFollowEnabled.get())
            return;

        realLocation = location;
        timeOfLastLocation = System.currentTimeMillis();

        disableWatchdog();
        updateROI();
    }

    @Override
    public void onLocationUnavailable() {
        disableWatchdog();
    }

    protected void disableWatchdog() {
        watchdog.removeCallbacks(watchdogCallback);
    }

    protected void updateROI() {
        if (realLocation == null) {
            return;
        }

        Coord2D gcsCoord = realLocation.getCoord();

        double bearing = realLocation.getBearing();
        double distanceTraveledSinceLastPoint = realLocation.getSpeed()
                * (System.currentTimeMillis() - timeOfLastLocation) / 1000f;
        Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing, distanceTraveledSinceLastPoint);

        MavLinkDoCmds.setROI(drone, new Coord3D(goCoord.getLat(), goCoord.getLng(), (0.0)), null);

        if (realLocation.getSpeed() > 0)
            watchdog.postDelayed(watchdogCallback, TIMEOUT);
    }

    public boolean isFollowEnabled() {
        return isFollowEnabled.get();
    }
}
