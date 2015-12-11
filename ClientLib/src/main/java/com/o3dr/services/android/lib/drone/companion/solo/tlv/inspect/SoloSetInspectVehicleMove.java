package com.o3dr.services.android.lib.drone.companion.solo.tlv.inspect;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Created by chavi on 12/9/15.
 */
public class SoloSetInspectVehicleMove extends TLVPacket {
    private float vx;
    private float vy;
    private float vz;

    public SoloSetInspectVehicleMove(float vx, float vy, float vz) {
        super(2003, 12);
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(vx);
        valueCarrier.putFloat(vy);
        valueCarrier.putFloat(vz);
    }

    protected SoloSetInspectVehicleMove(Parcel in) {
        super(in);
        vx = in.readFloat();
        vy = in.readFloat();
        vz = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(vx);
        dest.writeFloat(vy);
        dest.writeFloat(vz);
    }


    public static final Creator<SoloSetInspectVehicleMove> CREATOR = new Creator<SoloSetInspectVehicleMove>() {
        public SoloSetInspectVehicleMove createFromParcel(Parcel source) {
            return new SoloSetInspectVehicleMove(source);
        }

        public SoloSetInspectVehicleMove[] newArray(int size) {
            return new SoloSetInspectVehicleMove[size];
        }
    };
}
