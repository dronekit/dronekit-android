package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 4/13/15.
 */
public class SoloShotError extends TLVPacket {

    public static final int SHOT_ERROR_BAD_EKF = 0;
    public static final int SHOT_ERROR_UNARMED = 1;

    private int errorType;

    public SoloShotError(int errorType){
        super(TLVMessageTypes.TYPE_SOLO_SHOT_OPTIONS, 4);
        this.errorType = errorType;
    }

    public int getErrorType() {
        return errorType;
    }

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(errorType);
    }
}
