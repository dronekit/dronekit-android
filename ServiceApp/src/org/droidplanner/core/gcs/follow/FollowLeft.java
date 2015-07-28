package org.droidplanner.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.services.android.drone.autopilot.MavLinkDrone;

public class FollowLeft extends FollowHeadingAngle {

    public FollowLeft(MavLinkDrone drone, Handler handler, double radius) {
        super(drone, handler, radius, -90.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEFT;
    }

}
