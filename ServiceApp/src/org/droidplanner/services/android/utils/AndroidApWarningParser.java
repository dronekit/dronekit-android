package org.droidplanner.services.android.utils;

import org.droidplanner.core.model.AutopilotWarningParser;
import org.droidplanner.core.model.Drone;

import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ALTITUDE_DISPARITY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_GYRO_CALIBRATION_FAILED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_LEANING;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_MODE_NOT_ARMABLE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_ROTOR_NOT_SPINNING;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_SAFETY_SWITCH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_THROTTLE_BELOW_FAILSAFE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_THROTTLE_TOO_HIGH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.AUTO_TUNE_FAILED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.CRASH_DISARMING;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.EKF_VARIANCE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.LOW_BATTERY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.NO_DATAFLASH_INSERTED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.NO_ERROR;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PARACHUTE_TOO_LOW;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_ACCELEROMETERS_NOT_HEALTHY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_ACRO_BAL_ROLL_PITCH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_BAROMETER_NOT_HEALTHY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_ANGLE_MAX;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_BOARD_VOLTAGE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_FAILSAFE_THRESHOLD_VALUE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_FENCE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_MAGNETIC_FIELD;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_COMPASS_NOT_CALIBRATED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_COMPASS_NOT_HEALTHY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_COMPASS_OFFSETS_TOO_HIGH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_DUPLICATE_AUX_SWITCH_OPTIONS;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_EKF_HOME_VARIANCE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_GYROS_NOT_HEALTHY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_HIGH_GPS_HDOP;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_INCONSISTENT_ACCELEROMETERS;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_INCONSISTENT_COMPASSES;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_INCONSISTENT_GYROS;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_INS_NOT_CALIBRATED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_NEED_GPS_LOCK;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_RC_NOT_CALIBRATED;

/**
 * Autopilot error parser.
 * Created by fhuya on 12/16/14.
 */
public class AndroidApWarningParser implements AutopilotWarningParser {

    @Override
    public String getDefaultWarning() {
        return NO_ERROR;
    }

    /**
     * Maps the ArduPilot warnings set to the 3DR Services warnings set.
     *
     * @param warning warning originating from the ArduPilot autopilot
     * @return equivalent 3DR Services warning type
     */
    @Override
    public String parseWarning(Drone drone, String warning) {
        switch (warning) {
            case "Arm: Thr below FS":
            case "Arm: Throttle below Failsafe":
                return ARM_THROTTLE_BELOW_FAILSAFE;

            case "Arm: Gyro calibration failed":
                return ARM_GYRO_CALIBRATION_FAILED;

            case "Arm: Mode not armable":
                return ARM_MODE_NOT_ARMABLE;

            case "Arm: Rotor not spinning":
                return ARM_ROTOR_NOT_SPINNING;

            case "Arm: Altitude disparity":
            case "PreArm: Altitude disparity":
                return ALTITUDE_DISPARITY;

            case "Arm: Leaning":
                return ARM_LEANING;

            case "Arm: Throttle too high":
                return ARM_THROTTLE_TOO_HIGH;

            case "Arm: Safety Switch":
                return ARM_SAFETY_SWITCH;


            case "PreArm: RC not calibrated":
                return PRE_ARM_RC_NOT_CALIBRATED;

            case "PreArm: Barometer not healthy":
                return PRE_ARM_BAROMETER_NOT_HEALTHY;

            case "PreArm: Compass not healthy":
                return PRE_ARM_COMPASS_NOT_HEALTHY;

            case "PreArm: Compass not calibrated":
                return PRE_ARM_COMPASS_NOT_CALIBRATED;

            case "PreArm: Compass offsets too high":
                return PRE_ARM_COMPASS_OFFSETS_TOO_HIGH;

            case "PreArm: Check mag field":
                return PRE_ARM_CHECK_MAGNETIC_FIELD;

            case "PreArm: inconsistent compasses":
                return PRE_ARM_INCONSISTENT_COMPASSES;

            case "PreArm: check fence":
                return PRE_ARM_CHECK_FENCE;

            case "PreArm: INS not calibrated":
                return PRE_ARM_INS_NOT_CALIBRATED;

            case "PreArm: Accelerometers not healthy":
                return PRE_ARM_ACCELEROMETERS_NOT_HEALTHY;

            case "PreArm: inconsistent Accelerometers":
                return PRE_ARM_INCONSISTENT_ACCELEROMETERS;

            case "PreArm: Gyros not healthy":
                return PRE_ARM_GYROS_NOT_HEALTHY;

            case "PreArm: inconsistent Gyros":
                return PRE_ARM_INCONSISTENT_GYROS;

            case "PreArm: Check Board Voltage":
                return PRE_ARM_CHECK_BOARD_VOLTAGE;

            case "PreArm: Duplicate Aux Switch Options":
                return PRE_ARM_DUPLICATE_AUX_SWITCH_OPTIONS;

            case "PreArm: Check FS_THR_VALUE":
                return PRE_ARM_CHECK_FAILSAFE_THRESHOLD_VALUE;

            case "PreArm: Check ANGLE_MAX":
                return PRE_ARM_CHECK_ANGLE_MAX;

            case "PreArm: ACRO_BAL_ROLL/PITCH":
                return PRE_ARM_ACRO_BAL_ROLL_PITCH;

            case "PreArm: Need 3D Fix":
                return PRE_ARM_NEED_GPS_LOCK;

            case "PreArm: EKF-home variance":
                return PRE_ARM_EKF_HOME_VARIANCE;

            case "PreArm: High GPS HDOP":
                return PRE_ARM_HIGH_GPS_HDOP;


            case "No dataflash inserted":
                return NO_DATAFLASH_INSERTED;

            case "Low Battery!":
                return LOW_BATTERY;

            case "AutoTune: Failed":
                return AUTO_TUNE_FAILED;

            case "Crash: Disarming":
                return CRASH_DISARMING;

            case "Parachute: Too Low":
                return PARACHUTE_TOO_LOW;

            case "EKF variance":
                return EKF_VARIANCE;


            default:
                return null;
        }
    }
}
