package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Created by phu on 7/6/16.
 */
public class SoloZiplineLock extends TLVPacket {
    public SoloZiplineLock() {
        super(TLVMessageTypes.TYPE_SOLO_ZIPLINE_LOCK, 0);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected SoloZiplineLock(Parcel in) {
        super(in);
    }

    public static final Creator<SoloZiplineLock> CREATOR = new Creator<SoloZiplineLock>() {
        public SoloZiplineLock createFromParcel(Parcel source) {
            return new SoloZiplineLock(source);
        }

        public SoloZiplineLock[] newArray(int size) {
            return new SoloZiplineLock[size];
        }
    };

    @Override
    public String toString() {
        return "SoloZiplineLock{}";
    }
}
