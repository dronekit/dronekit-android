package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * A set request message will be transmitted to the camera.
 * Created by Fredia Huya-Kouadio on 7/14/15.
 */
public class SoloGoproSetRequest extends TLVPacket {

    public static final int MESSAGE_LENGTH = 4;

    @SoloGoproConstants.RequestCommand
    private short command;

    @SoloGoproConstants.RequestCommandValue
    private short value;

    public SoloGoproSetRequest(@SoloGoproConstants.RequestCommand short requestCommand, @SoloGoproConstants.RequestCommandValue short value){
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_SET_REQUEST, MESSAGE_LENGTH);
        this.command = requestCommand;
        this.value = value;
    }

    @SoloGoproConstants.RequestCommand
    public short getCommand() {
        return command;
    }

    public void setCommand(@SoloGoproConstants.RequestCommand short command) {
        this.command = command;
    }

    @SoloGoproConstants.RequestCommandValue
    public short getValue() {
        return value;
    }

    public void setValue(@SoloGoproConstants.RequestCommandValue short value) {
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
        @SoloGoproConstants.RequestCommand short readCommand = (short) in.readInt();
        @SoloGoproConstants.RequestCommandValue short readValue = (short) in.readInt();

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
