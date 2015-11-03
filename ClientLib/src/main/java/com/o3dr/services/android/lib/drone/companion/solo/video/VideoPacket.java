package com.o3dr.services.android.lib.drone.companion.solo.video;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

/**
 * Wrapper class for a raw video data, allowing it to be transmitted over android IPC mechanism.
 */
public class VideoPacket implements Parcelable
{
    private byte[] mBytes;
    private int mValidLength;

    public VideoPacket(byte[] bytes, int valid_length) {
        mValidLength = valid_length;
        if (true)
        {
            this.mBytes = new byte[valid_length];
            System.arraycopy(bytes, 0, this.mBytes, 0, valid_length);
        } else
        {
            this.mBytes = bytes;
        }
    }

    public byte[] data()
    {
        return this.mBytes;
    }
    public int size()
    {
        return mValidLength;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mValidLength);
        dest.writeByteArray(this.mBytes, 0, this.mValidLength);
    }

    private VideoPacket(Parcel in) {
        this.mValidLength = in.readInt();
        this.mBytes= new byte[this.mValidLength];
        in.readByteArray(this.mBytes);
    }

    public static final Parcelable.Creator<VideoPacket> CREATOR = new Parcelable.Creator<VideoPacket>() {
        public VideoPacket createFromParcel(Parcel source) {
            return new VideoPacket(source);
        }

        public VideoPacket[] newArray(int size) {
            return new VideoPacket[size];
        }
    };
}
