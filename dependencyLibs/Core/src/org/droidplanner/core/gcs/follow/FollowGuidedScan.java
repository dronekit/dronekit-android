package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.roi.ROIEstimator;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fhuya on 1/9/15.
 */
public class FollowGuidedScan extends FollowAbove {

    public static final String EXTRA_FOLLOW_ROI_TARGET = "extra_follow_roi_target";

    @Override
    public FollowModes getType() {
        return FollowModes.GUIDED_SCAN;
    }

    public FollowGuidedScan(Drone drone, DroneInterfaces.Handler handler) {
        super(drone, handler);
    }

    @Override
    public void updateAlgorithmParams(Map<String, ?> params){
        super.updateAlgorithmParams(params);

        Coord2D target = (Coord2D) params.get(EXTRA_FOLLOW_ROI_TARGET);
        getROIEstimator().updateROITarget(target);
    }

    @Override
    protected ROIEstimator initROIEstimator(Drone drone, DroneInterfaces.Handler handler){
        return new GuidedROIEstimator(drone, handler);
    }

    @Override
    public Map<String, Object> getParams(){
        Map<String, Object> params = new HashMap<>();
        params.put(EXTRA_FOLLOW_ROI_TARGET, getROIEstimator().roiTarget);
        return params;
    }

    @Override
    protected GuidedROIEstimator getROIEstimator(){
        return (GuidedROIEstimator) super.getROIEstimator();
    }

    private static class GuidedROIEstimator extends ROIEstimator {

        private Coord2D roiTarget;

        public GuidedROIEstimator(Drone drone, DroneInterfaces.Handler handler) {
            super(drone, handler);
        }

        void updateROITarget(Coord2D roiTarget){
            this.roiTarget = roiTarget;
            onLocationChanged(null);
        }

        @Override
        protected void updateROI(){
            if(roiTarget == null){
                //Fallback to the default behavior
                super.updateROI();
            }
            else{
                //Track the target until told otherwise.
                MavLinkROI.setROI(drone, new Coord3D(roiTarget, new Altitude(1.0)));
            }
        }
    }
}
