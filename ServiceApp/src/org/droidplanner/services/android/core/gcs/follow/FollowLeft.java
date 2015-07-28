package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public class FollowLeft extends FollowHeadingAngle {

    public FollowLeft(MavLinkDrone drone, Handler handler, double radius) {
        super(drone, handler, radius, -90.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEFT;
    }

}
