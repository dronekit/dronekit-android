package com.o3dr.services.android.lib.drone.companion.solo.tlv.inspect;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Created by chavi on 12/8/15.
 */
public class SoloMessageStartInspect extends TLVPacket {
    float alt;

    public SoloMessageStartInspect(float alt) {
        super(2000, 4);
        this.alt = alt;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(alt);
    }

    protected SoloMessageStartInspect(Parcel in) {
        super(in);
        this.alt = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.alt);
    }

    public static final Creator<SoloMessageStartInspect> CREATOR = new Creator<SoloMessageStartInspect>() {
        public SoloMessageStartInspect createFromParcel(Parcel source) {
            return new SoloMessageStartInspect(source);
        }

        public SoloMessageStartInspect[] newArray(int size) {
            return new SoloMessageStartInspect[size];
        }
    };
}
