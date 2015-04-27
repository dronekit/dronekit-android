package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Created by fhuya on 11/6/14.
 */
public class Waypoint extends BaseSpatialItem implements android.os.Parcelable {

    private double delay;
    private double acceptanceRadius;
    private double yawAngle;
    private double orbitalRadius;
    private boolean orbitCCW;

    public Waypoint(){
        super(MissionItemType.WAYPOINT);
    }

    public Waypoint(Waypoint copy){
        super(copy);
        delay = copy.delay;
        acceptanceRadius = copy.acceptanceRadius;
        yawAngle = copy.yawAngle;
        orbitalRadius = copy.orbitalRadius;
        orbitCCW = copy.orbitCCW;
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public double getAcceptanceRadius() {
        return acceptanceRadius;
    }

    public void setAcceptanceRadius(double acceptanceRadius) {
        this.acceptanceRadius = acceptanceRadius;
    }

    public double getYawAngle() {
        return yawAngle;
    }

    public void setYawAngle(double yawAngle) {
        this.yawAngle = yawAngle;
    }

    public double getOrbitalRadius() {
        return orbitalRadius;
    }

    public void setOrbitalRadius(double orbitalRadius) {
        this.orbitalRadius = orbitalRadius;
    }

    public boolean isOrbitCCW() {
        return orbitCCW;
    }

    public void setOrbitCCW(boolean orbitCCW) {
        this.orbitCCW = orbitCCW;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.delay);
        dest.writeDouble(this.acceptanceRadius);
        dest.writeDouble(this.yawAngle);
        dest.writeDouble(this.orbitalRadius);
        dest.writeByte(orbitCCW ? (byte) 1 : (byte) 0);
    }

    private Waypoint(Parcel in) {
        super(in);
        this.delay = in.readDouble();
        this.acceptanceRadius = in.readDouble();
        this.yawAngle = in.readDouble();
        this.orbitalRadius = in.readDouble();
        this.orbitCCW = in.readByte() != 0;
    }

    @Override
    public MissionItem clone() {
        return new Waypoint(this);
    }

    public static final Creator<Waypoint> CREATOR = new Creator<Waypoint>() {
        public Waypoint createFromParcel(Parcel source) {
            return new Waypoint(source);
        }

        public Waypoint[] newArray(int size) {
            return new Waypoint[size];
        }
    };
}
