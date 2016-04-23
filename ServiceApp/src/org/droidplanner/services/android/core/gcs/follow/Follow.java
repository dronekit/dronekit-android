package org.droidplanner.services.android.core.gcs.follow;

import android.content.Context;
import android.os.Handler;

import com.o3dr.services.android.lib.drone.action.ControlActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.gcs.follow.FollowLocation;

import org.droidplanner.services.android.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.core.drone.autopilot.apm.solo.ArduSolo;
import org.droidplanner.services.android.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.core.drone.variables.State;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.gcs.location.Location.LocationFinder;
import org.droidplanner.services.android.core.gcs.location.Location.LocationReceiver;

import timber.log.Timber;

public class Follow implements OnDroneListener<MavLinkDrone>, LocationReceiver {

    private static final String TAG = Follow.class.getSimpleName();
    private Location lastLocation;
    private boolean mUseExternalLocations = false;

    /**
     * Set of return value for the 'toggleFollowMeState' method.
     */
    public enum FollowStates {
        FOLLOW_INVALID_STATE, FOLLOW_DRONE_NOT_ARMED, FOLLOW_DRONE_DISCONNECTED, FOLLOW_START, FOLLOW_RUNNING, FOLLOW_END
    }

    private FollowStates state = FollowStates.FOLLOW_INVALID_STATE;
    private final MavLinkDroneManager droneMgr;

    private final LocationFinder locationFinder;
    private FollowAlgorithm followAlgorithm;
    private final LocationRelay mLocationRelay;

    public Follow(Context context, MavLinkDroneManager droneMgr, Handler handler, LocationFinder locationFinder) {
        this.droneMgr = droneMgr;
        final MavLinkDrone drone = droneMgr.getDrone();
        if(drone != null)
            drone.addDroneListener(this);

        followAlgorithm = (drone instanceof ArduSolo)?
                FollowAlgorithm.FollowModes.SOLO_SHOT.getAlgorithmType(droneMgr, handler):
                FollowAlgorithm.FollowModes.LEASH.getAlgorithmType(droneMgr, handler);

        this.locationFinder = locationFinder;
        locationFinder.addLocationListener(TAG, this);

        mLocationRelay = new LocationRelay(context, this);
    }

    public void toggleFollowMeState(boolean useExternal) {
        final MavLinkDrone drone = droneMgr.getDrone();
        final State droneState = drone == null ? null : drone.getState();
        if (droneState == null) {
            state = FollowStates.FOLLOW_INVALID_STATE;
            return;
        }

        if (isEnabled()) {
            disableFollowMe();
        } else {
            if (droneMgr.isConnected()) {
                if (droneState.isArmed()) {
                    enableFollowMe();
                } else {
                    state = FollowStates.FOLLOW_DRONE_NOT_ARMED;
                }
            } else {
                state = FollowStates.FOLLOW_DRONE_DISCONNECTED;
            }
        }
    }

    private void enableFollowMe() {
        GuidedPoint.changeToGuidedMode(droneMgr.getDrone(), null);

        state = FollowStates.FOLLOW_START;

        mLocationRelay.onFollowStart();

        if(!mUseExternalLocations) {
            locationFinder.enableLocationUpdates();
        }

        followAlgorithm.enableFollow();

        droneMgr.onAttributeEvent(AttributeEvent.FOLLOW_START, null, false);
    }

    public void disableFollowMe() {
        Timber.i("disableFollowMe(): state=%s", this.state);

        followAlgorithm.disableFollow();

        locationFinder.disableLocationUpdates();

        lastLocation = null;

        if (isEnabled()) {
            state = FollowStates.FOLLOW_END;
            droneMgr.onAttributeEvent(AttributeEvent.FOLLOW_STOP, null, false);
        }

        final MavLinkDrone drone = droneMgr.getDrone();
        // Send a brake command only on APM Follow, Solo Shot follow braking is handled by its Shot Manager onboard
        if (GuidedPoint.isGuidedMode(drone)
                && followAlgorithm.getType() != FollowAlgorithm.FollowModes.SOLO_SHOT) {
            droneMgr.getDrone().executeAsyncAction(new Action(ControlActions.ACTION_SEND_BRAKE_VEHICLE), null);
        }
    }

    public boolean isEnabled() {
        return state == FollowStates.FOLLOW_RUNNING || state == FollowStates.FOLLOW_START;
    }

    public boolean isUsingExternalLocations() {
        return mUseExternalLocations;
    }

    public void useExternalLocations(boolean use) {
        Timber.d("useExternalLocations(): use=%s", use);

        if(mUseExternalLocations != use) {
            // We're turning external locations off, going back to normal
            if(mUseExternalLocations) {
                Timber.d("Turn external OFF");
                locationFinder.addLocationListener(TAG, this);
                locationFinder.enableLocationUpdates();
            } else {
                Timber.d("Turn external ON");
                // We're turning them on, ignoring on-device GPS
                locationFinder.removeLocationListener(TAG);
                locationFinder.disableLocationUpdates();
            }

            mUseExternalLocations = use;
        }
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case MODE:
                if (isEnabled() && !GuidedPoint.isGuidedMode(drone)) {
                    Timber.i("Follow enabled, but current mode is not guided. Disable follow");
                    disableFollowMe();
                }
                break;

            case HEARTBEAT_TIMEOUT:
            case DISCONNECTED:
                if(isEnabled()) {
                    disableFollowMe();
                }
                break;
        }
    }

    public void onFollowNewLocation(FollowLocation fl) {
        Timber.d("onFollowNewLocation(%s)", fl);
        Location loc = mLocationRelay.toLocation(fl);
        if(loc != null) {
            onLocationUpdate(loc);
        }
    }

    @Override
    public void onLocationUpdate(Location location) {
        Timber.d("onLocationUpdate(): lat/lng=%.4f/%.4f accurate=%s",
                location.getCoord().getLatitude(),
                location.getCoord().getLongitude(),
                location.isAccurate()
        );

        if (location.isAccurate()) {
            state = FollowStates.FOLLOW_RUNNING;
            lastLocation = location;
            Timber.d("Sending location to followAlgorithm " + followAlgorithm);
            followAlgorithm.onLocationReceived(location);
        } else {
            Timber.d("Location not accurate");
            state = FollowStates.FOLLOW_START;
        }

        droneMgr.onAttributeEvent(AttributeEvent.FOLLOW_UPDATE, null, false);
    }

    @Override
    public void onLocationUnavailable() {
        disableFollowMe();
    }

    public void setAlgorithm(FollowAlgorithm algorithm) {
        Timber.i("setAlgorithm(): algo=" + algorithm);

        if(followAlgorithm != null && followAlgorithm != algorithm) {
            Timber.i("%s.disableFollow()", followAlgorithm);
            followAlgorithm.disableFollow();
        }

        followAlgorithm = algorithm;
        if(isEnabled()){
            Timber.i("%s.enableFollow()", followAlgorithm);
            followAlgorithm.enableFollow();

            if(lastLocation != null)
                followAlgorithm.onLocationReceived(lastLocation);
        }

        droneMgr.onAttributeEvent(AttributeEvent.FOLLOW_UPDATE, null, false);
    }

    public FollowAlgorithm getFollowAlgorithm() {
        return followAlgorithm;
    }

    public FollowStates getState() {
        return state;
    }
}
