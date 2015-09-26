package org.droidplanner.services.android.core.gcs.location;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager.GoogleApiClientTask;

import org.droidplanner.services.android.core.gcs.location.Location.LocationFinder;
import org.droidplanner.services.android.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.services.android.core.helpers.coordinates.Coord3D;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feeds Location Data from Android's FusedLocation LocationProvider
 */
public class FusedLocation extends LocationCallback implements LocationFinder, GoogleApiClientManager.ManagerListener {

    private static final String TAG = FusedLocation.class.getSimpleName();

    private static final long MIN_TIME_MS = 16;
    private static final float MIN_DISTANCE_M = 0.0f;
    private static final float LOCATION_ACCURACY_THRESHOLD = 15.0f;
    private static final float JUMP_FACTOR = 4.0f;

    private final static Api<? extends Api.ApiOptions.NotRequiredOptions>[] apisList = new Api[]{LocationServices.API};

    private final GoogleApiClientManager gApiMgr;
    private final GoogleApiClientTask requestLocationUpdate;

    private final GoogleApiClientTask removeLocationUpdate = new GoogleApiClientTask() {
        @Override
        protected void doRun() {
            LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(),
                    FusedLocation.this);
        }
    };

    private final Map<String, LocationReceiver> receivers = new ConcurrentHashMap<>();

    private Location mLastLocation;

    private float mTotalSpeed;
    private long mSpeedReadings;

    private final Context context;

    public FusedLocation(Context context, final Handler handler) {
        this(context, handler, LocationRequest.PRIORITY_HIGH_ACCURACY, MIN_TIME_MS, MIN_TIME_MS, MIN_DISTANCE_M);
    }

    public FusedLocation(Context context, final Handler handler, final int locationRequestPriority,
                         final long interval, final long fastestInterval, final float smallestDisplacement){
        this.context = context;

        requestLocationUpdate = new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                final LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(locationRequestPriority);
                locationRequest.setInterval(interval);
                locationRequest.setFastestInterval(fastestInterval);
                locationRequest.setSmallestDisplacement(smallestDisplacement);
                LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(),
                        locationRequest, FusedLocation.this, handler.getLooper());
            }
        };

        gApiMgr = new GoogleApiClientManager(context, handler, apisList);
        gApiMgr.setManagerListener(this);
    }

    @Override
    public void enableLocationUpdates() {
        gApiMgr.start();
        mSpeedReadings = 0;
        mTotalSpeed = 0f;
        mLastLocation = null;
    }

    @Override
    public void disableLocationUpdates() {
        gApiMgr.addTask(removeLocationUpdate);
        gApiMgr.stopSafely();
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        super.onLocationAvailability(locationAvailability);

        //TODO: notify the location listener.
    }

    @Override
    public void onLocationResult(LocationResult result) {
        final Location androidLocation = result.getLastLocation();
        if (androidLocation == null)
            return;

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
        final boolean isLocationAccurate = isLocationAccurate(androidLocation.getAccuracy(), currentSpeed);

        org.droidplanner.services.android.core.gcs.location.Location location = new org.droidplanner.services.android.core.gcs.location.Location(
                new Coord3D(
                        androidLocation.getLatitude(),
                        androidLocation.getLongitude(),
                        androidLocation.getAltitude()),
                androidLocation.getBearing(),
                androidLocation.hasSpeed() ? androidLocation.getSpeed() : currentSpeed,
                isLocationAccurate,
                androidLocationTime);

        mLastLocation = androidLocation;
        notifyLocationUpdate(location);
    }

    private void notifyLocationUpdate(org.droidplanner.services.android.core.gcs.location.Location location){
        if(receivers.isEmpty())
            return;

        for(LocationReceiver receiver: receivers.values()){
            receiver.onLocationUpdate(location);
        }
    }

    private boolean isLocationAccurate(float accuracy, float currentSpeed) {
        if (accuracy >= LOCATION_ACCURACY_THRESHOLD) {
            Log.d(TAG, "High accuracy: " + accuracy);
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
                    Log.d(TAG, "High current speed: " + currentSpeed);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void addLocationListener(String tag, LocationReceiver receiver) {
        receivers.put(tag, receiver);
    }

    @Override
    public void removeLocationListener(String tag){
        receivers.remove(tag);
    }

    @Override
    public void onGoogleApiConnectionError(ConnectionResult result) {
        notifyLocationUnavailable();

        GooglePlayServicesUtil.showErrorNotification(result.getErrorCode(), this.context);
    }

    @Override
    public void onUnavailableGooglePlayServices(int status) {
        notifyLocationUnavailable();

        GooglePlayServicesUtil.showErrorNotification(status, this.context);
    }

    private void notifyLocationUnavailable(){
        if(receivers.isEmpty())
            return;

        for(LocationReceiver listener: receivers.values()){
            listener.onLocationUnavailable();
        }
    }

    @Override
    public void onManagerStarted() {
        gApiMgr.addTask(requestLocationUpdate);
    }

    @Override
    public void onManagerStopped() {
    }
}
