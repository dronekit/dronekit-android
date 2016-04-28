package org.droidplanner.services.android.core.drone.autopilot.apm.solo.controller;

import org.droidplanner.services.android.lib.drone.companion.solo.button.ButtonPacket;
import org.droidplanner.services.android.lib.drone.companion.solo.controller.SoloControllerUnits;
import org.droidplanner.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import org.droidplanner.services.android.core.drone.autopilot.apm.solo.AbstractLinkManager;

/**
 * Created by Fredia Huya-Kouadio on 7/10/15.
 */
public interface ControllerLinkListener extends AbstractLinkManager.LinkListener {

    void onTlvPacketReceived(TLVPacket packet);

    void onWifiInfoUpdated(String wifiName, String wifiPassword);

    void onButtonPacketReceived(ButtonPacket packet);

    void onTxPowerComplianceCountryUpdated(String compliantCountry);

    void onControllerModeUpdated();

    void onControllerUnitUpdated(@SoloControllerUnits.ControllerUnit String trimmedResponse);
}
