package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.geoTools.GeoTools;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public abstract class FollowHeadingAngle extends FollowWithRadiusAlgorithm {

    protected double angleOffset;
    protected final MavLinkDrone drone;

    protected FollowHeadingAngle(DroneManager droneMgr, Handler handler, double radius, double angleOffset) {
        super(droneMgr, handler, radius);
        this.angleOffset = angleOffset;

        this.drone = droneMgr.getDrone();
    }

    @Override
    public void processNewLocation(Location location) {
        Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
        double bearing = location.getBearing();

        Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing + angleOffset, radius);
        drone.getGuidedPoint().newGuidedCoord(goCoord);
    }

}