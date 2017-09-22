package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

/**
 * FenceStatus atttribute
 */
public class FenceStatus implements DroneAttribute {

    private long breachTime;
    private int breachCount;
    private short breachStatus;
    private short breachType;

    public FenceStatus() {
        super();
    }

    public FenceStatus(long time, int count, short status, short type) {
        this();
        this.breachTime = time;
        this.breachCount = count;
        this.breachStatus = status;
        this.breachType = type;
    }

    public long getBreachTime() { return breachTime; }
    public void setBreachTime(long breachTime) { this.breachTime = breachTime; }

    public int getBreachCount() { return breachCount; }
    public void setBreachCount(int breachCount) { this.breachCount = breachCount; }

    public short getBreachStatus() { return breachStatus; }
    public void setBreachStatus(short breachStatus) { this.breachStatus = breachStatus; }

    public short getBreachType() { return breachType; }
    public void setBreachType(short breachType) { this.breachType = breachType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FenceStatus that = (FenceStatus) o;

        if (breachTime != that.breachTime) return false;
        if (breachCount != that.breachCount) return false;
        if (breachStatus != that.breachStatus) return false;
        return breachType == that.breachType;

    }

    @Override
    public int hashCode() {
        int result = (int) (breachTime ^ (breachTime >>> 32));
        result = 31 * result + breachCount;
        result = 31 * result + (int) breachStatus;
        result = 31 * result + (int) breachType;
        return result;
    }

    @Override
    public String toString() {
        return "FenceStatus{" +
                "breachTime=" + breachTime +
                ", breachCount=" + breachCount +
                ", breachStatus=" + breachStatus +
                ", breachType=" + breachType +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(breachTime);
        dest.writeInt(breachCount);
        dest.writeInt(breachStatus);
        dest.writeInt(breachType);
    }

    private FenceStatus(Parcel in) {
        breachTime = in.readLong();
        breachCount = in.readInt();
        breachStatus = (short)in.readInt();
        breachType = (short)in.readInt();
    }

    public static final Creator<FenceStatus> CREATOR = new Creator<FenceStatus>() {
        public FenceStatus createFromParcel(Parcel source) {
            return new FenceStatus(source);
        }

        public FenceStatus[] newArray(int size) {
            return new FenceStatus[size];
        }
    };
}
