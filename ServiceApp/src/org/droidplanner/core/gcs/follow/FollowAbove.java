package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;

public class FollowAbove extends FollowAlgorithm {

    public FollowAbove(Drone drone, DroneInterfaces.Handler handler) {
        super(drone, handler);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.ABOVE;
    }

    @Override
    protected void processNewLocation(Location location) {
        Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
        drone.getGuidedPoint().newGuidedCoord(gcsCoord);
    }

}
