package com.o3dr.services.android.lib.gcs.action;

import android.os.Bundle;

import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class CalibrationActions {

    public static final String ACTION_START_IMU_CALIBRATION = Utils.PACKAGE_NAME + ".action.START_IMU_CALIBRATION";
    public static final String ACTION_SEND_IMU_CALIBRATION_ACK = Utils.PACKAGE_NAME + ".action" +
            ".SEND_IMU_CALIBRATION_ACK";

    public static final String EXTRA_IMU_STEP = "extra_step";

    public static final String ACTION_START_MAGNETOMETER_CALIBRATION = Utils.PACKAGE_NAME + ".action" +
            ".START_MAGNETOMETER_CALIBRATION";
    public static final String ACTION_STOP_MAGNETOMETER_CALIBRATION = Utils.PACKAGE_NAME + ".action" +
            ".STOP_MAGNETOMETER_CALIBRATION";

    public static final String EXTRA_MAGNETOMETER_START_X = "extra_magnetometer_start_x";
    public static final String EXTRA_MAGNETOMETER_START_Y = "extra_magnetometer_start_y";
    public static final String EXTRA_MAGNETOMETER_START_Z = "extra_magnetometer_start_z";

    /**
     * Start the imu calibration.
     */
    public static Action buildStartIMU(){
        return new Action(ACTION_START_IMU_CALIBRATION);
    }

    /**
     * Generate an action to send an imu calibration acknowledgement.
     */
    public static Action buildSendIMUAck(int step){
        Bundle params = new Bundle();
        params.putInt(EXTRA_IMU_STEP, step);
        return new Action(ACTION_SEND_IMU_CALIBRATION_ACK, params);
    }

    /**
     * Start the magnetometer calibration process.
     * @param startPoints points to start the calibration with.
     */
    public static Action buildStartMagnetometer(double[] pointsX, double[] pointsY, double[] pointsZ){
        Bundle params = new Bundle();
        params.putDoubleArray(EXTRA_MAGNETOMETER_START_X, pointsX);
        params.putDoubleArray(EXTRA_MAGNETOMETER_START_Y, pointsY);
        params.putDoubleArray(EXTRA_MAGNETOMETER_START_Z, pointsZ);

        return new Action(ACTION_START_MAGNETOMETER_CALIBRATION, params);
    }

    /**
     * Stop the magnetometer calibration is one if running.
     */
    public static Action buildStopMagnetometer(){
        return new Action(ACTION_STOP_MAGNETOMETER_CALIBRATION);
    }
}
