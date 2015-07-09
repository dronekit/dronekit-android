package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import java.nio.ByteBuffer;

/**
 * Debugging tool - shotmanager sends this to the app when it has hit an exception.
 */
public class SoloMessageShotManagerError extends TLVPacket {

    private final String exceptionInfo;

    public SoloMessageShotManagerError(String exceptionInfo) {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR, exceptionInfo.length());
        this.exceptionInfo = exceptionInfo;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put(exceptionInfo.getBytes());
    }
}
