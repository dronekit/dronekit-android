package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

/**
 * Sent from app to solo to request that solo begin a shot.
 */
public class SoloMessageShotSetter extends SoloMessageShot {
    public SoloMessageShotSetter(int shotType) {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT, shotType);
    }
}
