package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;
import android.support.annotation.IntDef;

import java.nio.ByteBuffer;

/**
 * Created by phu on 6/24/16.
 */
public class SoloPanoOptions extends SoloShotOptions {

    public static final int MESSAGE_LENGTH = 12;

    /**
     * Pano can automatically run, is it running?
     */
    private static final int PANO_ON_VALUE = 1;
    private static final int PANO_OFF_VALUE = 0;

    private boolean isRunning;

    /**
     * Pano has multiple sub modes
     */
    @IntDef({
            PANO_PREFERENCE_CYLINDRICAL,
            PANO_PREFERENCE_SPHERICAL,
            PANO_PREFERENCE_VIDEO
    })
    public @interface PanoPreference{}

    public static final int PANO_PREFERENCE_CYLINDRICAL = 0;
    public static final int PANO_PREFERENCE_SPHERICAL = 1;
    public static final int PANO_PREFERENCE_VIDEO = 2;
    @PanoPreference
    private int panoPreference;

    /**
     * Pan angle (used in cylindrical)
     */
    private short panAngle;

    /**
     * Yaw speed (used in video)
     */
    private float degreesPerSecondYawSpeed;

    /**
     * Camera FOV
     */
    private float cameraFOV;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @PanoPreference
    public int getPanoPreference() {
        return panoPreference;
    }

    public void setPanoPreference(int panoPreference) {
        this.panoPreference = panoPreference;
    }

    public short getPanAngle() {
        return panAngle;
    }

    public void setPanAngle(short panAngle) {
        this.panAngle = panAngle;
    }

    public float getDegreesPerSecondYawSpeed() {
        return degreesPerSecondYawSpeed;
    }

    public void setDegreesPerSecondYawSpeed(float degreesPerSecondYawSpeed) {
        this.degreesPerSecondYawSpeed = degreesPerSecondYawSpeed;
    }

    public float getCameraFOV() {
        return cameraFOV;
    }

    public void setCameraFOV(float cameraFOV) {
        this.cameraFOV = cameraFOV;
    }

    public SoloPanoOptions(boolean isRunning, int panoPreference, short panAngle, float degreesPerSecondYawSpeed, float cameraFOV) {
        super(TLVMessageTypes.TYPE_SOLO_PANO_OPTIONS, MESSAGE_LENGTH, PAUSED_CRUISE_SPEED);
        this.panoPreference = panoPreference;
        this.isRunning = isRunning;
        this.panAngle = panAngle;
        this.degreesPerSecondYawSpeed = degreesPerSecondYawSpeed;
        this.cameraFOV = cameraFOV;
    }

    SoloPanoOptions(ByteBuffer buffer) {
        this(buffer.get() ==  PANO_ON_VALUE, buffer.get(), (short) buffer.getShort(), buffer.getFloat(), buffer.getFloat());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put((byte) panoPreference);
        valueCarrier.put((byte) (isRunning ? 1 : 0));
        valueCarrier.putShort(panAngle);
        valueCarrier.putFloat(degreesPerSecondYawSpeed);
        valueCarrier.putFloat(cameraFOV);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) panoPreference);
        dest.writeByte(isRunning ? (byte) 1 : (byte) 0);
        dest.writeInt(panAngle);
        dest.writeFloat(degreesPerSecondYawSpeed);
        dest.writeFloat(cameraFOV);
    }

    protected SoloPanoOptions(Parcel in) {
        super(in);
        @PanoPreference int panoPreference = (int) in.readByte();
        this.panoPreference = panoPreference;
        this.isRunning = in.readByte() != 0;
        this.panAngle = (short) in.readInt();
        this.degreesPerSecondYawSpeed = in.readFloat();
        this.cameraFOV = in.readFloat();
    }

    public static final Creator<SoloPanoOptions> CREATOR = new Creator<SoloPanoOptions>() {
        public SoloPanoOptions createFromParcel(Parcel source) {
            return new SoloPanoOptions(source);
        }

        public SoloPanoOptions[] newArray(int size) {
            return new SoloPanoOptions[size];
        }
    };

    @Override
    public String toString() {
        return "SoloPanoOptions{" +
                "panoPreference=" + panoPreference +
                "isRunning=" + isRunning +
                "panAngle=" + panAngle +
                "degreesPerSecondYawSpeed=" + degreesPerSecondYawSpeed +
                "cameraFOV=" + cameraFOV +
                '}';
    }
}
