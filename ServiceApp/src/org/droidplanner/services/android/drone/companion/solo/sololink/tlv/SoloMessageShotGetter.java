package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

/**
 * Sent from solo to app when it enters a shot.
 */
public class SoloMessageShotGetter extends SoloMessageShot {
    public SoloMessageShotGetter(int shotType) {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT, shotType);
    }
}
