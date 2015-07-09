package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import java.nio.ByteBuffer;

/**
 * Sent from app to Solo to request recording of a position.
 */
public class SoloMessageRecordPosition extends TLVPacket {
    public SoloMessageRecordPosition() {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_RECORD_POSITION, 0);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {}
}
