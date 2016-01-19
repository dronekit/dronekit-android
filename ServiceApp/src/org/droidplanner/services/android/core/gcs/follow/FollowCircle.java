package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.services.android.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.helpers.geoTools.GeoTools;

public class FollowCircle extends FollowWithRadiusAlgorithm {
    /**
     * °/s
     */
    private double circleStep = 2;
    private double circleAngle = 0.0;

    public FollowCircle(MavLinkDroneManager droneMgr, Handler handler, double radius, double rate) {
        super(droneMgr, handler, radius);
        circleStep = rate;
    }

    @Override
    public FollowModes getType() {
        return FollowModes.CIRCLE;
    }

    @Override
    public void processNewLocation(Location location) {
        LatLong gcsCoord = new LatLong(location.getCoord());
        LatLong goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, circleAngle, radius);
        circleAngle = MathUtils.constrainAngle(circleAngle + circleStep);
        drone.getGuidedPoint().newGuidedCoord(goCoord);
    }
}
