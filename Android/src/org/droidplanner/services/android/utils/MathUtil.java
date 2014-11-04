package org.droidplanner.services.android.utils;

import android.graphics.Point;

import com.ox3dr.services.android.lib.coordinate.Point3D;

import java.util.ArrayList;
import java.util.List;

import ellipsoidFit.ThreeSpacePoint;

/**
 * Created by fhuya on 11/4/14.
 */
public class MathUtil {

    public static List<Point3D> threeSpacePointToPoint3D(List<ThreeSpacePoint> spacePoints){
        final List<Point3D> pointsList = new ArrayList<Point3D>();

        if(spacePoints != null && !spacePoints.isEmpty()) {
            for (ThreeSpacePoint spacePoint : spacePoints) {
                Point3D point = new Point3D(spacePoint.x, spacePoint.y, spacePoint.z);
                pointsList.add(point);
            }
        }

        return pointsList;
    }

    public static List<ThreeSpacePoint> point3DToThreeSpacePoint(List<Point3D> points){
        final List<ThreeSpacePoint> spacePoints = new ArrayList<ThreeSpacePoint>();

        if(points != null && !points.isEmpty()){
            for(Point3D point: points){
                ThreeSpacePoint spacePoint = new ThreeSpacePoint(point.x, point.y, point.z);
                spacePoints.add(spacePoint);
            }
        }

        return spacePoints;
    }
}
