package com.o3dr.services.android.lib.gcs.action;

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

}
