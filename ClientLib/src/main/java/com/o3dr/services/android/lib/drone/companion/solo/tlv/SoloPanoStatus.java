package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Created by phu on 7/17/16.
 */
public class SoloPanoStatus extends TLVPacket {

    private int currentStep;
    public static final int MESSAGE_LENGTH = 2;

    public int getCurrentStep() {
        return currentStep;
    }

    private int totalSteps;
    public int getTotalSteps() {
        return totalSteps;
    }

    public SoloPanoStatus(int currentStep, int totalSteps) {
        super(TLVMessageTypes.TYPE_SOLO_PANO_STATUS, MESSAGE_LENGTH);
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
    }

    SoloPanoStatus(ByteBuffer buffer) {
        this((int) buffer.get(), (int) buffer.get());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put((byte) currentStep);
        valueCarrier.put((byte) totalSteps);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) currentStep);
        dest.writeByte((byte) totalSteps);
    }

    protected SoloPanoStatus(Parcel in) {
        super(in);
        this.currentStep = (int) in.readByte();
        this.totalSteps = (int) in.readByte();
    }

    public static final Creator<SoloPanoStatus> CREATOR = new Creator<SoloPanoStatus>() {
        public SoloPanoStatus createFromParcel(Parcel source) {
            return new SoloPanoStatus(source);
        }
        public SoloPanoStatus[] newArray(int size) {
            return new SoloPanoStatus[size];
        }
    };

    @Override
    public String toString() {
        return "SoloPanoStatus{" +
                "currentStep=" + currentStep +
                "totalSteps=" + totalSteps +
                '}';
    }

}
