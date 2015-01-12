package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.variables.Altitude;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowSplineLeash extends FollowAlgorithm {
    @Override
    public void processNewLocation(Location location) {
        final Coord3D userLoc = location.getCoord();
        final Coord2D droneLoc = drone.getGps().getPosition();

        if(userLoc == null || droneLoc == null)
            return;

        final double radiusInMeters = radius.valueInMeters();
        if(GeoTools.getDistance(userLoc, droneLoc).valueInMeters() > radiusInMeters){
            double headingGCSToDrone = GeoTools.getHeadingFromCoordinates(userLoc, droneLoc);
            Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(userLoc, headingGCSToDrone, radiusInMeters);

            double speed = location.getSpeed();
            double bearing = location.getBearing();
            double bearingInRad = Math.toRadians(bearing);
            double xVel = speed * Math.cos(bearingInRad);
            double yVel = speed * Math.sin(bearingInRad);
            drone.getGuidedPoint().newGuidedCoordAndVelocity(goCoord, xVel, yVel, 0);
        }

    }

    @Override
    public FollowModes getType() {
        return FollowModes.SPLINE_LEASH;
    }

    public FollowSplineLeash(Drone drone, Length length) {
        super(drone, length);
    }
}
