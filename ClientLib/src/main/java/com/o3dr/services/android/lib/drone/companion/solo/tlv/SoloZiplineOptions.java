package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Created by phu on 7/6/16.
 */
public class SoloZiplineOptions extends SoloShotOptions {
    public static final int MESSAGE_LENGTH = 6;

    /**
     * Zipline can do a 3D path (with altitude) or 2d
     */
    private boolean is3D;

    /**
     * Zipline can lock the camera on a GPS spot while the copter is moving
     */
    private boolean cameraLock;

    public boolean is3D() {
        return is3D;
    }

    public void setIs3D(boolean is3D) {
        this.is3D = is3D;
    }

    public boolean isCameraLock() {
        return cameraLock;
    }

    public void setCameraLock(boolean cameraLock) {
        this.cameraLock = cameraLock;
    }

    public SoloZiplineOptions(float cruiseSpeed, boolean is3D, boolean cameraLock) {
        super(TLVMessageTypes.TYPE_SOLO_ZIPLINE_OPTIONS, MESSAGE_LENGTH, cruiseSpeed);
        this.is3D = is3D;
        this.cameraLock = cameraLock;
    }

    SoloZiplineOptions(ByteBuffer buffer) {
        this(buffer.getFloat(), buffer.get() == 1, buffer.get() == 1);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        super.getMessageValue(valueCarrier);
        valueCarrier.put((byte) (is3D ? 1 : 0));
        valueCarrier.put((byte) (cameraLock ? 1 : 0));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(is3D ? (byte) 1 : (byte) 0);
        dest.writeByte(cameraLock ? (byte) 1 : (byte) 0);
    }

    protected SoloZiplineOptions(Parcel in) {
        super(in);
        this.is3D = in.readByte() != 0;
        this.cameraLock = in.readByte() != 0;
    }

    public static final Creator<SoloZiplineOptions> CREATOR = new Creator<SoloZiplineOptions>() {
        public SoloZiplineOptions createFromParcel(Parcel source) {
            return new SoloZiplineOptions(source);
        }

        public SoloZiplineOptions[] newArray(int size) {
            return new SoloZiplineOptions[size];
        }
    };

    @Override
    public String toString() {
        return "SoloZiplineOptions{" +
                "cruiseSpeed=" + getCruiseSpeed() +
                "is3D=" + is3D +
                "cameraLock=" + cameraLock +
                '}';
    }
}
