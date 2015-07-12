package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Created by djmedina on 4/15/15.
 */
public class ArtooMessageInputReport extends TLVPacket {

    private double timestamp;
    private short gimbalY;
    private short gimbalRate;
    private short battery;

    public ArtooMessageInputReport(double timestamp, short gimbalY, short gimbalRate, short battery) {
        super(TLVMessageTypes.TYPE_ARTOO_INPUT_REPORT_MESSAGE, 14);
        this.timestamp = timestamp;
        this.gimbalY = gimbalY;
        this.gimbalRate = gimbalRate;
        this.battery = battery;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public short getGimbalY() {
        return gimbalY;
    }

    public short getGimbalRate() {
        return gimbalRate;
    }

    public short getBattery() {
        return battery;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putDouble(timestamp);
        valueCarrier.putShort(gimbalY);
        valueCarrier.putShort(gimbalRate);
        valueCarrier.putShort(battery);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.timestamp);
        dest.writeInt(this.gimbalY);
        dest.writeInt(this.gimbalRate);
        dest.writeInt(this.battery);
    }

    protected ArtooMessageInputReport(Parcel in) {
        super(in);
        this.timestamp = in.readDouble();
        this.gimbalY = (short) in.readInt();
        this.gimbalRate = (short) in.readInt();
        this.battery = (short) in.readInt();
    }

    public static final Creator<ArtooMessageInputReport> CREATOR = new Creator<ArtooMessageInputReport>() {
        public ArtooMessageInputReport createFromParcel(Parcel source) {
            return new ArtooMessageInputReport(source);
        }

        public ArtooMessageInputReport[] newArray(int size) {
            return new ArtooMessageInputReport[size];
        }
    };
}
