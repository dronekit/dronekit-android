package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowSplineAbove extends FollowAlgorithm {
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

    public FollowSplineAbove(Drone drone, DroneInterfaces.Handler handler) {
        super(drone, handler);
    }
}
