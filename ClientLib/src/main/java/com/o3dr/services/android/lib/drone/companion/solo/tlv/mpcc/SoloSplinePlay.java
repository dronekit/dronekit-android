package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 12/8/15.
 */
public class SoloSplinePlay extends TLVPacket {

    public static final int MESSAGE_LENGTH = 0;

    public SoloSplinePlay(){
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_PLAY, MESSAGE_LENGTH);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
    }

    protected SoloSplinePlay(Parcel in) {
        super(in);
    }

    public static final Creator<SoloSplinePlay> CREATOR = new Creator<SoloSplinePlay>() {
        public SoloSplinePlay createFromParcel(Parcel source) {
            return new SoloSplinePlay(source);
        }

        public SoloSplinePlay[] newArray(int size) {
            return new SoloSplinePlay[size];
        }
    };
}
