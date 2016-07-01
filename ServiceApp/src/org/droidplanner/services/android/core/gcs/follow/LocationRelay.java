package org.droidplanner.services.android.core.gcs.follow;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.services.android.core.gcs.location.Location;

import timber.log.Timber;

/**
 * Created by kellys on 2/24/16.
 */
public class LocationRelay {
    static final String TAG = LocationRelay.class.getSimpleName();

    private static final float LOCATION_ACCURACY_THRESHOLD = 10.0f;
    private static final float JUMP_FACTOR = 4.0f;
    private static boolean VERBOSE = false;

    public static String getLatLongFromLocation(final android.location.Location location) {
        return android.location.Location.convert(location.getLatitude(), android.location.Location.FORMAT_DEGREES) + " " +
                android.location.Location.convert(location.getLongitude(), android.location.Location.FORMAT_DEGREES);
    }

    private android.location.Location mLastLocation;
    private float mTotalSpeed = 0;
    private int mSpeedReadings = 0;

    public LocationRelay() {
        super();
    }

    public void onFollowStart() {
        mTotalSpeed = 0;
        mSpeedReadings = 0;
        mLastLocation = null;
    }

    /**
     * Convert the specified Android location to a local Location, and track speed/accuracy
     */
    public Location toGcsLocation(android.location.Location androidLocation) {
        Location gcsLocation = null;
        if(VERBOSE) Timber.d("toLocation(): followLoc=" + androidLocation);

        boolean ok = (androidLocation.hasAccuracy() && androidLocation.hasBearing() && androidLocation.getTime() > 0);

        if(!ok) {
            Timber.w("toLocation(): Location needs accuracy, heading, and time");
        }

        if(ok) {
            float distanceToLast = -1.0f;
            long timeSinceLast = -1L;

            final long androidLocationTime = androidLocation.getTime();
            if (mLastLocation != null) {
                distanceToLast = androidLocation.distanceTo(mLastLocation);
                timeSinceLast = (androidLocationTime - mLastLocation.getTime()) / 1000;
            }

            final float currentSpeed = (distanceToLast > 0f && timeSinceLast > 0) ?
                    (distanceToLast / timeSinceLast) : 0f;

            final boolean isAccurate = isLocationAccurate(androidLocation.getAccuracy(), currentSpeed);

            if(VERBOSE) {
                Timber.d(
                        "toLocation(): distancetoLast=%.2f timeToLast=%d currSpeed=%.2f accurate=%s",
                        distanceToLast, timeSinceLast, currentSpeed, isAccurate);
            }

            // Make a new location
            gcsLocation = new Location(
                    new LatLongAlt(
                        androidLocation.getLatitude(),
                        androidLocation.getLongitude(),
                        androidLocation.getAltitude()
                    ),
                    androidLocation.getBearing(),
                    androidLocation.getSpeed(),
                    isAccurate,
                    androidLocation.getTime()
                );

            mLastLocation = androidLocation;

            if(VERBOSE) Timber.d("External location lat/lng=" + getLatLongFromLocation(androidLocation));
        }

        return gcsLocation;
    }

    private boolean isLocationAccurate(float accuracy, float currentSpeed) {
        if (accuracy >= LOCATION_ACCURACY_THRESHOLD) {
            Timber.w("isLocationAccurate() -- High/bad accuracy: " + accuracy);
            return false;
        }

        mTotalSpeed += currentSpeed;
        float avg = (mTotalSpeed / ++mSpeedReadings);

        // If moving:
        if (currentSpeed > 0) {
            // if average indicates some movement
            if (avg >= 1.0) {
                // Reject unreasonable updates.
                if (currentSpeed >= (avg * JUMP_FACTOR)) {
                    Timber.w("isLocationAccurate() -- High current speed: " + currentSpeed);
                    return false;
                }
            }
        }

        return true;
    }
}
