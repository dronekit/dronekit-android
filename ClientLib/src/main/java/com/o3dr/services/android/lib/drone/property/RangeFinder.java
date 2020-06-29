package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

public class RangeFinder implements DroneAttribute {
    private float distance;
    private float voltage;
    private final Map<Integer, DistanceSensor> sensors = new HashMap<>();

    public RangeFinder() {
        super();
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public Map<Integer, DistanceSensor> getSensors() {
        return sensors;
    }

    @Override
    public String toString() {
        return "RangeFinder{" +
                "distance=" + distance +
                ", voltage=" + voltage +
                ", sensors=" + sensors +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(distance);
        dest.writeFloat(voltage);
    }

    private RangeFinder(Parcel in) {
        distance = in.readFloat();
        voltage = in.readFloat();
    }

    public static final Creator<RangeFinder> CREATOR = new Creator<RangeFinder>() {
        public RangeFinder createFromParcel(Parcel source) {
            return new RangeFinder(source);
        }

        public RangeFinder[] newArray(int size) {
            return new RangeFinder[size];
        }
    };
}
