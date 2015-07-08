package com.o3dr.android.client.apis;

import android.os.Bundle;
import android.text.TextUtils;

import com.o3dr.android.client.Drone;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows to query the capabilities offered by the vehicle.
 * Created by Fredia Huya-Kouadio on 7/5/15.
 */
public class CapabilityApi implements Api {

    /**
     * Feature support check result. Indicate the feature is supported.
     */
    public static final int FEATURE_SUPPORTED = 1;

    /**
     * Feature support check result. Indicate the feature is not supported.
     */
    public static final int FEATURE_UNSUPPORTED = 2;

    private static final ConcurrentHashMap<Drone, CapabilityApi> capabilityApiCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a capability api instance.
     * @param drone target vehicle.
     * @return a CapabilityApi instance.
     */
    public static CapabilityApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, capabilityApiCache, new Builder<CapabilityApi>() {
            @Override
            public CapabilityApi build() {
                return new CapabilityApi(drone);
            }
        });
    }

    private final Drone drone;

    private CapabilityApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Determine whether the given feature is supported.
     * @param featureId Id of the feature to check.
     * @param resultListener Callback to receive feature support status.
     */
    public void checkFeatureSupport(final String featureId, final FeatureSupportListener resultListener){
        if(TextUtils.isEmpty(featureId) || resultListener == null)
            return;

        switch(featureId){
            case FeatureIds.IMU_CALIBRATION:
                    drone.post(new Runnable() {
                        @Override
                        public void run() {
                            resultListener.onFeatureSupportResult(featureId, FEATURE_SUPPORTED, null);
                        }
                    });
                break;

            default:
                    drone.post(new Runnable() {
                        @Override
                        public void run() {
                            resultListener.onFeatureSupportResult(featureId, FEATURE_UNSUPPORTED, null);
                        }
                    });
                break;
        }
    }

    /**
     * Defines the set of feature ids.
     */
    public static final class FeatureIds {

        /**
         * Id for the video feature.
         */
        public static final String VIDEO = "feature_video";

        /**
         * Id for the compass calibration feature.
         */
        public static final String COMPASS_CALIBRATION = "feature_compass_calibration";

        /**
         * Id for the imu calibration feature.
         */
        public static final String IMU_CALIBRATION = "feature_imu_calibration";

        //Private to prevent instantiation.
        private FeatureIds(){}
    }

    public interface FeatureSupportListener {

        /**
         * Callback for the result from checking feature support.
         * @param featureId Id of the feature for which we're checking support.
         * @param result Result of the feature support check.
         * @param resultInfo Additional info about the level of support for the feature. Can be null.
         */
        void onFeatureSupportResult(String featureId, int result, Bundle resultInfo);
    }

}
