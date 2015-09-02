package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public class FollowLead extends FollowHeadingAngle {

    public FollowLead(DroneManager droneMgr, Handler handler, double radius) {
        super(droneMgr, handler, radius, 0.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEAD;
    }

}
