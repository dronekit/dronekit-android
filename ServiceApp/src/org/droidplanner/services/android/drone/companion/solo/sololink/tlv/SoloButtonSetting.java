package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import java.nio.ByteBuffer;

/**
 * Base class for button mapping setting.
 */
public abstract class SoloButtonSetting extends TLVPacket {


    private int button;
    private int event;

    /**
     * shot index, -1 if none.  One of shot/mode should be -1, and the other should have a value
     */
    private int shotType;

    /**
     * APM mode index, -1 if none
     */
    private int flightMode;

    public SoloButtonSetting(int messageType, int button, int event, int shotType, int flightModeIndex) {
        super(messageType, 16);
        this.button = button;
        this.event = event;
        this.shotType = shotType;
        this.flightMode = flightModeIndex;
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getShotType() {
        return shotType;
    }

    public int getFlightMode() {
        return flightMode;
    }

    public void setShotTypeFlightMode(int shotType, int flightMode) {
        this.shotType = shotType;
        this.flightMode = flightMode;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(button);
        valueCarrier.putInt(event);
        valueCarrier.putInt(shotType);
        valueCarrier.putInt(flightMode);
    }
}
