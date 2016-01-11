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
public class SoloSplineDuration extends TLVPacket {

    public static final int MESSAGE_LENGTH = 8;

    /**
     * The estimated time it will take to fly the entire path at minUVelocity
     */
    private float minUVelocityTime;

    /**
     * The estimated time it will take to fly the entire path at maxUVelocity.
     */
    private float maxUVelocityTime;

    public SoloSplineDuration(float minUVelocityTime, float maxUVelocityTime) {
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_DURATION, MESSAGE_LENGTH);
        this.maxUVelocityTime = maxUVelocityTime;
        this.minUVelocityTime = minUVelocityTime;
    }

    public SoloSplineDuration(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getFloat());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(minUVelocityTime);
        valueCarrier.putFloat(maxUVelocityTime);
    }

    public float getMaxUVelocityTime() {
        return maxUVelocityTime;
    }

    public float getMinUVelocityTime() {
        return minUVelocityTime;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.minUVelocityTime);
        dest.writeFloat(this.maxUVelocityTime);
    }

    protected SoloSplineDuration(Parcel in) {
        super(in);
        this.minUVelocityTime = in.readFloat();
        this.maxUVelocityTime = in.readFloat();
    }

    public static final Creator<SoloSplineDuration> CREATOR = new Creator<SoloSplineDuration>() {
        public SoloSplineDuration createFromParcel(Parcel source) {
            return new SoloSplineDuration(source);
        }

        public SoloSplineDuration[] newArray(int size) {
            return new SoloSplineDuration[size];
        }
    };

    @Override
    public String toString() {
        return "SoloSplineDuration{" +
            "minUVelocityTime=" + minUVelocityTime +
            ", maxUVelocityTime=" + maxUVelocityTime +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SoloSplineDuration that = (SoloSplineDuration) o;

        if (Float.compare(that.minUVelocityTime, minUVelocityTime) != 0) {
            return false;
        }
        return Float.compare(that.maxUVelocityTime, maxUVelocityTime) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (minUVelocityTime != +0.0f ? Float.floatToIntBits(minUVelocityTime) : 0);
        result = 31 * result + (maxUVelocityTime != +0.0f ? Float.floatToIntBits(maxUVelocityTime) : 0);
        return result;
    }
}
