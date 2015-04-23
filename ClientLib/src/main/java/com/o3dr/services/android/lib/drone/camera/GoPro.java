package com.o3dr.services.android.lib.drone.camera;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Fredia Huya-Kouadio on 4/7/15.
 */
public class GoPro implements Parcelable {

    private boolean isConnected;
    private boolean isRecording;

    public GoPro() {}

    public GoPro(boolean isConnected, boolean isRecording) {
        this.isConnected = isConnected;
        this.isRecording = isRecording;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isConnected ? (byte) 1 : (byte) 0);
        dest.writeByte(isRecording ? (byte) 1 : (byte) 0);
    }

    private GoPro(Parcel in) {
        this.isConnected = in.readByte() != 0;
        this.isRecording = in.readByte() != 0;
    }

    public static final Parcelable.Creator<GoPro> CREATOR = new Parcelable.Creator<GoPro>() {
        public GoPro createFromParcel(Parcel source) {
            return new GoPro(source);
        }

        public GoPro[] newArray(int size) {
            return new GoPro[size];
        }
    };
}
