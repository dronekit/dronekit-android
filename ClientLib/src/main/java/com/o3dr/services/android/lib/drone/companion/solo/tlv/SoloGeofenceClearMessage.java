package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Bi-directional:
 * App->Solo: Clear the current fence
 * Solo->App: Current fence has been cleared (but not in response to a CLEAR message from the app)
 */
public class SoloGeofenceClearMessage extends TLVPacket {

    public SoloGeofenceClearMessage() {
        super(TLVMessageTypes.TYPE_SOLO_GEOFENCE_CLEAR, 0);
    }

    protected SoloGeofenceClearMessage(Parcel in) {
        super(in);
    }

    @Override
    protected void getMessageValue(ByteBuffer byteBuffer) {

    }

    public static final Creator<SoloGeofenceClearMessage> CREATOR = new Creator<SoloGeofenceClearMessage>() {
        public SoloGeofenceClearMessage createFromParcel(Parcel source) {
            return new SoloGeofenceClearMessage(source);
        }

        public SoloGeofenceClearMessage[] newArray(int size) {
            return new SoloGeofenceClearMessage[size];
        }
    };
}
