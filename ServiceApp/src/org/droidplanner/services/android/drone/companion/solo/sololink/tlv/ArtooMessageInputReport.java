package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import java.nio.ByteBuffer;

/**
 * Created by djmedina on 4/15/15.
 */
public class ArtooMessageInputReport extends TLVPacket {

    private double timestamp;
    private short gimbalY;
    private short gimbalRate;
    private short battery;

    public ArtooMessageInputReport(double timestamp, short gimbalY, short gimbalRate, short battery) {
        super(TLVMessageTypes.TYPE_ARTOO_INPUT_REPORT_MESSAGE, 14);
        this.timestamp = timestamp;
        this.gimbalY = gimbalY;
        this.gimbalRate = gimbalRate;
        this.battery = battery;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public short getGimbalY() {
        return gimbalY;
    }

    public short getGimbalRate() {
        return gimbalRate;
    }

    public short getBattery() {
        return battery;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {

        valueCarrier.putDouble(timestamp);
        valueCarrier.putShort(gimbalY);
        valueCarrier.putShort(gimbalRate);
        valueCarrier.putShort(battery);

    }
}
