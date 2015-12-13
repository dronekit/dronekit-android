package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Bidirectional
 * <p/>
 * Used to transmit proposed or actual Keypoints on the current camera Path.
 * <p/>
 * Shotmanager uses this message for two things:
 * To transmit Keypoints it creates in response to Artoo button presses or SOLO_MESSAGE_RECORD_POSITION messages.
 * To confirm the validity of Keypoints it receives from the app;  to ACK those Keypoints.
 * <p/>
 * When shotmanager creates a Keypoint, it assigns an index.  When the app receives this message and a Keypoint with the index already exists, it updates the Keypoint to the values in this message;  it replaces the existing Keypoint.
 * <p/>
 * The app uses this message to load Keypoints from previously recorded, known valid Paths into shotmanager.
 * <p/>
 * In every case, shotmanager sends a SOLO_SPLINE_POINT message back to the app to confirm it was able to create the Keypoint.  If it can't create the Keypoint, it sends a failure status.
 * <p/>
 * <p/>
 * Created by Fredia Huya-Kouadio on 12/8/15.
 *
 * @since 2.8.0
 */
public class SoloSplinePoint extends TLVPacket {

    public static final int MESSAGE_LENGTH = 38;

    public static final short STATUS_SUCCESS = 0;
    public static final short STATUS_MODE_ERROR = -1; //tried setting a spline point when we were already in PLAY mode
    public static final short STATUS_KEYPOINTS_TOO_CLOSE_ERROR = -2; //Keypoint too close to a previous keypoint
    public static final short STATUS_DUPLICATE_INDEX_ERROR = -3; //Received multiple keypoints for a single index.

    private int index;
    private LatLongAlt coordinate;

    private float pitch;
    private float yaw;

    /**
     * Parametric offset of the Keypoint along the Path.
     * In Record mode, these values have no meaning since they can't be assigned until the Path is complete.
     * In Play mode, shotmanager assigns these values and sends messages to the app.
     * The app never creates these values.
     */
    private float uPosition;

    /**
     * Shotmanager sends this value to indicate success or failure when creating a Keypoint.
     * Negative values are failure;
     * 0 or positive is success.
     */
    private short status;

    public SoloSplinePoint(int index, LatLongAlt coordinate, float pitch, float yaw, float uPosition, short status) {
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_POINT, MESSAGE_LENGTH);
        this.coordinate = coordinate;
        this.index = index;
        this.pitch = pitch;
        this.status = status;
        this.uPosition = uPosition;
        this.yaw = yaw;
    }

    public SoloSplinePoint(ByteBuffer dataBuffer){
        this(dataBuffer.getInt(),
                new LatLongAlt(dataBuffer.getDouble(), dataBuffer.getDouble(), dataBuffer.getFloat()),
                dataBuffer.getFloat(),
                dataBuffer.getFloat(),
                dataBuffer.getFloat(),
                dataBuffer.getShort());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier){
        valueCarrier.putInt(index);
        valueCarrier.putDouble(coordinate.getLatitude());
        valueCarrier.putDouble(coordinate.getLongitude());
        valueCarrier.putFloat((float) coordinate.getAltitude());
        valueCarrier.putFloat(pitch);
        valueCarrier.putFloat(yaw);
        valueCarrier.putFloat(uPosition);
        valueCarrier.putShort(status);
    }

    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    public int getIndex() {
        return index;
    }

    public float getPitch() {
        return pitch;
    }

    public short getStatus() {
        return status;
    }

    public float getUPosition() {
        return uPosition;
    }

    public float getYaw() {
        return yaw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloSplinePoint)) return false;
        if (!super.equals(o)) return false;

        SoloSplinePoint that = (SoloSplinePoint) o;

        if (index != that.index) return false;
        if (Float.compare(that.pitch, pitch) != 0) return false;
        if (Float.compare(that.yaw, yaw) != 0) return false;
        if (Float.compare(that.uPosition, uPosition) != 0) return false;
        if (status != that.status) return false;
        return !(coordinate != null ? !coordinate.equals(that.coordinate) : that.coordinate != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + index;
        result = 31 * result + (coordinate != null ? coordinate.hashCode() : 0);
        result = 31 * result + (pitch != +0.0f ? Float.floatToIntBits(pitch) : 0);
        result = 31 * result + (yaw != +0.0f ? Float.floatToIntBits(yaw) : 0);
        result = 31 * result + (uPosition != +0.0f ? Float.floatToIntBits(uPosition) : 0);
        result = 31 * result + (int) status;
        return result;
    }

    @Override
    public String toString() {
        return "SoloSplinePoint{" +
                "coordinate=" + coordinate +
                ", index=" + index +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                ", uPosition=" + uPosition +
                ", status=" + status +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.index);
        dest.writeParcelable(this.coordinate, 0);
        dest.writeFloat(this.pitch);
        dest.writeFloat(this.yaw);
        dest.writeFloat(this.uPosition);
        dest.writeInt(this.status);
    }

    protected SoloSplinePoint(Parcel in) {
        super(in);
        this.index = in.readInt();
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
        this.pitch = in.readFloat();
        this.yaw = in.readFloat();
        this.uPosition = in.readFloat();
        this.status = (short) in.readInt();
    }

    public static final Creator<SoloSplinePoint> CREATOR = new Creator<SoloSplinePoint>() {
        public SoloSplinePoint createFromParcel(Parcel source) {
            return new SoloSplinePoint(source);
        }

        public SoloSplinePoint[] newArray(int size) {
            return new SoloSplinePoint[size];
        }
    };
}
