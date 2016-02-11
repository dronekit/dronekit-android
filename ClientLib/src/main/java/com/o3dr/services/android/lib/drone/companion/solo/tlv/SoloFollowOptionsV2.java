package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 12/29/15.
 */
public class SoloFollowOptionsV2 extends SoloFollowOptions {

    private static final int FREE_LOOK_ENABLED_VALUE = 1;
    private static final int FREE_LOOK_DISABLED_VALUE = 0;

    private boolean freeLook;

    public SoloFollowOptionsV2() {
        this(PAUSED_CRUISE_SPEED, true, false);
    }

    public SoloFollowOptionsV2(float cruiseSpeed, boolean lookAt, boolean freeLook){
        super(TLVMessageTypes.TYPE_SOLO_FOLLOW_OPTIONS_V2, 12, cruiseSpeed, lookAt);
        this.freeLook = freeLook;
    }

    SoloFollowOptionsV2(float cruiseSpeed, int lookAtValue, int freeLookValue){
        this(cruiseSpeed, lookAtValue == LOOK_AT_ENABLED_VALUE, freeLookValue == FREE_LOOK_ENABLED_VALUE);
    }

    SoloFollowOptionsV2(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getInt(), buffer.getInt());
    }

    public boolean isFreeLook() {
        return freeLook;
    }

    public void setFreeLook(boolean freeLook) {
        this.freeLook = freeLook;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloFollowOptionsV2)) return false;
        if (!super.equals(o)) return false;

        SoloFollowOptionsV2 that = (SoloFollowOptionsV2) o;

        return freeLook == that.freeLook;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (freeLook ? FREE_LOOK_ENABLED_VALUE : FREE_LOOK_DISABLED_VALUE);
        return result;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier){
        super.getMessageValue(valueCarrier);
        valueCarrier.putInt(freeLook ? FREE_LOOK_ENABLED_VALUE : FREE_LOOK_DISABLED_VALUE);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(freeLook ? (byte) FREE_LOOK_ENABLED_VALUE : (byte) FREE_LOOK_DISABLED_VALUE);
    }

    protected SoloFollowOptionsV2(Parcel in) {
        super(in);
        this.freeLook = in.readByte() != FREE_LOOK_DISABLED_VALUE;
    }

    public static final Creator<SoloFollowOptionsV2> CREATOR = new Creator<SoloFollowOptionsV2>() {
        public SoloFollowOptionsV2 createFromParcel(Parcel source) {
            return new SoloFollowOptionsV2(source);
        }

        public SoloFollowOptionsV2[] newArray(int size) {
            return new SoloFollowOptionsV2[size];
        }
    };
}
