package org.droidplanner.services.android.core.gcs.follow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.o3dr.android.client.apis.FollowApi;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.services.android.core.gcs.location.Location;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by kellys on 2/24/16.
 */
public class LocationRelay {
    static final String TAG = LocationRelay.class.getSimpleName();

    private static final float LOCATION_ACCURACY_THRESHOLD = 15.0f;
    private static final float JUMP_FACTOR = 4.0f;

    private static final IntentFilter sLocationEventFilter = new IntentFilter();
    static {
        sLocationEventFilter.addAction(FollowApi.EVT_EXTERNAL_LOCATION);
        sLocationEventFilter.addAction(FollowApi.EVT_LOCATION_AVAILABILITY);
    }

    static String getLatLongFromLocation(final android.location.Location location) {
        return android.location.Location.convert(location.getLatitude(), android.location.Location.FORMAT_DEGREES) + " " +
                android.location.Location.convert(location.getLongitude(), android.location.Location.FORMAT_DEGREES);
    }

    static String dumpExtras(Intent intent) {
        final ArrayList<String> list = new ArrayList<String>();

        for(String ex: intent.getExtras().keySet()) {
            list.add(ex);
        }

        return list.toString();
    }

    static String dumpIntent(Intent intent) {
        return String.format(
                "{locationIntent: lat=%.4f lng=%.4f alt=%.2f acc=%.2f hdg=%.1f spd=%.1f tim=%d}",
                intent.getDoubleExtra(FollowApi.EXTRA_LAT, 0),
                intent.getDoubleExtra(FollowApi.EXTRA_LNG, 0),
                intent.getDoubleExtra(FollowApi.EXTRA_ALTITUDE, 0),
                intent.getFloatExtra(FollowApi.EXTRA_ACCURACY, 0f),
                intent.getFloatExtra(FollowApi.EXTRA_HEADING, 0f),
                intent.getFloatExtra(FollowApi.EXTRA_SPEED, 0f),
                intent.getLongExtra(FollowApi.EXTRA_TIME, 0L)
        );
    }

    static android.location.Location toAndroidLocation(Intent intent) {
        android.location.Location loc = new android.location.Location("explicit");

        loc.setLatitude(intent.getDoubleExtra(FollowApi.EXTRA_LAT, 0));
        loc.setLongitude(intent.getDoubleExtra(FollowApi.EXTRA_LNG, 0));
        loc.setAltitude(intent.getDoubleExtra(FollowApi.EXTRA_ALTITUDE, 0));
        loc.setAccuracy(intent.getFloatExtra(FollowApi.EXTRA_ACCURACY, 10f));
        loc.setBearing(intent.getFloatExtra(FollowApi.EXTRA_HEADING, 0f));
        loc.setSpeed(intent.getFloatExtra(FollowApi.EXTRA_SPEED, 0f));
        loc.setTime(intent.getLongExtra(FollowApi.EXTRA_TIME, 0));

        return loc;
    }

    private final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Timber.d("DroneKit: intent.action=" + action);

            switch(action) {
                case FollowApi.EVT_EXTERNAL_LOCATION: {
                    Location location = toLocation(intent);
                    if(location != null) {
                        mFollow.onLocationUpdate(location);
                    }
                    break;
                }

                case FollowApi.EVT_LOCATION_AVAILABILITY: {
                    if(intent.hasExtra(FollowApi.EXTRA_AVAILABLE)) {
                        boolean available = intent.getBooleanExtra(FollowApi.EXTRA_AVAILABLE, false);
                        if(!available) {
                            mFollow.onLocationUnavailable();
                        }
                    }
                    break;
                }
            }
        }
    };

    private final Context mContext;
    private final Follow mFollow;

    private android.location.Location mLastLocation;
    private float mTotalSpeed = 0;
    private int mSpeedReadings = 0;

    public LocationRelay(Context context, Follow follow) {
        mContext = context;
        mFollow = follow;
    }

    public void unregisterLocationReceiver() {
        try {
            mContext.unregisterReceiver(mLocationReceiver);
        } catch(Throwable ex) { /* ok */ }
    }

    public void registerLocationReceiver() {
        mContext.registerReceiver(mLocationReceiver, sLocationEventFilter);
    }

    public Location toLocation(Intent intent) {
        boolean ok = true;

        Timber.d("toLocation(): intent=" + dumpIntent(intent));

        for(String extra: new String[] {
                FollowApi.EXTRA_LAT, FollowApi.EXTRA_LNG, FollowApi.EXTRA_ACCURACY,
                FollowApi.EXTRA_HEADING, FollowApi.EXTRA_TIME
        }) {
            if(!intent.hasExtra(extra)) {
                Timber.w(String.format(
                        "FAIL: Intent doesn't have '%s'. (It does have %s)",
                        extra, dumpExtras(intent)));
                ok = false;
                break;
            }
        }

        Location loc = null;

        if(ok) {
            android.location.Location androidLocation = toAndroidLocation(intent);

            float distanceToLast = -1.0f;
            long timeSinceLast = -1L;

            final long androidLocationTime = androidLocation.getTime();
            if (mLastLocation != null) {
                distanceToLast = androidLocation.distanceTo(mLastLocation);
                timeSinceLast = (androidLocationTime - mLastLocation.getTime()) / 1000;
            }

            final float currentSpeed = distanceToLast > 0f && timeSinceLast > 0
                    ? (distanceToLast / timeSinceLast)
                    : 0f;

            final boolean isAccurate = true; // TODO: for now! isLocationAccurate(androidLocation.getAccuracy(), currentSpeed);
            Timber.d("Computed isLocationAccurate(): " + isLocationAccurate(androidLocation.getAccuracy(), currentSpeed));

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

            Timber.d("External location lat/lng=" + getLatLongFromLocation(androidLocation));
        }

        return loc;
    }

    private boolean isLocationAccurate(float accuracy, float currentSpeed) {
        if (accuracy >= LOCATION_ACCURACY_THRESHOLD) {
            Timber.w("High/bad accuracy: " + accuracy);
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
                    Timber.w("High current speed: " + currentSpeed);
                    return false;
                }
            }
        }

        return true;
    }
}
