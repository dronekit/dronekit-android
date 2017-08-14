package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.nio.ByteBuffer;

import timber.log.Timber;

/**
 * Sent by the Solo when a picture is taken
 */
public class SoloGoproPhoto extends TLVPacket {
    public static final int MESSAGE_LENGTH = 24;

    private LatLongAlt coordinate;
    private long time;

    public SoloGoproPhoto(double lat, double lng, float alt, long time) {
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_PHOTO, MESSAGE_LENGTH);
        Timber.d("SoloGoproPhoto(): lat=%.2f lng=%.2f alt=%.2f time=%d", lat, lng, alt, time);
        this.coordinate = new LatLongAlt(lat, lng, alt);
        this.time = time;
    }

    public SoloGoproPhoto(ByteBuffer buffer) {
        this(buffer.getDouble(), buffer.getDouble(), buffer.getFloat(), buffer.getInt());
    }

    public LatLongAlt getCoordinate() { return this.coordinate; }
    public void setCoordinate(LatLongAlt coord) { this.coordinate = coord; }

    public long getTime() { return this.time; }
    public void setTime(long time) { this.time = time; }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        Timber.d("getMessageValue(): coordinate=%s time=%d", coordinate, time);
        valueCarrier.putDouble(coordinate.getLatitude());
        valueCarrier.putDouble(coordinate.getLongitude());
        valueCarrier.putFloat((float)coordinate.getAltitude());
        valueCarrier.putInt((int)time);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.coordinate, 0);
        dest.writeLong(time);
    }

    protected SoloGoproPhoto(Parcel in) {
        super(in);
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
        this.time = in.readLong();
    }

    public static final Creator<SoloGoproPhoto> CREATOR = new Creator<SoloGoproPhoto>() {
        public SoloGoproPhoto createFromParcel(Parcel source) {
            return new SoloGoproPhoto(source);
        }

        public SoloGoproPhoto[] newArray(int size) {
            return new SoloGoproPhoto[size];
        }
    };
}
