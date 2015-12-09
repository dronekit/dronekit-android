package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Shotmanager to App.  Optional. Valid only in playback mode
 * <p/>
 * Used by Shotmanager to transmit meta information about the currently defined Path.  It is optional -- Shotmanager does not guarantee it will ever send this message.  The data in this message is advisory, but not critical -- the app isn’t functionally dependent on receiving it.
 * <p/>
 * Shotmanager should attempt to send this message:
 * Each time it enters PLAY mode.
 * When it receives a SOLO_SPLINE_PATH_SETTINGS message
 * <p/>
 * Optionally, shotmanager can also resend the information any time previous values become invalid.  For example, high winds or a low battery might mean that the previously reported maxUVelocity isn’t realistic;  Shotmanager would re-send the message to advise the app to change its estimates.
 * <p/>
 * <p/>
 * Created by Fredia Huya-Kouadio on 12/8/15.
 *
 * @since 2.8.0
 */
public class SoloSplinePathStatus extends TLVPacket {

    public static final int MESSAGE_LENGTH = 24;

    /**
     * The minimum uVelocity the app should send in SOLO_SPLINE_SEEK messages
     */
    private float minUVelocity;

    /**
     * The estimated time it will take to fly the entire path at minUVelocity
     */
    private float minUVelocityTime;

    /**
     * The maximum uVelocity the app should send to SOLO_SPLINE_SEEK.
     */
    private float maxUVelocity;

    /**
     * The estimated time it will take to fly the entire path at maxUVelocity.
     */
    private float maxUVelocityTime;

    /**
     * A specific velocity set by the SOLO_SPLINE_PATH_SETTINGS message.
     */
    private float queryUVelocity;

    /**
     * The estimated time to fly the entire path at queryUVelocity.
     */
    private float queryUVelocityTime;

    public SoloSplinePathStatus(float minUVelocity, float minUVelocityTime, float maxUVelocity, float maxUVelocityTime, float queryUVelocity, float queryUVelocityTime) {
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_PATH_STATUS, MESSAGE_LENGTH);
        this.maxUVelocity = maxUVelocity;
        this.maxUVelocityTime = maxUVelocityTime;
        this.minUVelocity = minUVelocity;
        this.minUVelocityTime = minUVelocityTime;
        this.queryUVelocity = queryUVelocity;
        this.queryUVelocityTime = queryUVelocityTime;
    }

    public SoloSplinePathStatus(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getFloat(),
                buffer.getFloat(), buffer.getFloat(),
                buffer.getFloat(), buffer.getFloat());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(minUVelocity);
        valueCarrier.putFloat(minUVelocityTime);
        valueCarrier.putFloat(maxUVelocity);
        valueCarrier.putFloat(maxUVelocityTime);
        valueCarrier.putFloat(queryUVelocity);
        valueCarrier.putFloat(queryUVelocityTime);
    }

    public float getMaxUVelocity() {
        return maxUVelocity;
    }

    public float getMaxUVelocityTime() {
        return maxUVelocityTime;
    }

    public float getMinUVelocity() {
        return minUVelocity;
    }

    public float getMinUVelocityTime() {
        return minUVelocityTime;
    }

    public float getQueryUVelocity() {
        return queryUVelocity;
    }

    public float getQueryUVelocityTime() {
        return queryUVelocityTime;
    }

    @Override
    public String toString() {
        return "SoloSplinePathStatus{" +
                "maxUVelocity=" + maxUVelocity +
                ", minUVelocity=" + minUVelocity +
                ", minUVelocityTime=" + minUVelocityTime +
                ", maxUVelocityTime=" + maxUVelocityTime +
                ", queryUVelocity=" + queryUVelocity +
                ", queryUVelocityTime=" + queryUVelocityTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloSplinePathStatus)) return false;
        if (!super.equals(o)) return false;

        SoloSplinePathStatus that = (SoloSplinePathStatus) o;

        if (Float.compare(that.minUVelocity, minUVelocity) != 0) return false;
        if (Float.compare(that.minUVelocityTime, minUVelocityTime) != 0) return false;
        if (Float.compare(that.maxUVelocity, maxUVelocity) != 0) return false;
        if (Float.compare(that.maxUVelocityTime, maxUVelocityTime) != 0) return false;
        if (Float.compare(that.queryUVelocity, queryUVelocity) != 0) return false;
        return Float.compare(that.queryUVelocityTime, queryUVelocityTime) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (minUVelocity != +0.0f ? Float.floatToIntBits(minUVelocity) : 0);
        result = 31 * result + (minUVelocityTime != +0.0f ? Float.floatToIntBits(minUVelocityTime) : 0);
        result = 31 * result + (maxUVelocity != +0.0f ? Float.floatToIntBits(maxUVelocity) : 0);
        result = 31 * result + (maxUVelocityTime != +0.0f ? Float.floatToIntBits(maxUVelocityTime) : 0);
        result = 31 * result + (queryUVelocity != +0.0f ? Float.floatToIntBits(queryUVelocity) : 0);
        result = 31 * result + (queryUVelocityTime != +0.0f ? Float.floatToIntBits(queryUVelocityTime) : 0);
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.minUVelocity);
        dest.writeFloat(this.minUVelocityTime);
        dest.writeFloat(this.maxUVelocity);
        dest.writeFloat(this.maxUVelocityTime);
        dest.writeFloat(this.queryUVelocity);
        dest.writeFloat(this.queryUVelocityTime);
    }

    protected SoloSplinePathStatus(Parcel in) {
        super(in);
        this.minUVelocity = in.readFloat();
        this.minUVelocityTime = in.readFloat();
        this.maxUVelocity = in.readFloat();
        this.maxUVelocityTime = in.readFloat();
        this.queryUVelocity = in.readFloat();
        this.queryUVelocityTime = in.readFloat();
    }

    public static final Creator<SoloSplinePathStatus> CREATOR = new Creator<SoloSplinePathStatus>() {
        public SoloSplinePathStatus createFromParcel(Parcel source) {
            return new SoloSplinePathStatus(source);
        }

        public SoloSplinePathStatus[] newArray(int size) {
            return new SoloSplinePathStatus[size];
        }
    };
}
