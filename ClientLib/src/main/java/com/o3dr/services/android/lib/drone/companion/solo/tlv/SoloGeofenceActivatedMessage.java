package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Solo->app: Fence is activated
 */
public class SoloGeofenceActivatedMessage extends TLVPacket {

    public SoloGeofenceActivatedMessage() {
        super(TLVMessageTypes.TYPE_SOLO_GEOFENCE_ACTIVATED, 0);
    }

    @Override
    protected void getMessageValue(ByteBuffer byteBuffer) {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected SoloGeofenceActivatedMessage(Parcel in) {
        super(in);
    }

    public static final Creator<SoloGeofenceActivatedMessage> CREATOR = new Creator<SoloGeofenceActivatedMessage>() {
        public SoloGeofenceActivatedMessage createFromParcel(Parcel source) {
            return new SoloGeofenceActivatedMessage(source);
        }

        public SoloGeofenceActivatedMessage[] newArray(int size) {
            return new SoloGeofenceActivatedMessage[size];
        }
    };
}
