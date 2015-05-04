package com.o3dr.android.client.apis.gcs;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_ACCEPT_MAGNETOMETER_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_CANCEL_MAGNETOMETER_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_START_IMU_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_IMU_STEP;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_RETRY_ON_FAILURE;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_SAVE_AUTOMATICALLY;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_START_DELAY;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class CalibrationApi {

    /**
     * Start the imu calibration.
     */
    public static void startIMUCalibration(Drone drone) {
        drone.performAsyncAction(new Action(ACTION_START_IMU_CALIBRATION));
    }

    /**
     * Generate an action to send an imu calibration acknowledgement.
     */
    public static void sendIMUAck(Drone drone, int step) {
        Bundle params = new Bundle();
        params.putInt(EXTRA_IMU_STEP, step);
        drone.performAsyncAction(new Action(ACTION_SEND_IMU_CALIBRATION_ACK, params));
    }

    /**
     * Start the magnetometer calibration process.
     */
    public static void startMagnetometerCalibration(Drone drone) {
        startMagnetometerCalibration(drone, false, true, 0);
    }

    /**
     * Start the magnetometer calibration process
     * @param drone vehicle to calibrate
     * @param retryOnFailure if true, automatically retry the magnetometer calibration if it fails
     * @param saveAutomatically if true, save the calibration automatically without user input.
     * @param startDelay positive delay in seconds before starting the calibration
     */
    public static void startMagnetometerCalibration(Drone drone, boolean retryOnFailure, boolean saveAutomatically,
                                                    int startDelay){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_RETRY_ON_FAILURE, retryOnFailure);
        params.putBoolean(EXTRA_SAVE_AUTOMATICALLY, saveAutomatically);
        params.putInt(EXTRA_START_DELAY, startDelay);

        drone.performAsyncAction(new Action(ACTION_START_MAGNETOMETER_CALIBRATION, params));
    }

    public static void acceptMagnetometerCalibration(Drone drone) {
        drone.performAsyncAction(new Action(ACTION_ACCEPT_MAGNETOMETER_CALIBRATION));
    }

    /**
     * Cancel the magnetometer calibration is one if running.
     */
    public static void cancelMagnetometerCalibration(Drone drone) {
        drone.performAsyncAction(new Action(ACTION_CANCEL_MAGNETOMETER_CALIBRATION));
    }
}
