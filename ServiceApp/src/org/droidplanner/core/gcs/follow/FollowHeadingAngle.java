package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.model.Drone;

public abstract class FollowHeadingAngle extends FollowWithRadiusAlgorithm {

    protected double angleOffset;

    protected FollowHeadingAngle(Drone drone, DroneInterfaces.Handler handler, double radius, double angleOffset) {
        super(drone, handler, radius);
        this.angleOffset = angleOffset;
    }

    @Override
    public void processNewLocation(Location location) {
        Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
        double bearing = location.getBearing();

        Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing + angleOffset, radius);
        drone.getGuidedPoint().newGuidedCoord(goCoord);
    }

}