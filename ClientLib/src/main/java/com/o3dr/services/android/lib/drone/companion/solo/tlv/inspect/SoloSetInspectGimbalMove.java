package com.o3dr.services.android.lib.drone.companion.solo.tlv.inspect;

import android.os.Parcel;
import android.util.Log;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Created by chavi on 12/9/15.
 */
public class SoloSetInspectGimbalMove extends TLVPacket {
    private float pitch;
    private float roll;
    private float yaw;

    public SoloSetInspectGimbalMove(float pitch, float roll, float yaw) {
        super(2002, 12);

        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
    }

    protected SoloSetInspectGimbalMove(Parcel in) {
        super(in);
        pitch = in.readFloat();
        roll = in.readFloat();
        yaw = in.readFloat();
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        Log.e("CHAVI", "message: " + pitch + " " + roll + " " + yaw);
        valueCarrier.putFloat(pitch);
        valueCarrier.putFloat(roll);
        valueCarrier.putFloat(yaw);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(pitch);
        dest.writeFloat(roll);
        dest.writeFloat(yaw);
    }

    public static final Creator<SoloSetInspectGimbalMove> CREATOR = new Creator<SoloSetInspectGimbalMove>() {
        public SoloSetInspectGimbalMove createFromParcel(Parcel source) {
            return new SoloSetInspectGimbalMove(source);
        }

        public SoloSetInspectGimbalMove[] newArray(int size) {
            return new SoloSetInspectGimbalMove[size];
        }
    };
}
