package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * A set request message will be transmitted to the camera.
 * Created by Fredia Huya-Kouadio on 7/14/15.
 */
public class SoloGoproSetRequest extends TLVPacket {

    public static final int MESSAGE_LENGTH = 4;

    @IntDef({POWER, CAPTURE_MODE, SHUTTER_STATUS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestCommand{}

    public static final short POWER = 0;
    public static final short CAPTURE_MODE = 1;
    public static final short SHUTTER_STATUS = 2;

    private short command;
    private short value;

    public SoloGoproSetRequest(@RequestCommand short requestCommand, short value){
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_SET_REQUEST, MESSAGE_LENGTH);
        this.command = requestCommand;
        this.value = value;
    }

    @RequestCommand
    public short getCommand() {
        return command;
    }

    public void setCommand(@RequestCommand short command) {
        this.command = command;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putShort(command);
        valueCarrier.putShort(value);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.command);
        dest.writeInt(this.value);
    }

    protected SoloGoproSetRequest(Parcel in) {
        super(in);
        this.command = (short) in.readInt();
        this.value = (short) in.readInt();
    }

    public static final Creator<SoloGoproSetRequest> CREATOR = new Creator<SoloGoproSetRequest>() {
        public SoloGoproSetRequest createFromParcel(Parcel source) {
            return new SoloGoproSetRequest(source);
        }

        public SoloGoproSetRequest[] newArray(int size) {
            return new SoloGoproSetRequest[size];
        }
    };
}
