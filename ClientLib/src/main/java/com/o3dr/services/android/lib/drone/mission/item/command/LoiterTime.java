package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * MAV_CMD_NAV_LOITER_TIME
 *
 * Begin loiter at the specified coordinate, and don't consider this waypoint complete
 * until the specified delay in seconds has elapsed.
 */
public class LoiterTime extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private LatLongAlt coordinate;
    private long delay;
    private double radius;

    public LoiterTime(){
        super(MissionItemType.LOITER_TIME);
    }

    public LoiterTime(LoiterTime src){
        super(MissionItemType.LOITER_TIME);
        coordinate = src.coordinate;
    }

    public LatLongAlt getCoordinate() { return coordinate; }
    public void setCoordinate(LatLongAlt coord) {
        coordinate = coord;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.coordinate.getLatitude());
        dest.writeDouble(this.coordinate.getLongitude());
        dest.writeDouble(this.coordinate.getAltitude());
        dest.writeLong(this.delay);
        dest.writeDouble(this.radius);
    }

    private LoiterTime(Parcel in) {
        super(in);
        double lat = in.readDouble();
        double lng = in.readDouble();
        double alt = in.readDouble();
        this.coordinate = new LatLongAlt(lat, lng, alt);
        this.delay = in.readLong();
        this.radius = in.readDouble();
    }

    @Override
    public MissionItem clone() {
        return new LoiterTime(this);
    }

    @Override
    public String toString() {
        return "LoiterTime{" +
                "coordinate=" + coordinate +
                ", delay=" + delay +
                ", radius=" + radius +
                '}';
    }

    public static final Creator<LoiterTime> CREATOR = new Creator<LoiterTime>() {
        public LoiterTime createFromParcel(Parcel source) {
            return new LoiterTime(source);
        }

        public LoiterTime[] newArray(int size) {
            return new LoiterTime[size];
        }
    };
}
