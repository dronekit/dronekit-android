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

    @IntDef({POWER, CAPTURE_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestCommand{}

    public static final short POWER = 0;
    public static final short CAPTURE_MODE = 1;

    @IntDef({POWER_OFF, POWER_ON,
            CAPTURE_MODE_VIDEO, CAPTURE_MODE_PHOTO, CAPTURE_MODE_BURST, CAPTURE_MODE_TIME_LAPSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestCommandValue{}

    public static final short POWER_OFF = 0;
    public static final short POWER_ON = 1;
    public static final short CAPTURE_MODE_VIDEO = 0;
    public static final short CAPTURE_MODE_PHOTO = 1;
    public static final short CAPTURE_MODE_BURST = 2;
    public static final short CAPTURE_MODE_TIME_LAPSE = 3;

    @RequestCommand
    private short command;

    @RequestCommandValue
    private short value;

    public SoloGoproSetRequest(@RequestCommand short requestCommand, @RequestCommandValue short value){
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

    @RequestCommandValue
    public short getValue() {
        return value;
    }

    public void setValue(@RequestCommandValue short value) {
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
        @RequestCommand short readCommand = (short) in.readInt();
        @RequestCommandValue short readValue = (short) in.readInt();

        this.command = readCommand;
        this.value = readValue;
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
