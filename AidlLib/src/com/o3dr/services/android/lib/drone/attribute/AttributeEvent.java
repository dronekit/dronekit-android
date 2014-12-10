package com.o3dr.services.android.lib.drone.attribute;

/**
 * Stores all possible drone events.
 */
public class AttributeEvent {

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.attribute.event";

    /**
     * Attitude attribute events.
     */
    public static final String ATTITUDE_UPDATED = PACKAGE_NAME + ".ATTITUDE_UPDATED";

    public static final String AUTOPILOT_FAILSAFE = PACKAGE_NAME + ".AUTOPILOT_FAILSAFE";

    /**
     * Signals the start of magnetometer calibration.
     */
    public static final String CALIBRATION_MAG_STARTED = PACKAGE_NAME +
            ".CALIBRATION_MAG_STARTED";
    /**
     * Signals a magnetometer calibration fitness update.
     */
    public static final String CALIBRATION_MAG_ESTIMATION = PACKAGE_NAME +
            ".CALIBRATION_MAG_ESTIMATION";
    /**
     * Signals completion of the magnetometer calibration.
     */
    public static final String CALIBRATION_MAG_COMPLETED = PACKAGE_NAME +
            ".CALIBRATION_MAG_COMPLETED";

    public static final String CALIBRATION_IMU = PACKAGE_NAME + ".CALIBRATION_IMU";
    public static final String CALIBRATION_IMU_ERROR = PACKAGE_NAME + ".CALIBRATION_IMU_ERROR";
    public static final String CALIBRATION_IMU_TIMEOUT = PACKAGE_NAME +
            ".CALIBRATION_IMU_TIMEOUT";

    public static final String FOLLOW_START = PACKAGE_NAME + ".FOLLOW_START";
    public static final String FOLLOW_STOP = PACKAGE_NAME + ".FOLLOW_STOP";
    public static final String FOLLOW_UPDATE = PACKAGE_NAME + ".FOLLOW_UPDATE";

    /**
     * Camera attribute events.
     */
    public static final String CAMERA_UPDATED = PACKAGE_NAME + ".CAMERA_UPDATED";
    public static final String CAMERA_FOOTPRINTS_UPDATED = PACKAGE_NAME + ".CAMERA_FOOTPRINTS_UPDATED";

    /**
     * GuidedState attribute events.
     */
    public static final String GUIDED_POINT_UPDATED = PACKAGE_NAME + ".GUIDED_POINT_UPDATED";

    /**
     * Mission attribute events.
     */
    public static final String MISSION_UPDATED = PACKAGE_NAME + ".MISSION_UPDATED";
    public static final String MISSION_DRONIE_CREATED = PACKAGE_NAME + "" +
            ".MISSION_DRONIE_CREATED";
    public static final String MISSION_SENT = PACKAGE_NAME + ".MISSION_SENT";
    public static final String MISSION_RECEIVED = PACKAGE_NAME + ".MISSION_RECEIVED";
    public static final String MISSION_ITEM_UPDATED = PACKAGE_NAME + ".MISSION_ITEM_UPDATED";

    /**
     * Parameter attribute events.
     */
    public static final String PARAMETERS_REFRESH_STARTED = PACKAGE_NAME + ".PARAMETERS_REFRESH_STARTED";
    public static final String PARAMETERS_REFRESH_ENDED = PACKAGE_NAME + ".PARAMETERS_REFRESH_ENDED";
    public static final String PARAMETERS_RECEIVED = PACKAGE_NAME + ".PARAMETERS_RECEIVED";

    public static final String TYPE_UPDATED = PACKAGE_NAME + ".TYPE_UPDATED";

    /**
     * Signal attribute events.
     */
    public static final String SIGNAL_UPDATED = PACKAGE_NAME + ".SIGNAL_UPDATED";
    public static final String SIGNAL_WEAK = PACKAGE_NAME + ".SIGNAL_WEAK";

    /**
     * Speed attribute events.
     */
    public static final String SPEED_UPDATED = PACKAGE_NAME + ".SPEED_UPDATED";

    /**
     * Battery attribute events.
     */
    public static final String BATTERY_UPDATED = PACKAGE_NAME + ".BATTERY_UPDATED";

    /**
     * State attribute events.
     */
    public static final String STATE_UPDATED = PACKAGE_NAME + ".STATE_UPDATED";
    public static final String STATE_ARMING = PACKAGE_NAME + ".STATE_ARMING";
    public static final String STATE_CONNECTED = PACKAGE_NAME + ".STATE_CONNECTED";
    public static final String STATE_DISCONNECTED = PACKAGE_NAME + ".STATE_DISCONNECTED";
    public static final String STATE_VEHICLE_MODE = PACKAGE_NAME + ".STATE_VEHICLE_MODE";

    /**
     * Home attribute events.
     */
    public static final String HOME_UPDATED = PACKAGE_NAME + ".HOME_UPDATED";

    /**
     * Gps' attribute events.
     */
    public static final String GPS_POSITION = PACKAGE_NAME + ".GPS_POSITION";
    public static final String GPS_FIX = PACKAGE_NAME + ".GPS_FIX";
    public static final String GPS_COUNT = PACKAGE_NAME + ".GPS_COUNT";
    public static final String WARNING_NO_GPS = PACKAGE_NAME + ".WARNING_NO_GPS";

    public static final String HEARTBEAT_FIRST = PACKAGE_NAME + ".HEARTBEAT_FIRST";
    public static final String HEARTBEAT_RESTORED = PACKAGE_NAME + ".HEARTBEAT_RESTORED";
    public static final String HEARTBEAT_TIMEOUT = PACKAGE_NAME + ".HEARTBEAT_TIMEOUT";

    /**
     * Altitude's attribute events.
     */
    public static final String ALTITUDE_400FT_EXCEEDED = PACKAGE_NAME +
            ".ALTITUDE_400FT_EXCEEDED";

}
