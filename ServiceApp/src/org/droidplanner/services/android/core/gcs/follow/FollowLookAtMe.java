package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

/**
 * Created by Fredia Huya-Kouadio on 3/23/15.
 */
public class FollowLookAtMe extends FollowAlgorithm {

    public FollowLookAtMe(DroneManager droneMgr, Handler handler) {
        super(droneMgr, handler);
    }

    @Override
    protected void processNewLocation(Location location) {}

    @Override
    public FollowModes getType() {
        return FollowModes.LOOK_AT_ME;
    }
}
