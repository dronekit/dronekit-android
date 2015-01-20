package com.o3dr.android.client.apis.gcs;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_START_IMU_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.ACTION_STOP_MAGNETOMETER_CALIBRATION;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_IMU_STEP;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_MAGNETOMETER_START_X;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_MAGNETOMETER_START_Y;
import static com.o3dr.services.android.lib.gcs.action.CalibrationActions.EXTRA_MAGNETOMETER_START_Z;

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
     *
     * @param startPoints points to start the calibration with.
     */
    public static void startMagnetometerCalibration(Drone drone, double[] pointsX, double[] pointsY, double[] pointsZ) {
        Bundle params = new Bundle();
        params.putDoubleArray(EXTRA_MAGNETOMETER_START_X, pointsX);
        params.putDoubleArray(EXTRA_MAGNETOMETER_START_Y, pointsY);
        params.putDoubleArray(EXTRA_MAGNETOMETER_START_Z, pointsZ);

        drone.performAsyncAction(new Action(ACTION_START_MAGNETOMETER_CALIBRATION, params));
    }

    /**
     * Stop the magnetometer calibration is one if running.
     */
    public static void stopMagnetometerCalibration(Drone drone) {
        drone.performAsyncAction(new Action(ACTION_STOP_MAGNETOMETER_CALIBRATION));
    }
}
