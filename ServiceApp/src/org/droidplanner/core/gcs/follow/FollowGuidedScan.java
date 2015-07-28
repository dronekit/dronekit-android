package org.droidplanner.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.core.gcs.roi.ROIEstimator;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.services.android.drone.autopilot.MavLinkDrone;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by fhuya on 1/9/15.
 */
public class FollowGuidedScan extends FollowAbove {

    private static final long TIMEOUT = 1000; //ms

    public static final String EXTRA_FOLLOW_ROI_TARGET = "extra_follow_roi_target";

    public static final double DEFAULT_FOLLOW_ROI_ALTITUDE = 10; //meters
    private static final double sDefaultRoiAltitude = (DEFAULT_FOLLOW_ROI_ALTITUDE);

    @Override
    public FollowModes getType() {
        return FollowModes.GUIDED_SCAN;
    }

    public FollowGuidedScan(MavLinkDrone drone, Handler handler) {
        super(drone, handler);
    }

    @Override
    public void updateAlgorithmParams(Map<String, ?> params) {
        super.updateAlgorithmParams(params);

        final Coord3D target;

        Coord2D tempCoord = (Coord2D) params.get(EXTRA_FOLLOW_ROI_TARGET);
        if (tempCoord == null || tempCoord instanceof Coord3D) {
            target = (Coord3D) tempCoord;
        } else {
            target = new Coord3D(tempCoord, sDefaultRoiAltitude);
        }

        getROIEstimator().updateROITarget(target);
    }

    @Override
    protected ROIEstimator initROIEstimator(MavLinkDrone drone, Handler handler) {
        return new GuidedROIEstimator(drone, handler);
    }

    @Override
    public Map<String, Object> getParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(EXTRA_FOLLOW_ROI_TARGET, getROIEstimator().roiTarget);
        return params;
    }

    @Override
    protected GuidedROIEstimator getROIEstimator() {
        return (GuidedROIEstimator) super.getROIEstimator();
    }

    private static class GuidedROIEstimator extends ROIEstimator {

        private Coord3D roiTarget;

        public GuidedROIEstimator(MavLinkDrone drone, Handler handler) {
            super(drone, handler);
        }

        void updateROITarget(Coord3D roiTarget) {
            this.roiTarget = roiTarget;
            onLocationUpdate(null);
        }

        @Override
        protected void updateROI() {
            if (roiTarget == null) {
                System.out.println("Cancelling ROI lock.");
                //Fallback to the default behavior
                super.updateROI();
            } else {
                Timber.d("ROI Target: " + roiTarget.toString());

                //Track the target until told otherwise.
                MavLinkDoCmds.setROI(drone, roiTarget, null);
                watchdog.postDelayed(watchdogCallback, TIMEOUT);
            }
        }
    }
}
