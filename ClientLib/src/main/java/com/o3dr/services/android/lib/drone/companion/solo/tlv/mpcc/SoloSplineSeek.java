package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * App to Shotmanager.  Valid only in playback mode.
 * <p/>
 * This message tells shotmanager to fly the vehicle to a position along the normalized length of the Path.  It should attempt to to fly at uVelocity, but there is no guarantee.
 * <p/>
 * uVelocity must be positive -- there is no way to direct shotmanger to fly away from a particular uPosition, only toward it.
 * <p/>
 * Created by Fredia Huya-Kouadio on 12/8/15.
 *
 * @since 2.8.0
 */
public class SoloSplineSeek extends TLVPacket {

    public static final int MESSAGE_LENGTH = 8;

    /**
     * A parametric offset along the Path normalized to (0,1)
     */
    private float uPosition;

    /**
     * A velocity along the path in terms of uPosition;  du/dt.  This value must be positive.
     */
    private int cruiseState;

    public SoloSplineSeek(float uPosition, int cruiseState){
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_SEEK, MESSAGE_LENGTH);
        this.uPosition = uPosition;
        this.cruiseState = cruiseState;
    }

    public SoloSplineSeek(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getInt());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier){
        valueCarrier.putFloat(uPosition);
        valueCarrier.putInt(cruiseState);
    }

    public float getUPosition() {
        return uPosition;
    }

    public int getCruiseState() {
        return cruiseState;
    }

    @Override
    public String toString() {
        return "SoloSplineSeek{" +
                "uPosition=" + uPosition +
                ", cruiseState=" + cruiseState +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.uPosition);
        dest.writeInt(this.cruiseState);
    }

    protected SoloSplineSeek(Parcel in) {
        super(in);
        this.uPosition = in.readFloat();
        this.cruiseState = in.readInt();
    }

    public static final Creator<SoloSplineSeek> CREATOR = new Creator<SoloSplineSeek>() {
        public SoloSplineSeek createFromParcel(Parcel source) {
            return new SoloSplineSeek(source);
        }

        public SoloSplineSeek[] newArray(int size) {
            return new SoloSplineSeek[size];
        }
    };

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

        SoloSplineSeek that = (SoloSplineSeek) o;

        if (Float.compare(that.uPosition, uPosition) != 0) {
            return false;
        }
        return cruiseState == that.cruiseState;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uPosition != +0.0f ? Float.floatToIntBits(uPosition) : 0);
        result = 31 * result + cruiseState;
        return result;
    }
}
