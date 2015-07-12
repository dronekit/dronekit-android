package org.droidplanner.services.android.drone.companion.solo;

import android.content.Context;
import android.os.Handler;
import android.support.v4.util.Pair;
import android.util.SparseArray;

import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import org.droidplanner.services.android.drone.companion.CompComp;
import org.droidplanner.services.android.drone.companion.solo.artoo.ArtooLinkListener;
import org.droidplanner.services.android.drone.companion.solo.artoo.ArtooLinkManager;
import org.droidplanner.services.android.drone.companion.solo.sololink.SoloLinkListener;
import org.droidplanner.services.android.drone.companion.solo.sololink.SoloLinkManager;
import org.droidplanner.services.android.utils.NetworkUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * Sololink companion computer implementation
 * Created by Fredia Huya-Kouadio on 7/9/15.
 */
public class SoloComp implements CompComp, SoloLinkListener, ArtooLinkListener {

    public interface SoloCompListener {
        void onConnected();

        void onDisconnected();

        void onTlvPacketReceived(TLVPacket packet);

        void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings);

        void onWifiInfoUpdated(String wifiName, String wifiPassword);

        void onButtonPacketReceived(ButtonPacket packet);
    }

    public static final String SOLO_LINK_WIFI_PREFIX = "SoloLink_";

    public static final String SSH_USERNAME = "root";
    public static final String SSH_PASSWORD = "TjSDBkAu";

    private final ArtooLinkManager artooMgr;
    private final SoloLinkManager soloLinkMgr;

    private final Context context;
    private final ExecutorService asyncExecutor;

    private SoloCompListener listener;

    /**
     * Solo companion computer implementation
     *
     * @param context Application context
     */
    public SoloComp(Context context, Handler handler) {
        this.context = context;

        asyncExecutor = Executors.newCachedThreadPool();

        this.artooMgr = new ArtooLinkManager(context, handler, asyncExecutor);
        this.soloLinkMgr = new SoloLinkManager(context, handler, asyncExecutor);
    }

    public void setListener(SoloCompListener listener) {
        this.listener = listener;
    }

    public boolean isAvailable() {
        //TODO: complement the logic.
        return NetworkUtils.isOnSololinkNetwork(this.context);
    }

    public void start() {
        if (!isAvailable()) {
            return;
        }

        artooMgr.start(this);
        soloLinkMgr.start(this);
    }

    public void stop() {
        artooMgr.stop();
        soloLinkMgr.stop();
    }

    /**
     * Terminates and releases resources used by this companion computer instance. The instance should no longer be used after calling this method.
     */
    public void destroy() {
        stop();
        asyncExecutor.shutdownNow();
    }

    @Override
    public void onTlvPacketReceived(TLVPacket packet) {
        if (listener != null)
            listener.onTlvPacketReceived(packet);
    }

    @Override
    public void onWifiInfoUpdated(String wifiName, String wifiPassword) {
        if (listener != null)
            listener.onWifiInfoUpdated(wifiName, wifiPassword);
    }

    @Override
    public void onButtonPacketReceived(ButtonPacket packet) {
        if (listener != null)
            listener.onButtonPacketReceived(packet);
    }

    @Override
    public void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings) {
        if (listener != null)
            listener.onPresetButtonLoaded(buttonType, buttonSettings);
    }

    @Override
    public void onLinkConnected() {
        tryStartingVideo();
        if (isConnected()) {
            if (listener != null)
                listener.onConnected();
        } else {
            if (!artooMgr.isLinkConnected())
                artooMgr.start(this);

            if (!soloLinkMgr.isLinkConnected())
                soloLinkMgr.start(this);
        }
    }

    @Override
    public void onLinkDisconnected() {
        if (listener != null)
            listener.onDisconnected();

        artooMgr.stopVideoManager();
        soloLinkMgr.stop();
    }

    private void tryStartingVideo() {
        if (isConnected()) {
            artooMgr.startVideoManager();
        }
    }

    public boolean isConnected() {
        return artooMgr.isLinkConnected() && soloLinkMgr.isLinkConnected();
    }

    public Pair<String, String> getWifiSettings() {
        return artooMgr.getSoloLinkWifiInfo();
    }

    public String getControllerVersion() {
        return artooMgr.getArtooVersion();
    }

    public String getControllerFirmwareVersion() {
        return artooMgr.getStm32Version();
    }

    public String getVehicleVersion() {
        return soloLinkMgr.getVehicleVersion();
    }

    public String getAutopilotVersion() {
        return soloLinkMgr.getPixhawkVersion();
    }

    public SoloButtonSetting getButtonSetting(int buttonType) {
        return soloLinkMgr.getLoadedPresetButton(buttonType);
    }

    public SparseArray<SoloButtonSetting> getButtonSettings(){
        final SparseArray<SoloButtonSetting> buttonSettings = new SparseArray<>(2);
        buttonSettings.append(ButtonTypes.BUTTON_A, soloLinkMgr.getLoadedPresetButton(ButtonTypes.BUTTON_A));
        buttonSettings.append(ButtonTypes.BUTTON_B, soloLinkMgr.getLoadedPresetButton(ButtonTypes.BUTTON_B));
        return buttonSettings;
    }

    public void sendSoloLinkMessage(TLVPacket message){
        soloLinkMgr.sendTLVPacket(message);
    }

    public void updateWifiSettings(final String wifiSsid, final String wifiPassword){
        if(asyncExecutor != null && !asyncExecutor.isShutdown()){
            asyncExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if(soloLinkMgr.updateSololinkWifi(wifiSsid, wifiPassword)
                            && artooMgr.updateSololinkWifi(wifiSsid, wifiPassword)){
                        Timber.d("Sololink wifi update successful.");
                    }
                    else{
                        Timber.d("Sololink wifi update failed.");
                    }
                }
            });
        }
    }

    public void pushButtonSettings(SoloButtonSettingSetter buttonSettings){
        soloLinkMgr.pushPresetButtonSettings(buttonSettings);
    }

}
