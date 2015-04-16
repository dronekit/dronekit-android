package com.o3dr.services.android.lib.drone.attribute.error;

/**
 * List all the possible error types.
 * Created by Fredia Huya-Kouadio on 4/15/15.
 */
public class ErrorType {

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.drone.attribute.error";

    public static final String NO_ERROR = PACKAGE_NAME + ".NO_ERROR";

    public static final String ARM_THROTTLE_BELOW_FAILSAFE = PACKAGE_NAME + ".ARM_THROTTLE_BELOW_FAILSAFE";
    public static final String ARM_GYRO_CALIBRATION_FAILED = PACKAGE_NAME + ".ARM_GYRO_CALIBRATION_FAILED";
    public static final String ARM_MODE_NOT_ARMABLE = PACKAGE_NAME + ".ARM_MODE_NOT_ARMABLE";
    public static final String ARM_ROTOR_NOT_SPINNING = PACKAGE_NAME + ".ARM_ROTOR_NOT_SPINNING";

    public static final String ARM_LEANING = PACKAGE_NAME + ".ARM_LEANING";
    public static final String ARM_THROTTLE_TOO_HIGH = PACKAGE_NAME + ".ARM_THROTTLE_TOO_HIGH";
    public static final String ARM_SAFETY_SWITCH = PACKAGE_NAME + ".ARM_SAFETY_SWITCH";
    public static final String ARM_COMPASS_CALIBRATION_RUNNING = PACKAGE_NAME + ".ARM_COMPASS_CALIBRATION_RUNNING";

    public static final String PRE_ARM_RC_NOT_CALIBRATED = PACKAGE_NAME + ".PRE_ARM_RC_NOT_CALIBRATED";
    public static final String PRE_ARM_BAROMETER_NOT_HEALTHY = PACKAGE_NAME + ".PRE_ARM_BAROMETER_NOT_HEALTHY";
    public static final String PRE_ARM_COMPASS_NOT_HEALTHY = PACKAGE_NAME + ".PRE_ARM_COMPASS_NOT_HEALTHY";
    public static final String PRE_ARM_COMPASS_NOT_CALIBRATED = PACKAGE_NAME + ".PRE_ARM_COMPASS_NOT_CALIBRATED";
    public static final String PRE_ARM_COMPASS_OFFSETS_TOO_HIGH = PACKAGE_NAME + ".PRE_ARM_COMPASS_OFFSETS_TOO_HIGH";
    public static final String PRE_ARM_CHECK_MAGNETIC_FIELD = PACKAGE_NAME + ".PRE_ARM_CHECK_MAGNETIC_FIELD";
    public static final String PRE_ARM_INCONSISTENT_COMPASSES = PACKAGE_NAME + ".PRE_ARM_INCONSISTENT_COMPASSES";
    public static final String PRE_ARM_CHECK_FENCE = PACKAGE_NAME + ".PRE_ARM_CHECK_FENCE";
    public static final String PRE_ARM_INS_NOT_CALIBRATED = PACKAGE_NAME + ".PRE_ARM_INS_NOT_CALIBRATED";
    public static final String PRE_ARM_ACCELEROMETERS_NOT_HEALTHY = PACKAGE_NAME + ".PRE_ARM_ACCELEROMETERS_NOT_HEALTHY";
    public static final String PRE_ARM_INCONSISTENT_ACCELEROMETERS = PACKAGE_NAME + ".PRE_ARM_INCONSISTENT_ACCELEROMETERS";
    public static final String PRE_ARM_GYROS_NOT_HEALTHY = PACKAGE_NAME + ".PRE_ARM_GYROS_NOT_HEALTHY";
    public static final String PRE_ARM_INCONSISTENT_GYROS = PACKAGE_NAME + ".PRE_ARM_INCONSISTENT_GYROS";
    public static final String PRE_ARM_CHECK_BOARD_VOLTAGE = PACKAGE_NAME + ".PRE_ARM_CHECK_BOARD_VOLTAGE";
    public static final String PRE_ARM_DUPLICATE_AUX_SWITCH_OPTIONS = PACKAGE_NAME + ".PRE_ARM_DUPLICATE_AUX_SWITCH_OPTIONS";
    public static final String PRE_ARM_CHECK_FAILSAFE_THRESHOLD_VALUE = PACKAGE_NAME + ".PRE_ARM_CHECK_FAILSAFE_THRESHOLD_VALUE";
    public static final String PRE_ARM_CHECK_ANGLE_MAX = PACKAGE_NAME + ".PRE_ARM_CHECK_ANGLE_MAX";
    public static final String PRE_ARM_ACRO_BAL_ROLL_PITCH = PACKAGE_NAME + ".PRE_ARM_ACRO_BAL_ROLL_PITCH";
    public static final String PRE_ARM_NEED_GPS_LOCK = PACKAGE_NAME + ".PRE_ARM_NEED_GPS_LOCK";
    public static final String PRE_ARM_EKF_HOME_VARIANCE = PACKAGE_NAME + ".PRE_ARM_EKF_HOME_VARIANCE";
    public static final String PRE_ARM_HIGH_GPS_HDOP = PACKAGE_NAME + ".PRE_ARM_HIGH_GPS_HDOP";
    public static final String PRE_ARM_GPS_GLITCH = PACKAGE_NAME + ".PRE_ARM_GPS_GLITCH";
    public static final String WAITING_FOR_NAVIGATION_ALIGNMENT = PACKAGE_NAME + ".WAITING_FOR_NAVIGATION_ALIGNMENT";

    public static final String ALTITUDE_DISPARITY = PACKAGE_NAME + ".ALTITUDE_DISPARITY";
    public static final String LOW_BATTERY = PACKAGE_NAME + ".LOW_BATTERY";
    public static final String AUTO_TUNE_FAILED = PACKAGE_NAME + ".AUTO_TUNE_FAILED";
    public static final String CRASH_DISARMING = PACKAGE_NAME + ".CRASH_DISARMING";
    public static final String PARACHUTE_TOO_LOW = PACKAGE_NAME + ".PARACHUTE_TOO_LOW";
    public static final String EKF_VARIANCE = PACKAGE_NAME + ".EKF_VARIANCE";
    public static final String NO_DATAFLASH_INSERTED = PACKAGE_NAME + ".NO_DATAFLASH_INSERTED";
    public static final String RC_FAILSAFE = PACKAGE_NAME + ".RC_FAILSAFE";
}
