package com.o3dr.services.android.lib.drone.attribute;

/**
 * Stores all possible drone events.
 */
public class AttributeEvent {

    private static final String CLAZZ_NAME = AttributeEvent.class.getName();

    /**
     * Attitude attribute events.
     */
    public static final String ATTITUDE_UPDATED = CLAZZ_NAME + ".ATTITUDE_UPDATED";

    public static final String AUTOPILOT_FAILSAFE = CLAZZ_NAME + ".AUTOPILOT_FAILSAFE";

    /**
     * Signals the start of magnetometer calibration.
     */
    public static final String CALIBRATION_MAG_STARTED = CLAZZ_NAME +
            ".CALIBRATION_MAG_STARTED";
    /**
     * Signals a magnetometer calibration fitness update.
     */
    public static final String CALIBRATION_MAG_ESTIMATION = CLAZZ_NAME +
            ".CALIBRATION_MAG_ESTIMATION";
    /**
     * Signals completion of the magnetometer calibration.
     */
    public static final String CALIBRATION_MAG_COMPLETED = CLAZZ_NAME +
            ".CALIBRATION_MAG_COMPLETED";

    public static final String CALIBRATION_IMU = CLAZZ_NAME + ".CALIBRATION_IMU";
    public static final String CALIBRATION_IMU_ERROR = CLAZZ_NAME + ".CALIBRATION_IMU_ERROR";
    public static final String CALIBRATION_IMU_TIMEOUT = CLAZZ_NAME +
            ".CALIBRATION_IMU_TIMEOUT";

    public static final String FOLLOW_START = CLAZZ_NAME + ".FOLLOW_START";
    public static final String FOLLOW_STOP = CLAZZ_NAME + ".FOLLOW_STOP";
    public static final String FOLLOW_UPDATE = CLAZZ_NAME + ".FOLLOW_UPDATE";

    /**
     * Camera attribute events.
     */
    public static final String CAMERA_UPDATED = CLAZZ_NAME + ".CAMERA_UPDATED";
    public static final String CAMERA_FOOTPRINTS_UPDATED = CLAZZ_NAME + ".CAMERA_FOOTPRINTS_UPDATED";

    /**
     * GuidedState attribute events.
     */
    public static final String GUIDED_POINT_UPDATED = CLAZZ_NAME + ".GUIDED_POINT_UPDATED";

    /**
     * Mission attribute events.
     */
    public static final String MISSION_UPDATED = CLAZZ_NAME + ".MISSION_UPDATED";
    public static final String MISSION_DRONIE_CREATED = CLAZZ_NAME + "" +
            ".MISSION_DRONIE_CREATED";
    public static final String MISSION_SENT = CLAZZ_NAME + ".MISSION_SENT";
    public static final String MISSION_RECEIVED = CLAZZ_NAME + ".MISSION_RECEIVED";
    public static final String MISSION_ITEM_UPDATED = CLAZZ_NAME + ".MISSION_ITEM_UPDATED";

    /**
     * Parameter attribute events.
     */
    public static final String PARAMETERS_REFRESH_STARTED = CLAZZ_NAME + ".PARAMETERS_REFRESH_STARTED";
    public static final String PARAMETERS_REFRESH_ENDED = CLAZZ_NAME + ".PARAMETERS_REFRESH_ENDED";
    public static final String PARAMETERS_RECEIVED = CLAZZ_NAME + ".PARAMETERS_RECEIVED";

    public static final String TYPE_UPDATED = CLAZZ_NAME + ".TYPE_UPDATED";

    /**
     * Signal attribute events.
     */
    public static final String SIGNAL_UPDATED = CLAZZ_NAME + ".SIGNAL_UPDATED";
    public static final String SIGNAL_WEAK = CLAZZ_NAME + ".SIGNAL_WEAK";

    /**
     * Speed attribute events.
     */
    public static final String SPEED_UPDATED = CLAZZ_NAME + ".SPEED_UPDATED";

    /**
     * Battery attribute events.
     */
    public static final String BATTERY_UPDATED = CLAZZ_NAME + ".BATTERY_UPDATED";

    /**
     * State attribute events.
     */
    public static final String STATE_UPDATED = CLAZZ_NAME + ".STATE_UPDATED";
    public static final String STATE_ARMING = CLAZZ_NAME + ".STATE_ARMING";
    public static final String STATE_CONNECTED = CLAZZ_NAME + ".STATE_CONNECTED";
    public static final String STATE_DISCONNECTED = CLAZZ_NAME + ".STATE_DISCONNECTED";
    public static final String STATE_VEHICLE_MODE = CLAZZ_NAME + ".STATE_VEHICLE_MODE";

    /**
     * Home attribute events.
     */
    public static final String HOME_UPDATED = CLAZZ_NAME + ".HOME_UPDATED";

    /**
     * Gps' attribute events.
     */
    public static final String GPS_POSITION = CLAZZ_NAME + ".GPS_POSITION";
    public static final String GPS_FIX = CLAZZ_NAME + ".GPS_FIX";
    public static final String GPS_COUNT = CLAZZ_NAME + ".GPS_COUNT";
    public static final String WARNING_NO_GPS = CLAZZ_NAME + ".WARNING_NO_GPS";

    public static final String HEARTBEAT_FIRST = CLAZZ_NAME + ".HEARTBEAT_FIRST";
    public static final String HEARTBEAT_RESTORED = CLAZZ_NAME + ".HEARTBEAT_RESTORED";
    public static final String HEARTBEAT_TIMEOUT = CLAZZ_NAME + ".HEARTBEAT_TIMEOUT";

    /**
     * Altitude's attribute events.
     */
    public static final String ALTITUDE_400FT_EXCEEDED = CLAZZ_NAME +
            ".ALTITUDE_400FT_EXCEEDED";

}
