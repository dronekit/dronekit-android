package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

public class MavlinkConnectionStats implements DroneAttribute {
    public int receivedCount;
    public int crcErrorCount;
    public int lostPacketCount;

    public MavlinkConnectionStats() {
        super();
    }

    public void set(int received, int crcErrors, int droppedCount) {
        receivedCount = received;
        crcErrorCount = crcErrors;
        lostPacketCount = droppedCount;
    }

    public int getReceivedCount() {
        return receivedCount;
    }

    public int getCrcErrorCount() {
        return crcErrorCount;
    }

    public int getLostPacketCount() {
        return lostPacketCount;
    }

    public static Creator<MavlinkConnectionStats> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.receivedCount);
        dest.writeInt(this.crcErrorCount);
        dest.writeInt(this.lostPacketCount);
    }

    protected MavlinkConnectionStats(Parcel in) {
        this.receivedCount = in.readInt();
        this.crcErrorCount = in.readInt();
        this.lostPacketCount = in.readInt();
    }

    public static final Creator<MavlinkConnectionStats> CREATOR = new Creator<MavlinkConnectionStats>() {
        @Override
        public MavlinkConnectionStats createFromParcel(Parcel source) {
            return new MavlinkConnectionStats(source);
        }

        @Override
        public MavlinkConnectionStats[] newArray(int size) {
            return new MavlinkConnectionStats[size];
        }
    };
}
