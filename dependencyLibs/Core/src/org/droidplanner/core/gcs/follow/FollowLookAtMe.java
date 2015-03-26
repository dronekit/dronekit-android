package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.model.Drone;

/**
 * Created by Fredia Huya-Kouadio on 3/23/15.
 */
public class FollowLookAtMe extends FollowAlgorithm {

    public FollowLookAtMe(Drone drone, DroneInterfaces.Handler handler) {
        super(drone, handler);
    }

    @Override
    protected void processNewLocation(Location location) {}

    @Override
    public FollowModes getType() {
        return FollowModes.LOOK_AT_ME;
    }
}
