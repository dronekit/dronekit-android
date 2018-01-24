package com.o3dr.services.android.lib.coordinate;

import com.MAVLink.enums.MAV_FRAME;

/** TODO This is the start of using a more generic location/frame class
 *  The frame for LatLongAlt would only ever be GLOBAL and not LOCAL.
 */

public enum Frame {
    GLOBAL_ABS      ("Absolute" , "Abs", 0 ),   // Absolute means Above Mean Sea Level AMSL
    LOCAL_NED       ("Local NED", "NED", 1 ),
    MISSION         ("Mission"  , "MIS", 2 ),
    GLOBAL_RELATIVE ("Relative" , "Rel", 3 ),   // Relative to HOME location
    LOCAL_ENU       ("Local ENU", "ENU", 4 ),
    GLOBAL_TERRAIN  ("Terrain"  , "Ter", 10);   // Relative to Terrain Level. (Either measured or from STRM)

    private final int frame;
    private final String name;
    private final String abbreviation;

    Frame(String name, String abbreviation, int frame ) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.frame = frame;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public int asInt() {
        return frame;
    }

    public static Frame getFrame(int mavFrame) {

        switch (mavFrame) {
            case MAV_FRAME.MAV_FRAME_GLOBAL:
            case MAV_FRAME.MAV_FRAME_GLOBAL_INT:
                return Frame.GLOBAL_ABS;

            case MAV_FRAME.MAV_FRAME_MISSION:
                return Frame.MISSION;

            case MAV_FRAME.MAV_FRAME_LOCAL_NED:
                return Frame.LOCAL_NED;

            case MAV_FRAME.MAV_FRAME_LOCAL_ENU:
                return Frame.LOCAL_NED;

            case MAV_FRAME.MAV_FRAME_GLOBAL_TERRAIN_ALT:
            case MAV_FRAME.MAV_FRAME_GLOBAL_TERRAIN_ALT_INT:
                return Frame.GLOBAL_TERRAIN;

            case MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT:
            case MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT_INT:
            default:
                return  Frame.GLOBAL_RELATIVE;
        }

    }
}
