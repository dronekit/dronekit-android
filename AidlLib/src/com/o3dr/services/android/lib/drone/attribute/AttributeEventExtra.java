package com.o3dr.services.android.lib.drone.attribute;

/**
 * Holds handles used to retrieve additional information broadcast along a drone event.
 */
public class AttributeEventExtra {

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.attribute.event" +
            ".extra";

    public static final String EXTRA_AUTOPILOT_FAILSAFE_MESSAGE = PACKAGE_NAME + "" +
            ".AUTOPILOT_FAILSAFE_MESSAGE";

    public static final String EXTRA_CALIBRATION_IMU_MESSAGE = PACKAGE_NAME +
            ".CALIBRATION_IMU_MESSAGE";

    /**
     * Used to access the points used to start the magnetometer calibration.
     */
    public static final String EXTRA_CALIBRATION_MAG_POINTS_X = PACKAGE_NAME + "" +
            ".CALIBRATION_MAG_POINTS_X";
    public static final String EXTRA_CALIBRATION_MAG_POINTS_Y = PACKAGE_NAME + "" +
            ".CALIBRATION_MAG_POINTS_Y";
    public static final String EXTRA_CALIBRATION_MAG_POINTS_Z = PACKAGE_NAME + "" +
            ".CALIBRATION_MAG_POINTS_Z";
    /**
     * Used to access the magnetometer calibration fitness value.
     */
    public static final String EXTRA_CALIBRATION_MAG_FITNESS = PACKAGE_NAME + "" +
            ".CALIBRATION_MAG_FITNESS";
    /**
     * Used to access the magnetometer calibration fit center values.
     */
    public static final String EXTRA_CALIBRATION_MAG_FIT_CENTER = PACKAGE_NAME + "" +
            ".CALIBRATION_MAG_FIT_CENTER";
    /**
     * Used to access the magnetometer calibration fit radii values.
     */
    public static final String EXTRA_CALIBRATION_MAG_FIT_RADII = PACKAGE_NAME + "" +
            ".CALIBRATION_MAG_FIT_RADII";
    /**
     * Used to access the magnetometer calibration final offset values.
     */
    public static final String EXTRA_CALIBRATION_MAG_OFFSETS = PACKAGE_NAME + "" +
            ".CALIBRATION_MAG_OFFSETS";

    /**
     * Used to access the mavlink version when the heartbeat is received for the first time or
     * restored.
     */
    public static final String EXTRA_MAVLINK_VERSION = PACKAGE_NAME + ".MAVLINK_VERSION";

    public static final String EXTRA_MISSION_CURRENT_WAYPOINT = PACKAGE_NAME + "" +
            ".MISSION_CURRENT_WAYPOINT";
    public static final String EXTRA_MISSION_DRONIE_BEARING = PACKAGE_NAME + "" +
            ".MISSION_DRONIE_BEARING";

    public static final String EXTRA_PARAMETERS_COUNT = PACKAGE_NAME + ".PARAMETERS_COUNT";
    public static final String EXTRA_PARAMETER_INDEX = PACKAGE_NAME + ".PARAMETER_INDEX";
}
