package com.o3dr.services.android.lib.drone.attribute;

/**
 * Stores the set of attribute types.
 */
public class AttributeType {

    private static final String CLAZZ_NAME = AttributeType.class.getName();

    /**
     * Used to access the vehicle's altitude state.
     * @see {@link com.o3dr.services.android.lib.drone.property.Altitude}
     */
    public static final String ALTITUDE = CLAZZ_NAME + ".ALTITUDE";

    /**
     * Used to access the vehicle's attitude state.
     * @see {@link com.o3dr.services.android.lib.drone.property.Attitude}
     */
    public static final String ATTITUDE = CLAZZ_NAME + ".ATTITUDE";

    /**
     * Used to access the vehicle's battery state.
     * @see {@link com.o3dr.services.android.lib.drone.property.Speed}
     */
    public static final String BATTERY = CLAZZ_NAME + ".BATTERY";

    /**
     * Used to access the set of camera information available for the connected drone.
     * @see {@link com.o3dr.services.android.lib.drone.property.CameraProxy}
     */
    public static final String CAMERA = CLAZZ_NAME + ".CAMERA";

    /**
     * Used to acces the vehicle's follow state.
     * @see {@link com.o3dr.services.android.lib.gcs.follow.FollowState}
     */
    public static final String FOLLOW_STATE = CLAZZ_NAME + ".FOLLOW_STATE";

    /**
     * Used to access the vehicle's guided state.
     * @see {@link com.o3dr.services.android.lib.drone.property.GuidedState}
     */
    public static final String GUIDED_STATE = CLAZZ_NAME + ".GUIDED_STATE";

    /**
     * Used to access the vehicle's gps state.
     * @see {@link com.o3dr.services.android.lib.drone.property.Gps} object.
     */
    public static final String GPS = CLAZZ_NAME + ".GPS";

    /**
     * Used to access the vehicle's home state.
     * @see {@link com.o3dr.services.android.lib.drone.property.Home}
     */
    public static final String HOME = CLAZZ_NAME + ".HOME";

    /**
     * Used to access the vehicle's mission state.
     * @see {@link com.o3dr.services.android.lib.drone.mission.Mission}
     */
    public static final String MISSION = CLAZZ_NAME + ".MISSION";

    /**
     * Used to access the vehicle's parameters.
     * @see {@link com.o3dr.services.android.lib.drone.property.Parameters}
     * @see {@link com.o3dr.services.android.lib.drone.property.Parameter}
     */
    public static final String PARAMETERS = CLAZZ_NAME + ".PARAMETERS";

    /**
     * Used to access the vehicle's signal state.
     * @see {@link com.o3dr.services.android.lib.drone.property.Signal}
     */
    public static final String SIGNAL = CLAZZ_NAME + ".SIGNAL";

    /**
     * Used to access the vehicle's speed info.
     * @see {@link com.o3dr.services.android.lib.drone.property.Speed}
     */
    public static final String SPEED = CLAZZ_NAME + ".SPEED";

    /**
     * Used to access the vehicle state.
     * @see {@link com.o3dr.services.android.lib.drone.property.State} object.
     */
    public static final String STATE = CLAZZ_NAME + ".STATE";

    /**
     * Used to access the vehicle type.
     * @see {@link com.o3dr.services.android.lib.drone.property.Type}
     */
    public static final String TYPE = CLAZZ_NAME + ".TYPE";
}
