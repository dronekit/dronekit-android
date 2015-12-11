package com.o3dr.services.android.lib.drone.companion.solo.tlv.inspect;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Created by chavi on 12/9/15.
 */
public class SoloSetInspectWP extends TLVPacket {
    float lat;
    float lon;
    float alt;

    public SoloSetInspectWP(float lat, float lon, float alt) {
        super(2001, 12);
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(lat);
        valueCarrier.putFloat(lon);
        valueCarrier.putFloat(alt);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.lat);
        dest.writeFloat(this.lon);
        dest.writeFloat(this.alt);
    }

    protected SoloSetInspectWP(Parcel in) {
        super(in);
        lat = in.readFloat();
        lon = in.readFloat();
        alt = in.readFloat();
    }

    public static final Creator<SoloSetInspectWP> CREATOR = new Creator<SoloSetInspectWP>() {
        public SoloSetInspectWP createFromParcel(Parcel source) {
            return new SoloSetInspectWP(source);
        }

        public SoloSetInspectWP[] newArray(int size) {
            return new SoloSetInspectWP[size];
        }
    };
}
