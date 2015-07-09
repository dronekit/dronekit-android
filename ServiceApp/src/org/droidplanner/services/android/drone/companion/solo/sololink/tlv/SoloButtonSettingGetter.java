package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

/**
 * Sent from app to Solo to request Button mapping setting.  Sent from Solo to app as a response.
 */
public class SoloButtonSettingGetter extends SoloButtonSetting {
    public SoloButtonSettingGetter(int button, int event, int shotType, int flightModeIndex) {
        super(TLVMessageTypes.TYPE_SOLO_GET_BUTTON_SETTING, button, event, shotType, flightModeIndex);
    }

    public SoloButtonSettingGetter(int button, int event){
        this(button, event, -1, -1);
    }
}
