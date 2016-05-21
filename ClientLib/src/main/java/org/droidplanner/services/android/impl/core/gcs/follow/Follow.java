package org.droidplanner.services.android.impl.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.drone.action.ControlActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.impl.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.impl.core.drone.variables.State;
import org.droidplanner.services.android.impl.core.gcs.location.Location;
import org.droidplanner.services.android.impl.core.gcs.location.Location.LocationFinder;
import org.droidplanner.services.android.impl.core.gcs.location.Location.LocationReceiver;

public class Follow implements OnDroneListener<MavLinkDrone>, LocationReceiver {

    private static final String TAG = Follow.class.getSimpleName();
    private Location lastLocation;

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

    public Follow(MavLinkDroneManager droneMgr, Handler handler, LocationFinder locationFinder) {
        this.droneMgr = droneMgr;
        final MavLinkDrone drone = droneMgr.getDrone();
        if(drone != null)
            drone.addDroneListener(this);

        followAlgorithm = FollowAlgorithm.FollowModes.LEASH.getAlgorithmType(droneMgr, handler);

        this.locationFinder = locationFinder;
        locationFinder.addLocationListener(TAG, this);
    }

    public void toggleFollowMeState() {
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

        locationFinder.enableLocationUpdates();
        followAlgorithm.enableFollow();

        droneMgr.onAttributeEvent(AttributeEvent.FOLLOW_START, null);
    }

    private void disableFollowMe() {
        followAlgorithm.disableFollow();
        locationFinder.disableLocationUpdates();

        lastLocation = null;

        if (isEnabled()) {
            state = FollowStates.FOLLOW_END;
            droneMgr.onAttributeEvent(AttributeEvent.FOLLOW_STOP, null);
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

        droneMgr.onAttributeEvent(AttributeEvent.FOLLOW_UPDATE, null);
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

        droneMgr.onAttributeEvent(AttributeEvent.FOLLOW_UPDATE, null);
    }

    public FollowAlgorithm getFollowAlgorithm() {
        return followAlgorithm;
    }

    public FollowStates getState() {
        return state;
    }
}
