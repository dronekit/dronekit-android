package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * App->Solo
 *
 * Update a polygon
 */
public class SoloGeofenceUpdatePolyMessage extends TLVPacket {

    byte updateType;
    short polygonIndex;
    short vertexIndex;
    double lat;
    double lng;
    double subLat;
    double subLng;

    public SoloGeofenceUpdatePolyMessage(String json) {
        super(TLVMessageTypes.TYPE_SOLO_GEOFENCE_UPDATE_POLY, json.length());
    }

    @Override
    protected void getMessageValue(ByteBuffer byteBuffer) {
        byteBuffer.put(updateType);
        byteBuffer.putShort(polygonIndex);
        byteBuffer.putShort(vertexIndex);
        byteBuffer.putDouble(lat);
        byteBuffer.putDouble(lng);
        byteBuffer.putDouble(subLat);
        byteBuffer.putDouble(subLng);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(updateType);
        dest.writeInt(polygonIndex);
        dest.writeInt(vertexIndex);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeDouble(subLat);
        dest.writeDouble(subLng);
    }

    protected SoloGeofenceUpdatePolyMessage(Parcel in) {
        super(in);
        updateType = in.readByte();
        polygonIndex = (short)in.readInt();
        vertexIndex = (short)in.readInt();
        lat = in.readDouble();
        lng = in.readDouble();
        subLat = in.readDouble();
        subLng = in.readDouble();
    }

    public static final Creator<SoloGeofenceUpdatePolyMessage> CREATOR = new Creator<SoloGeofenceUpdatePolyMessage>() {
        public SoloGeofenceUpdatePolyMessage createFromParcel(Parcel source) {
            return new SoloGeofenceUpdatePolyMessage(source);
        }

        public SoloGeofenceUpdatePolyMessage[] newArray(int size) {
            return new SoloGeofenceUpdatePolyMessage[size];
        }
    };
}
