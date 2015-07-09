package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import java.nio.ByteBuffer;

/**
 * Sent from app to Solo or vice versa to transmit cable cam options.
 */
public class SoloCableCamOptions extends SoloShotOptions {

    private static final int CAM_INTERPOLATION_ENABLED_VALUE = 1;
    private static final int CAM_INTERPOLATION_DISABLED_VALUE = 0;

    private static final int YAW_DIRECTION_CW_VALUE = 0;
    private static final int YAW_DIRECTION_CCW_VALUE = 1;
    /**
     * 0 if interpolation is off
     * 1 if on
     */
    private boolean camInterpolation;

    /**
     * 1 means counter clock wise
     * 0 means clockwise
     * Received from shot manager, and shouldn't be persisted in the app.
     */
    private boolean yawDirectionClockwise;

    public SoloCableCamOptions(boolean camInterpolation, boolean yawDirectionClockwise, float cruiseSpeed) {
        super(TLVMessageTypes.TYPE_SOLO_CABLE_CAM_OPTIONS, 8, cruiseSpeed);
        this.camInterpolation = camInterpolation;
        this.yawDirectionClockwise = yawDirectionClockwise;
    }

    SoloCableCamOptions(int camInterpolationValue, int yawDirectionValue, float cruiseSpeed) {
        this(camInterpolationValue == CAM_INTERPOLATION_ENABLED_VALUE,
                yawDirectionValue == YAW_DIRECTION_CW_VALUE,
                cruiseSpeed);
    }

    public boolean isCamInterpolationOn() {
        return camInterpolation;
    }

    public void setCamInterpolation(boolean camInterpolation) {
        this.camInterpolation = camInterpolation;
    }

    public boolean isYawDirectionClockWise() {
        return yawDirectionClockwise;
    }

    public void setYawDirection(boolean yawDirection) {
        this.yawDirectionClockwise = yawDirection;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putShort((short) (camInterpolation ? CAM_INTERPOLATION_ENABLED_VALUE : CAM_INTERPOLATION_DISABLED_VALUE));
        valueCarrier.putShort((short) (yawDirectionClockwise ? YAW_DIRECTION_CW_VALUE : YAW_DIRECTION_CCW_VALUE));
        super.getMessageValue(valueCarrier);
    }
}
