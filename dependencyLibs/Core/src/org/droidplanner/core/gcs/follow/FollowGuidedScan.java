package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

/**
 * Created by fhuya on 1/9/15.
 */
public class FollowGuidedScan extends FollowAlgorithm {
    @Override
    public void processNewLocation(Location location) {

    }

    @Override
    public FollowModes getType() {
        return null;
    }

    public FollowGuidedScan(Drone drone, Length length) {
        super(drone, length);
    }
}
