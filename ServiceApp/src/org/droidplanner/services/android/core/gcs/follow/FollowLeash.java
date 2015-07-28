package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.geoTools.GeoTools;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public class FollowLeash extends FollowWithRadiusAlgorithm {

    public FollowLeash(MavLinkDrone drone, Handler handler, double radius) {
        super(drone, handler, radius);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEASH;
    }

    @Override
    protected void processNewLocation(Location location) {
        final Coord2D locationCoord = location.getCoord();
        final Coord2D dronePosition = drone.getGps().getPosition();

        if (locationCoord == null || dronePosition == null) {
            return;
        }

        if (GeoTools.getDistance(locationCoord, dronePosition) > radius) {
            double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(locationCoord, dronePosition);
            Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(locationCoord, headingGCStoDrone, radius);
            drone.getGuidedPoint().newGuidedCoord(goCoord);
        }
    }

}
