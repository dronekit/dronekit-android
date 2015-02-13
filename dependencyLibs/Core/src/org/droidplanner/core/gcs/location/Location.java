package org.droidplanner.core.gcs.location;

import org.droidplanner.core.helpers.coordinates.Coord3D;

public class Location {

    public interface LocationReceiver {
        public void onLocationUpdate(Location location);

        public void onLocationUnavailable();
    }

    public interface LocationFinder {
        public void enableLocationUpdates();

        public void disableLocationUpdates();

        public void setLocationListener(LocationReceiver receiver);
    }

    private Coord3D coordinate;
    private double heading = 0.0;
    private double speed = 0.0;
    private boolean isAccurate;
    private long fixTime;

    public Location(Coord3D coord3d, float heading, float speed, boolean isAccurate, long fixTime) {
        coordinate = coord3d;
        this.heading = heading;
        this.speed = speed;
        this.isAccurate = isAccurate;
        this.fixTime = fixTime;
    }

    public Coord3D getCoord() {
        return coordinate;
    }

    public boolean isAccurate() {
        return this.isAccurate;
    }

    public double getBearing() {
        return heading;
    }

    public double getSpeed() {
        return speed;
    }

    public long getFixTime() {
        return fixTime;
    }

}
