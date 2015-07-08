package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;

public class FollowLead extends FollowHeadingAngle {

    public FollowLead(Drone drone, DroneInterfaces.Handler handler, double radius) {
        super(drone, handler, radius, 0.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEAD;
    }

}
