package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * Issue a record command to the gopro in either video or stills mode.
 * Created by Fredia Huya-Kouadio on 7/14/15.
 */
public class SoloGoproRecord extends TLVPacket {

    public static final int MESSAGE_LENGTH = 4;

    @IntDef({STOP_RECORDING, START_RECORDING, TOGGLE_RECORDING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RecordCommand{}

    public static final int STOP_RECORDING = 0;
    public static final int START_RECORDING = 1;
    public static final int TOGGLE_RECORDING = 2;

    private int recordCommand;

    public SoloGoproRecord(@RecordCommand int recordCommand){
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_RECORD, MESSAGE_LENGTH);
        this.recordCommand = recordCommand;
    }

    @RecordCommand
    public int getRecordCommand() {
        return recordCommand;
    }

    public void setRecordCommand(@RecordCommand int recordCommand) {
        this.recordCommand = recordCommand;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(recordCommand);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.recordCommand);
    }

    protected SoloGoproRecord(Parcel in) {
        super(in);
        this.recordCommand = in.readInt();
    }

    public static final Creator<SoloGoproRecord> CREATOR = new Creator<SoloGoproRecord>() {
        public SoloGoproRecord createFromParcel(Parcel source) {
            return new SoloGoproRecord(source);
        }

        public SoloGoproRecord[] newArray(int size) {
            return new SoloGoproRecord[size];
        }
    };
}
