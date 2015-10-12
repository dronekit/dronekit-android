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
import org.droidplanner.services.android.core.drone.autopilot.Drone;
import org.droidplanner.services.android.core.drone.autopilot.apm.ArduSolo;
import org.droidplanner.services.android.core.drone.companion.solo.SoloComp;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 7/29/15.
 */
public class SoloApiUtils {

    //Private to prevent instantiation.
    private SoloApiUtils() {
    }

    public static SoloState getSoloLinkState(ArduSolo arduSolo) {
        if (arduSolo == null)
            return null;

        final SoloComp soloComp = arduSolo.getSoloComp();
        final Pair<String, String> wifiSettings = soloComp.getWifiSettings();
        return new SoloState(soloComp.getAutopilotVersion(), soloComp.getControllerFirmwareVersion(),
                soloComp.getControllerVersion(), soloComp.getVehicleVersion(),
                wifiSettings.second, wifiSettings.first, soloComp.isEUTxPowerCompliant(),
                soloComp.getButtonSettings(), soloComp.getGimbalVersion(),
                soloComp.getControllerMode(), soloComp.getControllerUnit());
    }

    static boolean isSoloLinkFeatureAvailable(Drone drone, ICommandListener listener) {
        if (drone == null)
            return false;

        if(!(drone instanceof ArduSolo)){
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

    public static void sendSoloLinkMessage(ArduSolo arduSolo, TLVPacket messageData,
                                           ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(arduSolo, listener) || messageData == null)
            return;

        final SoloComp soloComp = arduSolo.getSoloComp();
        soloComp.sendSoloLinkMessage(messageData, listener);
    }

    public static void updateSoloLinkWifiSettings(ArduSolo arduSolo,
                                                  String wifiSsid, String wifiPassword,
                                                  ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(arduSolo, listener))
            return;

        if (android.text.TextUtils.isEmpty(wifiSsid) && android.text.TextUtils.isEmpty(wifiPassword))
            return;

        final SoloComp soloComp = arduSolo.getSoloComp();
        soloComp.updateWifiSettings(wifiSsid, wifiPassword, listener);
    }

    public static void updateSoloLinkButtonSettings(ArduSolo arduSolo,
                                                    SoloButtonSettingSetter buttonSettings,
                                                    ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(arduSolo, listener) || buttonSettings == null)
            return;

        final SoloComp soloComp = arduSolo.getSoloComp();
        soloComp.pushButtonSettings(buttonSettings, listener);
    }

    public static void updateSoloLinkControllerMode(ArduSolo arduSolo,
                                                    @SoloControllerMode.ControllerMode int mode,
                                                    ICommandListener listener) {
        if (!isSoloLinkFeatureAvailable(arduSolo, listener))
            return;

        final SoloComp soloComp = arduSolo.getSoloComp();
        soloComp.updateControllerMode(mode, listener);
    }

    public static void updateSoloControllerUnit(ArduSolo arduSolo, @SoloControllerUnits.ControllerUnit String unit, ICommandListener listener){
        if(!isSoloLinkFeatureAvailable(arduSolo, listener))
            return;

        final SoloComp soloComp = arduSolo.getSoloComp();
        soloComp.updateControllerUnit(unit, listener);
    }

    public static void updateSoloLinkEUTxPowerCompliance(ArduSolo arduSolo, boolean isCompliant, ICommandListener listener){
        if(!isSoloLinkFeatureAvailable(arduSolo, listener))
            return;

        final SoloComp soloComp = arduSolo.getSoloComp();
        soloComp.updateEUTxPowerCompliance(isCompliant, listener);
    }

}
