package com.o3dr.services.android.lib.coordinate;

/** TODO This is the start of using a more generic location/frame class
 *  The frame for LatLongAlt would only ever be GLOBAL and not LOCAL.
 */

public enum Frame {
    GLOBAL_ABS      ("Absolute" , "Abs", 0 ),   // Absolute means Above Mean Sea Level AMSL
    LOCAL_NED       ("Local NED", "NED", 1 ),
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

    String getAbbreviation() {
        return abbreviation;
    }

    int getFrameAsInt() {
        return frame;
    }
}
