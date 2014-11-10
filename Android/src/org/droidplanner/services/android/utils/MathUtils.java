package org.droidplanner.services.android.utils;

import android.graphics.Point;

import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.coordinate.LatLongAlt;
import com.ox3dr.services.android.lib.coordinate.Point3D;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;

import java.util.ArrayList;
import java.util.List;

import ellipsoidFit.ThreeSpacePoint;

/**
 * Created by fhuya on 11/4/14.
 */
public class MathUtils {

    public static ArrayList<Point3D> threeSpacePointToPoint3D(List<ThreeSpacePoint> spacePoints){
        final ArrayList<Point3D> pointsList = new ArrayList<Point3D>();

        if(spacePoints != null && !spacePoints.isEmpty()) {
            for (ThreeSpacePoint spacePoint : spacePoints) {
                Point3D point = new Point3D(spacePoint.x, spacePoint.y, spacePoint.z);
                pointsList.add(point);
            }
        }

        return pointsList;
    }

    public static ArrayList<ThreeSpacePoint> point3DToThreeSpacePoint(List<Point3D> points){
        final ArrayList<ThreeSpacePoint> spacePoints = new ArrayList<ThreeSpacePoint>();

        if(points != null && !points.isEmpty()){
            for(Point3D point: points){
                ThreeSpacePoint spacePoint = new ThreeSpacePoint(point.x, point.y, point.z);
                spacePoints.add(spacePoint);
            }
        }

        return spacePoints;
    }

    public static Coord2D latLongToCoord2D(LatLong latLong){
        return new Coord2D(latLong.getLatitude(), latLong.getLongitude());
    }

    public static LatLong coord2DToLatLong(Coord2D coord){
        return new LatLong((float)coord.getLat(), (float)coord.getLng());
    }

    public static LatLongAlt coord3DToLatLongAlt(Coord3D coord){
        return new LatLongAlt((float)coord.getLat(), (float) coord.getLng(),
                (float) coord.getAltitude().valueInMeters());
    }

    public static Coord3D latLongAltToCoord3D(LatLongAlt position){
        return new Coord3D(position.getLatitude(), position.getLongitude(),
                new Altitude(position.getAltitude()));
    }

    public static List<LatLong> coord2DToLatLong(List<Coord2D> coords){
        final List<LatLong> points = new ArrayList<LatLong>();
        if(coords != null && !coords.isEmpty()){
            for(Coord2D coord: coords){
                points.add(coord2DToLatLong(coord));
            }
        }

        return points;
    }

    public static List<Coord2D> latLongToCoord2D(List<LatLong> points){
        final List<Coord2D> coords = new ArrayList<Coord2D>();
        if(points != null && !points.isEmpty()){
            for(LatLong point : points){
                coords.add(latLongToCoord2D(point));
            }
        }

        return coords;
    }
}
