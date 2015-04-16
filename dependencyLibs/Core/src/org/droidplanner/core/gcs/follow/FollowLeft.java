package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;

public class FollowLeft extends FollowHeadingAngle {

    public FollowLeft(Drone drone, DroneInterfaces.Handler handler, double radius) {
        super(drone, handler, radius, -90.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEFT;
    }

}
