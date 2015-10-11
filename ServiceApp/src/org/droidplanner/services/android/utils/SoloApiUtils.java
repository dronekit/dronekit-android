package org.droidplanner.services.android.utils;

import android.os.RemoteException;
import android.util.Pair;
import android.view.Surface;

import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.SoloState;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.drone.companion.solo.SoloComp;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 7/29/15.
 */
public class SoloApiUtils {

    //Private to prevent instantiation.
    private SoloApiUtils() {
    }

    public static SoloState getSoloLinkState(DroneManager droneManager) {
        if (droneManager == null || !droneManager.isCompanionComputerEnabled())
            return null;

        final SoloComp soloComp = droneManager.getSoloComp();
        final Pair<String, String> wifiSettings = soloComp.getWifiSettings();
        return new SoloState(soloComp.getAutopilotVersion(), soloComp.getControllerFirmwareVersion(),
                soloComp.getControllerVersion(), soloComp.getVehicleVersion(),
                wifiSettings.second, wifiSettings.first, soloComp.isEUTxPowerCompliant(),
                soloComp.getButtonSettings(), soloComp.getGimbalVersion(),
                soloComp.getControllerMode(), soloComp.getControllerUnit());
    }

    static boolean isSoloLinkFeatureAvailable(DroneManager droneManager, ICommandListener listener) {
        if (droneManager == null)
            return false;

        if (!droneManager.isCompanionComputerEnabled()) {
            if (listener != null) {
                try {
                    listener.onError(CommandExecutionError.COMMAND_UNSUPPORTED);
                } catch (RemoteException e) {
                    Timber.e(e, e.getMessage());
                }
            }
            return false;
        }

        return true;
    }

    public static void sendSoloLinkMessage(DroneManager droneManager, TLVPacket messageData,
                                           ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(droneManager, listener) || messageData == null)
            return;

        final SoloComp soloComp = droneManager.getSoloComp();
        soloComp.sendSoloLinkMessage(messageData, listener);
    }

    public static void updateSoloLinkWifiSettings(DroneManager droneManager,
                                                  String wifiSsid, String wifiPassword,
                                                  ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(droneManager, listener))
            return;

        if (android.text.TextUtils.isEmpty(wifiSsid) && android.text.TextUtils.isEmpty(wifiPassword))
            return;

        final SoloComp soloComp = droneManager.getSoloComp();
        soloComp.updateWifiSettings(wifiSsid, wifiPassword, listener);
    }

    public static void updateSoloLinkButtonSettings(DroneManager droneManager,
                                                    SoloButtonSettingSetter buttonSettings,
                                                    ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(droneManager, listener) || buttonSettings == null)
            return;

        final SoloComp soloComp = droneManager.getSoloComp();
        soloComp.pushButtonSettings(buttonSettings, listener);
    }

    public static void updateSoloLinkControllerMode(DroneManager droneManager,
                                                    @SoloControllerMode.ControllerMode int mode,
                                                    ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(droneManager, listener))
            return;

        final SoloComp soloComp = droneManager.getSoloComp();
        soloComp.updateControllerMode(mode, listener);
    }

    public static void updateSoloControllerUnit(DroneManager droneManager, @SoloControllerUnits.ControllerUnit String unit, ICommandListener listener){
        if(!isSoloLinkFeatureAvailable(droneManager, listener))
            return;

        final SoloComp soloComp = droneManager.getSoloComp();
        soloComp.updateControllerUnit(unit, listener);
    }

    public static void updateSoloLinkEUTxPowerCompliance(DroneManager droneManager, boolean isCompliant, ICommandListener listener){
        if(!isSoloLinkFeatureAvailable(droneManager, listener))
            return;

        final SoloComp soloComp = droneManager.getSoloComp();
        soloComp.updateEUTxPowerCompliance(isCompliant, listener);
    }

    public static void startVideoStream(DroneManager droneManager, String appId, String videoTag, Surface videoSurface,
                                        ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(droneManager, listener))
            return;

        final SoloComp soloComp = droneManager.getSoloComp();
        soloComp.startVideoStream(appId, videoTag, videoSurface, listener);
    }

    public static void stopVideoStream(DroneManager droneManager, String appId, String videoTag, ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(droneManager, listener))
            return;

        final SoloComp soloComp = droneManager.getSoloComp();
        soloComp.stopVideoStream(appId, videoTag, listener);
    }
}
