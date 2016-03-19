package org.droidplanner.services.android.core.gcs.follow;

import android.content.Context;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.gcs.follow.FollowLocation;

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

    public static android.location.Location toAndroidLocation(FollowLocation fl) {
        android.location.Location loc = new android.location.Location("follow");

        loc.setLatitude(fl.getLat());
        loc.setLongitude(fl.getLng());
        loc.setAltitude(fl.getAltitude());
        loc.setAccuracy(fl.getAccuracy());
        loc.setBearing(fl.getHeading());
        loc.setSpeed(fl.getSpeed());
        loc.setTime(fl.getTime());

        return loc;
    }

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

    public Location toLocation(FollowLocation fl) {
        Location loc = null;
        if(VERBOSE) Timber.d("toLocation(): followLoc=" + fl);

        boolean ok = (fl.hasAccuracy() && fl.hasHeading() && fl.hasTime());

        if(!ok) {
            Timber.w("toLocation(): Location needs accuracy, heading, and time");
        }

        if(ok) {
            android.location.Location androidLocation = toAndroidLocation(fl);

            float distanceToLast = -1.0f;
            long timeSinceLast = -1L;

            final long androidLocationTime = androidLocation.getTime();
            if (mLastLocation != null) {
                distanceToLast = androidLocation.distanceTo(mLastLocation);
                timeSinceLast = (androidLocationTime - mLastLocation.getTime()) / 1000;
            }

            final float currentSpeed = (distanceToLast > 0f && timeSinceLast > 0)
                    ? (distanceToLast / timeSinceLast)
                    : 0f;

            final boolean isAccurate = isLocationAccurate(androidLocation.getAccuracy(), currentSpeed);

            if(VERBOSE) {
                Timber.d(
                        "toLocation(): distancetoLast=%.2f timeToLast=%d currSpeed=%.2f accurate=%s",
                        distanceToLast, timeSinceLast, currentSpeed, isAccurate);
            }

            // Make a new location
            LatLongAlt lla = new LatLongAlt(
                    androidLocation.getLatitude(),
                    androidLocation.getLongitude(),
                    androidLocation.getAltitude()
            );

            loc = new Location(
                    lla,
                    androidLocation.getBearing(),
                    androidLocation.getSpeed(),
                    isAccurate,
                    androidLocation.getTime());

            mLastLocation = androidLocation;

            if(VERBOSE) Timber.d("External location lat/lng=" + getLatLongFromLocation(androidLocation));
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
