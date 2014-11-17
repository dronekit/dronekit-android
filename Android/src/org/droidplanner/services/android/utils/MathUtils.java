package org.droidplanner.services.android.utils;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

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

    public static double[][] threeSpacePointToPointsArray(List<ThreeSpacePoint> spacePoints){
        final int pointsCount = spacePoints == null ? 0 : spacePoints.size();
        final double[][] pointsArray = new double[3][pointsCount];

        if(pointsCount > 0) {
            for (int i = 0; i < pointsCount; i++) {
                ThreeSpacePoint point = spacePoints.get(i);
                pointsArray[0][i] = point.x;
                pointsArray[1][i] = point.y;
                pointsArray[2][i] = point.z;
            }
        }

        return pointsArray;
    }

    public static ArrayList<ThreeSpacePoint> pointsArrayToThreeSpacePoint(double[][] points){
        final int pointsCount = points == null ? 0 : points[0].length;
        final ArrayList<ThreeSpacePoint> spacePoints = new ArrayList<ThreeSpacePoint>(pointsCount);

        if(pointsCount > 0){
            for(int i = 0; i < pointsCount; i++){
                ThreeSpacePoint spacePoint = new ThreeSpacePoint(points[0][i], points[1][i],
                        points[2][i]);
                spacePoints.add(spacePoint);
            }
        }

        return spacePoints;
    }

    public static Coord2D latLongToCoord2D(LatLong latLong){
        return new Coord2D(latLong.getLatitude(), latLong.getLongitude());
    }

    public static LatLong coord2DToLatLong(Coord2D coord){
        return new LatLong(coord.getLat(), coord.getLng());
    }

    public static LatLongAlt coord3DToLatLongAlt(Coord3D coord){
        return new LatLongAlt(coord.getLat(), coord.getLng(),
                coord.getAltitude().valueInMeters());
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
