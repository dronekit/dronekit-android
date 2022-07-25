package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * The vehicle will climb straight up from it’s current location to the altitude specified (in meters).
 * This should be the first command of VTOL mission.
 * If the mission is begun while the vehicle is already flying, the vehicle will climb straight up to the specified altitude.
 * If the vehicle is already above the specified altitude the vtol takeoff command will be ignored and the mission will move onto the next command immediately.
 *
 * Created by tajisoft on 25/7/22.
 */
public class VtolTakeoff extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private double takeoffAltitude;

    public VtolTakeoff(){
        super(MissionItemType.VTOL_TAKEOFF);
    }

    public VtolTakeoff(VtolTakeoff copy){
        this();
        takeoffAltitude = copy.takeoffAltitude;
    }

    /**
     * @return take off altitude in meters
     */
    public double getTakeoffAltitude() {
        return takeoffAltitude;
    }

    /**
     * Sets the take off altitude
     * @param takeoffAltitude Altitude value in meters
     */
    public void setTakeoffAltitude(double takeoffAltitude) {
        this.takeoffAltitude = takeoffAltitude;
    }

    @Override
    public String toString() {
        return "VtolTakeoff{" +
                "takeoffAltitude=" + takeoffAltitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VtolTakeoff)) return false;
        if (!super.equals(o)) return false;

        VtolTakeoff vtolTakeoff = (VtolTakeoff) o;

        return Double.compare(vtolTakeoff.takeoffAltitude, takeoffAltitude) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(takeoffAltitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.takeoffAltitude);
    }

    private VtolTakeoff(Parcel in) {
        super(in);
        this.takeoffAltitude = in.readDouble();
    }

    @Override
    public MissionItem clone() {
        return new VtolTakeoff(this);
    }

    public static final Creator<VtolTakeoff> CREATOR = new Creator<VtolTakeoff>() {
        public VtolTakeoff createFromParcel(Parcel source) {
            return new VtolTakeoff(source);
        }

        public VtolTakeoff[] newArray(int size) {
            return new VtolTakeoff[size];
        }
    };
}
