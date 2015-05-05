package org.droidplanner.services.android.utils;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 11/4/14.
 */
public class MathUtils {

    public static Coord2D latLongToCoord2D(LatLong latLong) {
        return new Coord2D(latLong.getLatitude(), latLong.getLongitude());
    }

    public static LatLong coord2DToLatLong(Coord2D coord) {
        return new LatLong(coord.getLat(), coord.getLng());
    }

    public static LatLongAlt coord3DToLatLongAlt(Coord3D coord) {
        return new LatLongAlt(coord.getLat(), coord.getLng(),
                coord.getAltitude());
    }

    public static Coord3D latLongAltToCoord3D(LatLongAlt position) {
        return new Coord3D(position.getLatitude(), position.getLongitude(), position.getAltitude());
    }

    public static List<LatLong> coord2DToLatLong(List<Coord2D> coords) {
        final List<LatLong> points = new ArrayList<LatLong>();
        if (coords != null && !coords.isEmpty()) {
            for (Coord2D coord : coords) {
                points.add(coord2DToLatLong(coord));
            }
        }

        return points;
    }

    public static List<Coord2D> latLongToCoord2D(List<LatLong> points) {
        final List<Coord2D> coords = new ArrayList<Coord2D>();
        if (points != null && !points.isEmpty()) {
            for (LatLong point : points) {
                coords.add(latLongToCoord2D(point));
            }
        }

        return coords;
    }
}
