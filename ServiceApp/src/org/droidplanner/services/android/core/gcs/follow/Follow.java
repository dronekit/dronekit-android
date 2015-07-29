package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.core.drone.variables.State;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.gcs.location.Location.LocationFinder;
import org.droidplanner.services.android.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public class Follow implements OnDroneListener, LocationReceiver {

    private Location lastLocation;

    /**
     * Set of return value for the 'toggleFollowMeState' method.
     */
    public enum FollowStates {
        FOLLOW_INVALID_STATE, FOLLOW_DRONE_NOT_ARMED, FOLLOW_DRONE_DISCONNECTED, FOLLOW_START, FOLLOW_RUNNING, FOLLOW_END
    }

    private FollowStates state = FollowStates.FOLLOW_INVALID_STATE;
    private MavLinkDrone drone;

    private LocationFinder locationFinder;
    private FollowAlgorithm followAlgorithm;

    public Follow(MavLinkDrone drone, Handler handler, LocationFinder locationFinder) {
        this.drone = drone;
        drone.addDroneListener(this);

        followAlgorithm = FollowAlgorithm.FollowModes.LEASH.getAlgorithmType(drone, handler);

        this.locationFinder = locationFinder;
        locationFinder.setLocationListener(this);
    }

    public void toggleFollowMeState() {
        final State droneState = drone.getState();
        if (droneState == null) {
            state = FollowStates.FOLLOW_INVALID_STATE;
            return;
        }

        if (isEnabled()) {
            disableFollowMe();
        } else {
            if (drone.isConnected()) {
                if (droneState.isArmed()) {
                    GuidedPoint.changeToGuidedMode(drone, null);
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
        lastLocation = null;
        state = FollowStates.FOLLOW_START;

        locationFinder.enableLocationUpdates();
        followAlgorithm.enableFollow();

        drone.notifyDroneEvent(DroneEventsType.FOLLOW_START);
    }

    private void disableFollowMe() {
        followAlgorithm.disableFollow();
        locationFinder.disableLocationUpdates();

        lastLocation = null;

        if (isEnabled()) {
            state = FollowStates.FOLLOW_END;
            drone.notifyDroneEvent(DroneEventsType.FOLLOW_STOP);
        }
    }

    public boolean isEnabled() {
        return state == FollowStates.FOLLOW_RUNNING || state == FollowStates.FOLLOW_START;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case MODE:
                if (isEnabled() && !GuidedPoint.isGuidedMode(drone)) {
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

    @Override
    public void onLocationUpdate(Location location) {
        if (location.isAccurate()) {
            state = FollowStates.FOLLOW_RUNNING;
            lastLocation = location;
            followAlgorithm.onLocationReceived(location);
        } else {
            state = FollowStates.FOLLOW_START;
        }

        drone.notifyDroneEvent(DroneEventsType.FOLLOW_UPDATE);
    }

    @Override
    public void onLocationUnavailable() {
        disableFollowMe();
    }

    public void setAlgorithm(FollowAlgorithm algorithm) {
        if(followAlgorithm != null && followAlgorithm != algorithm){
            followAlgorithm.disableFollow();
        }

        followAlgorithm = algorithm;
        if(isEnabled()){
            followAlgorithm.enableFollow();

            if(lastLocation != null)
                followAlgorithm.onLocationReceived(lastLocation);
        }
        drone.notifyDroneEvent(DroneEventsType.FOLLOW_CHANGE_TYPE);
    }

    public FollowAlgorithm getFollowAlgorithm() {
        return followAlgorithm;
    }

    public FollowStates getState() {
        return state;
    }
}
