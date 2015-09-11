package org.droidplanner.services.android.core.firmware;

public enum FirmwareType {
    /* APM firmware types */
    ARDU_PLANE("ArduPlane"),
    ARDU_COPTER("ArduCopter"),
    ARDU_ROVER("ArduRover"),
    ARDU_SOLO("ArduSolo"),

    /* PX4 firmware type */
    PX4_NATIVE("PX4 Native");

    private final String type;

    FirmwareType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
