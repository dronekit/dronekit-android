package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class SplineWaypoint extends BaseSpatialItem implements android.os.Parcelable {

    /**
     * Hold time in decimal seconds. (ignored by fixed wing, time to stay at
     * MISSION for rotary wing)
     */
    private double delay;

    public SplineWaypoint(){
        super(MissionItemType.SPLINE_WAYPOINT);
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.delay);
    }

    private SplineWaypoint(Parcel in) {
        super(in);
        this.delay = in.readDouble();
    }

    public static final Creator<SplineWaypoint> CREATOR = new Creator<SplineWaypoint>() {
        public SplineWaypoint createFromParcel(Parcel source) {
            return new SplineWaypoint(source);
        }

        public SplineWaypoint[] newArray(int size) {
            return new SplineWaypoint[size];
        }
    };
}
