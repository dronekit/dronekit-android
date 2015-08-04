package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public class FollowAbove extends FollowAlgorithm {

    protected final MavLinkDrone drone;

    public FollowAbove(DroneManager droneMgr, Handler handler) {
        super(droneMgr, handler);
        this.drone = droneMgr.getDrone();
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
