package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Sent from the Solo to the app to ACK (or NACK) a fence.
 */
public class SoloGeofenceAckMessage extends TLVPacket {

    private byte valid;
    private short count; // ushort

    public SoloGeofenceAckMessage() {
        super(TLVMessageTypes.TYPE_SOLO_GEOFENCE_SET_ACK, 3);
    }

    public SoloGeofenceAckMessage(short count, byte valid) {
        this();
        this.count = count;
        this.valid = valid;
    }

    public byte getValid() { return valid; }
    public void setValid(byte valid) { this.valid = valid; }
    public boolean isValid() { return (valid != 0); }

    public short getCount() { return this.count; }
    public void setCount(short count) { this.count = count; }

    @Override
    protected void getMessageValue(ByteBuffer byteBuffer) {
        byteBuffer.putShort(count);
        byteBuffer.put(valid);
    }

    public SoloGeofenceAckMessage(ByteBuffer buffer) {
        super(TLVMessageTypes.TYPE_SOLO_GEOFENCE_SET_ACK, 3);
        this.count = buffer.getShort();
        this.valid = buffer.get();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.count);
        dest.writeByte(this.valid);
    }

    protected SoloGeofenceAckMessage(Parcel in) {
        super(in);
        this.count = (short)in.readInt();
        this.valid = in.readByte();
    }

    public static final Creator<SoloGeofenceAckMessage> CREATOR = new Creator<SoloGeofenceAckMessage>() {
        public SoloGeofenceAckMessage createFromParcel(Parcel source) {
            return new SoloGeofenceAckMessage(source);
        }

        public SoloGeofenceAckMessage[] newArray(int size) {
            return new SoloGeofenceAckMessage[size];
        }
    };

    @Override
    public String toString() {
        return "SoloGeofenceSetAckMessage{" +
                "valid=" + valid +
                ", count=" + count +
                '}';
    }
}
