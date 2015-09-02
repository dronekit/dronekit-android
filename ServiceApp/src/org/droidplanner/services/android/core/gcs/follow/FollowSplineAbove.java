package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowSplineAbove extends FollowAlgorithm {

    private final MavLinkDrone drone;

    @Override
    public void processNewLocation(Location location) {
        Coord2D gcsLoc = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());

        //TODO: some device (nexus 6) do not report the speed (always 0).. figure out workaround.
        double speed = location.getSpeed();
        double bearing = location.getBearing();
        double bearingInRad = Math.toRadians(bearing);
        double xVel = speed * Math.cos(bearingInRad);
        double yVel = speed * Math.sin(bearingInRad);
        drone.getGuidedPoint().newGuidedCoordAndVelocity(gcsLoc, xVel, yVel, 0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.SPLINE_ABOVE;
    }

    public FollowSplineAbove(DroneManager droneManager, Handler handler) {
        super(droneManager, handler);
        drone = droneManager.getDrone();
    }
}
