package org.droidplanner.services.android.core.gcs.location;

import org.droidplanner.services.android.core.helpers.coordinates.Coord3D;

public class Location {

    public interface LocationReceiver {
        void onLocationUpdate(Location location);

        void onLocationUnavailable();
    }

    public interface LocationFinder {
        void enableLocationUpdates();

        void disableLocationUpdates();

        void addLocationListener(String tag, LocationReceiver receiver);

        void removeLocationListener(String tag);
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
        return !isInvalid() && this.isAccurate;
    }

    private boolean isInvalid(){
        return this.coordinate == null || (this.coordinate.getLat() == 0 && this.coordinate.getLng() == 0);
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
