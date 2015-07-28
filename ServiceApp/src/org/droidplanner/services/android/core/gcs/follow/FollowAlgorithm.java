package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.gcs.roi.ROIEstimator;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FollowAlgorithm {

    protected final MavLinkDrone drone;
    private final ROIEstimator roiEstimator;
    private final AtomicBoolean isFollowEnabled = new AtomicBoolean(false);

    public FollowAlgorithm(MavLinkDrone drone, Handler handler) {
        this.drone = drone;
        this.roiEstimator = initROIEstimator(drone, handler);
    }

    protected boolean isFollowEnabled() {
        return isFollowEnabled.get();
    }

    public void enableFollow() {
        isFollowEnabled.set(true);
        roiEstimator.enableFollow();
    }

    public void disableFollow() {
        if(isFollowEnabled.compareAndSet(true, false)) {
            if (GuidedPoint.isGuidedMode(drone)) {
                drone.getGuidedPoint().pauseAtCurrentLocation(null);
            }

            roiEstimator.disableFollow();
        }
    }

    public void updateAlgorithmParams(Map<String, ?> paramsMap) {
    }

    protected ROIEstimator initROIEstimator(MavLinkDrone drone, Handler handler) {
        return new ROIEstimator(drone, handler);
    }

    protected ROIEstimator getROIEstimator() {
        return roiEstimator;
    }

    public final void onLocationReceived(Location location) {
        if (isFollowEnabled.get()) {
            roiEstimator.onLocationUpdate(location);
            processNewLocation(location);
        }
    }

    protected abstract void processNewLocation(Location location);

    public abstract FollowModes getType();

    public Map<String, Object> getParams() {
        return Collections.emptyMap();
    }

    public enum FollowModes {
        LEASH("Leash"),
        LEAD("Lead"),
        RIGHT("Right"),
        LEFT("Left"),
        CIRCLE("Orbit"),
        ABOVE("Above"),
        SPLINE_LEASH("Vector Leash"),
        SPLINE_ABOVE("Vector Above"),
        GUIDED_SCAN("Guided Scan"),
        LOOK_AT_ME("Look At Me");

        private String name;

        FollowModes(String str) {
            name = str;
        }

        @Override
        public String toString() {
            return name;
        }

        public FollowModes next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public FollowAlgorithm getAlgorithmType(MavLinkDrone drone, Handler handler) {
            switch (this) {
                case LEASH:
                default:
                    return new FollowLeash(drone, handler, 8.0);
                case LEAD:
                    return new FollowLead(drone, handler, 15.0);
                case RIGHT:
                    return new FollowRight(drone, handler, 10.0);
                case LEFT:
                    return new FollowLeft(drone, handler, 10.0);
                case CIRCLE:
                    return new FollowCircle(drone, handler, 15.0, 10.0);
                case ABOVE:
                    return new FollowAbove(drone, handler);
                case SPLINE_LEASH:
                    return new FollowSplineLeash(drone, handler, 8.0);
                case SPLINE_ABOVE:
                    return new FollowSplineAbove(drone, handler);
                case GUIDED_SCAN:
                    return new FollowGuidedScan(drone, handler);
                case LOOK_AT_ME:
                    return new FollowLookAtMe(drone, handler);
            }
        }
    }

}
