package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Path: App->Solo
 *
 * This takes a JSON Object structured like this:
 *
 * {
 *     coord: [[lat1,lng1],[lat2,lng2],[lat3,lng3]...],
 *     subCoord: [[lat1,lng1],[lat2,lng2],[lat3,lng3]...],
 *     type: [0,0,0]
 * }
 *
 * All arrays must be the same length.
 * coord must have >= 3 elements in it.
 * Each element in a coord/subCoord has 2 parts (lat,lng)
 * type is "StayOut", a Bool (1 byte)
 *
 * When sent, GeofenceManager clears its polygons and makes a new set from this data.
 */
public class SoloGeofenceSetDataMessage extends TLVPacket {
    private final String json;

    public SoloGeofenceSetDataMessage(String json) {
        super(TLVMessageTypes.TYPE_SOLO_GEOFENCE_SET_DATA, json.length());
        this.json = json;
    }

    @Override
    protected void getMessageValue(ByteBuffer byteBuffer) {
        byteBuffer.put(this.json.getBytes());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.json);
    }

    protected SoloGeofenceSetDataMessage(Parcel in) {
        super(in);
        this.json = in.readString();
    }

    public static final Creator<SoloGeofenceSetDataMessage> CREATOR = new Creator<SoloGeofenceSetDataMessage>() {
        public SoloGeofenceSetDataMessage createFromParcel(Parcel source) {
            return new SoloGeofenceSetDataMessage(source);
        }

        public SoloGeofenceSetDataMessage[] newArray(int size) {
            return new SoloGeofenceSetDataMessage[size];
        }
    };

    @Override
    public String toString() {
        return "SoloGeofenceSetDataMessage{" +
                "json='" + json + '\'' +
                '}';
    }
}
