package org.droidplanner.services.android.core.gcs.follow;

import android.content.Context;

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
    private static boolean VERBOSE = true;

    static String getLatLongFromLocation(final android.location.Location location) {
        return android.location.Location.convert(location.getLatitude(), android.location.Location.FORMAT_DEGREES) + " " +
                android.location.Location.convert(location.getLongitude(), android.location.Location.FORMAT_DEGREES);
    }

    private final Context mContext;
    private final Follow mFollow;

    private android.location.Location mLastLocation;
    private float mTotalSpeed = 0;
    private int mSpeedReadings = 0;

    public LocationRelay(Context context, Follow follow) {
        mContext = context;
        mFollow = follow;
    }

    public void onFollowStart() {
        mTotalSpeed = 0;
        mSpeedReadings = 0;
    }

    public Location toLocation(android.location.Location location) {
        Location loc = null;
        if(VERBOSE) Timber.d("toLocation(): followLoc=" + location);

        boolean ok = (location.hasAccuracy() && location.hasBearing() && location.getTime() > 0);

        if(!ok) {
            Timber.w("toLocation(): Location needs accuracy, heading, and time");
        }

        if(ok) {
            float distanceToLast = -1.0f;
            long timeSinceLast = -1L;

            final long androidLocationTime = location.getTime();
            if (mLastLocation != null) {
                distanceToLast = location.distanceTo(mLastLocation);
                timeSinceLast = (androidLocationTime - mLastLocation.getTime()) / 1000;
            }

            final float currentSpeed = (distanceToLast > 0f && timeSinceLast > 0)
                    ? (distanceToLast / timeSinceLast)
                    : 0f;

            final boolean isAccurate = isLocationAccurate(location.getAccuracy(), currentSpeed);

            if(VERBOSE) {
                Timber.d(
                        "toLocation(): distancetoLast=%.2f timeToLast=%d currSpeed=%.2f accurate=%s",
                        distanceToLast, timeSinceLast, currentSpeed, isAccurate);
            }

            // Make a new location
            LatLongAlt lla = new LatLongAlt(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude()
            );

            loc = new Location(
                    lla,
                    location.getBearing(),
                    location.getSpeed(),
                    isAccurate,
                    location.getTime());

            mLastLocation = location;

            if(VERBOSE) Timber.d("External location lat/lng=" + getLatLongFromLocation(location));
        }

        return loc;

    }

    private boolean isLocationAccurate(float accuracy, float currentSpeed) {
        if (accuracy >= LOCATION_ACCURACY_THRESHOLD) {
            Timber.w("isLocationAccurate() -- High/bad accuracy: " + accuracy);
            return false;
        }

        mTotalSpeed += currentSpeed;
        float avg = (mTotalSpeed / ++mSpeedReadings);

        //If moving:
        if (currentSpeed > 0) {
            //if average indicates some movement
            if (avg >= 1.0) {
                //Reject unreasonable updates.
                if (currentSpeed >= (avg * JUMP_FACTOR)) {
                    Timber.w("isLocationAccurate() -- High current speed: " + currentSpeed);
                    return false;
                }
            }
        }

        return true;
    }
}
