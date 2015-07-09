package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * All communication follows this TLV format in little endian.
 */
public abstract class TLVPacket {

    public static final ByteOrder TLV_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private final int messageType;
    private final int messageLength;

    private final ByteBuffer byteBuffer;

    public TLVPacket(int type, int length){
        this.messageType = type;
        this.messageLength = length;

        byteBuffer = ByteBuffer.allocate(4 + 4 + messageLength);
        byteBuffer.order(TLV_BYTE_ORDER);
    }

    public final int getMessageType() {
        return messageType;
    }

    public final byte[] toBytes(){
        byteBuffer.clear();

        byteBuffer.putInt(this.messageType);
        byteBuffer.putInt(this.messageLength);

        getMessageValue(byteBuffer);

        final byte[] bytes = new byte[byteBuffer.position()];
        byteBuffer.rewind();
        byteBuffer.get(bytes);

        return bytes;
    }

    protected abstract void getMessageValue(ByteBuffer valueCarrier);
}
