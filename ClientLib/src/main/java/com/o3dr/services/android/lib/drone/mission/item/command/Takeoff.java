package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class Takeoff extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    /**
     * Default takeoff altitude in meters.
     */
    public static final double DEFAULT_TAKEOFF_ALTITUDE = 10.0;

    private double takeoffAltitude;

    public Takeoff(){
        super(MissionItemType.TAKEOFF);
    }

    public Takeoff(Takeoff copy){
        this();
        takeoffAltitude = copy.takeoffAltitude;
    }

    public double getTakeoffAltitude() {
        return takeoffAltitude;
    }

    public void setTakeoffAltitude(double takeoffAltitude) {
        this.takeoffAltitude = takeoffAltitude;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.takeoffAltitude);
    }

    private Takeoff(Parcel in) {
        super(in);
        this.takeoffAltitude = in.readDouble();
    }

    @Override
    public MissionItem clone() {
        return new Takeoff(this);
    }

    public static final Creator<Takeoff> CREATOR = new Creator<Takeoff>() {
        public Takeoff createFromParcel(Parcel source) {
            return new Takeoff(source);
        }

        public Takeoff[] newArray(int size) {
            return new Takeoff[size];
        }
    };
}
