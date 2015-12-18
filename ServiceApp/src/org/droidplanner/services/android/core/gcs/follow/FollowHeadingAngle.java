package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.services.android.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.helpers.geoTools.GeoTools;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public abstract class FollowHeadingAngle extends FollowWithRadiusAlgorithm {

    protected double angleOffset;
    protected final MavLinkDrone drone;

    protected FollowHeadingAngle(MavLinkDroneManager droneMgr, Handler handler, double radius, double angleOffset) {
        super(droneMgr, handler, radius);
        this.angleOffset = angleOffset;

        this.drone = droneMgr.getDrone();
    }

    @Override
    public void processNewLocation(Location location) {
        LatLong gcsCoord = new LatLong(location.getCoord());
        double bearing = location.getBearing();

        LatLong goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing + angleOffset, radius);
        drone.getGuidedPoint().newGuidedCoord(goCoord);
    }

}