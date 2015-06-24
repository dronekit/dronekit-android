package org.droidplanner.core.helpers.geoTools;

import org.droidplanner.core.helpers.coordinates.Coord2D;

import java.util.List;

public class PolylineTools {

    /**
     * Total length of the polyline in meters
     *
     * @param gridPoints
     * @return
     */
    public static double getPolylineLength(List<Coord2D> gridPoints) {
        double length = 0;
        for (int i = 1; i < gridPoints.size(); i++) {
            final Coord2D to = gridPoints.get(i - 1);
            if (to == null) {
                continue;
            }

            length += GeoTools.getDistance(gridPoints.get(i), to);
        }
        return length;
    }

}
