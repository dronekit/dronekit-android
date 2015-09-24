package org.droidplanner.services.android.core.drone.companion.solo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Surface;

import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.SoloEventExtras;
import com.o3dr.services.android.lib.drone.companion.solo.SoloEvents;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonTypes;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproState;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloMessageLocation;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.core.drone.companion.CompComp;
import org.droidplanner.services.android.core.drone.companion.solo.controller.ControllerLinkListener;
import org.droidplanner.services.android.core.drone.companion.solo.controller.ControllerLinkManager;
import org.droidplanner.services.android.core.drone.companion.solo.sololink.SoloLinkListener;
import org.droidplanner.services.android.core.drone.companion.solo.sololink.SoloLinkManager;
import org.droidplanner.services.android.utils.NetworkUtils;
import org.droidplanner.services.android.utils.video.DecoderListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Sololink companion computer implementation
 * Created by Fredia Huya-Kouadio on 7/9/15.
 */
public class SoloComp implements CompComp, SoloLinkListener, ControllerLinkListener {

    public interface SoloCompListener {
        void onConnected();

        void onDisconnected();

        void onTlvPacketReceived(TLVPacket packet);

        void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings);

        void onWifiInfoUpdated(String wifiName, String wifiPassword);

        void onButtonPacketReceived(ButtonPacket packet);

        void onEUTxPowerComplianceUpdated(boolean isCompliant);

        void onVersionsUpdated();

        void onControllerEvent(String event, Bundle eventInfo);
    }

    private static final String NO_VIDEO_OWNER = "no_video_owner";

    public static final String SOLO_LINK_WIFI_PREFIX = "SoloLink_";

    public static final String SSH_USERNAME = "root";
    public static final String SSH_PASSWORD = "TjSDBkAu";

    private final ControllerLinkManager controllerLinkManager;
    private final SoloLinkManager soloLinkMgr;

    private final Context context;
    private final Handler handler;
    private final ExecutorService asyncExecutor;

    private final AtomicReference<String> videoOwnerId = new AtomicReference<>(NO_VIDEO_OWNER);
    private final AtomicReference<String> videoTagRef = new AtomicReference<>("");

    private SoloCompListener compListener;

    private SoloGoproState goproState;

    /**
     * Solo companion computer implementation
     *
     * @param context Application context
     */
    public SoloComp(Context context, Handler handler) {
        this.context = context;

        this.handler = handler;
        asyncExecutor = Executors.newCachedThreadPool();

        this.controllerLinkManager = new ControllerLinkManager(context, handler, asyncExecutor);
        this.soloLinkMgr = new SoloLinkManager(context, handler, asyncExecutor);
    }

    public SoloGoproState getGoproState() {
        return goproState;
    }

    public void setListener(SoloCompListener listener) {
        this.compListener = listener;
    }

    public boolean isAvailable() {
        //TODO: complement the logic.
        return NetworkUtils.isOnSololinkNetwork(this.context);
    }

    public void start() {
        if (!isAvailable()) {
            return;
        }

        resetVideoOwner();

        controllerLinkManager.start(this);
        soloLinkMgr.start(this);
    }

    public void stop() {
        resetVideoOwner();
        controllerLinkManager.stop();
        soloLinkMgr.stop();
    }

    public void refreshState() {
        soloLinkMgr.refreshState();
        controllerLinkManager.refreshState();
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
        if(packet == null)
            return;

        switch(packet.getMessageType()){
            case TLVMessageTypes.TYPE_SOLO_GOPRO_STATE:
                goproState = (SoloGoproState) packet;
                Timber.d("Updated gopro state.");
                break;
        }

        if (compListener != null)
            compListener.onTlvPacketReceived(packet);
    }

    @Override
    public void onWifiInfoUpdated(String wifiName, String wifiPassword) {
        if (compListener != null)
            compListener.onWifiInfoUpdated(wifiName, wifiPassword);
    }

    @Override
    public void onButtonPacketReceived(ButtonPacket packet) {
        if (compListener != null)
            compListener.onButtonPacketReceived(packet);
    }

    @Override
    public void onEUTxPowerComplianceUpdated(boolean isCompliant) {
        if(compListener != null)
            compListener.onEUTxPowerComplianceUpdated(isCompliant);
    }

    @Override
    public void onControllerModeUpdated() {
        if (compListener != null){
            final Bundle eventInfo = new Bundle();
            eventInfo.putInt(SoloEventExtras.EXTRA_SOLO_CONTROLLER_MODE, getControllerMode());
            compListener.onControllerEvent(SoloEvents.SOLO_CONTROLLER_MODE_UPDATED, eventInfo);
        }
    }

    @Override
    public void onControllerUnitUpdated(String trimmedResponse) {
        if(compListener != null){
            final Bundle eventInfo = new Bundle();
            eventInfo.putString(SoloEventExtras.EXTRA_SOLO_CONTROLLER_UNIT, trimmedResponse);
            compListener.onControllerEvent(SoloEvents.SOLO_CONTROLLER_UNIT_UPDATED, eventInfo);
        }
    }

    @Override
    public void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings) {
        if (compListener != null)
            compListener.onPresetButtonLoaded(buttonType, buttonSettings);
    }

    @Override
    public void onLinkConnected() {
        if (isConnected()) {
            if (compListener != null)
                compListener.onConnected();
        } else {
            if (!controllerLinkManager.isLinkConnected())
                controllerLinkManager.start(this);

            if (!soloLinkMgr.isLinkConnected())
                soloLinkMgr.start(this);
        }
    }

    @Override
    public void onLinkDisconnected() {
        if (compListener != null)
            compListener.onDisconnected();

        controllerLinkManager.stop();
        soloLinkMgr.stop();
    }

    @Override
    public void onVersionsUpdated() {
        if(compListener != null)
            compListener.onVersionsUpdated();
    }

    @Override
    public void onMacAddressUpdated() {
        final String soloMacAddress = soloLinkMgr.getMacAddress();
        final String artooMacAddress = controllerLinkManager.getMacAddress();
        if(!TextUtils.isEmpty(soloMacAddress) && !TextUtils.isEmpty(artooMacAddress) && compListener != null){
            compListener.onControllerEvent(AttributeEvent.STATE_VEHICLE_UID, null);
        }
    }

    public boolean isConnected() {
        return controllerLinkManager.isLinkConnected() && soloLinkMgr.isLinkConnected();
    }

    public Pair<String, String> getWifiSettings() {
        return controllerLinkManager.getSoloLinkWifiInfo();
    }

    public boolean isEUTxPowerCompliant() {
        return controllerLinkManager.isEUTxPowerCompliant();
    }

    public void refreshSoloVersions(){
        soloLinkMgr.refreshSoloLinkVersions();
        controllerLinkManager.refreshControllerVersions();
    }

    public String getControllerVersion() {
        return controllerLinkManager.getArtooVersion();
    }

    public String getControllerFirmwareVersion() {
        return controllerLinkManager.getStm32Version();
    }

    public String getVehicleVersion() {
        return soloLinkMgr.getVehicleVersion();
    }

    @SoloControllerMode.ControllerMode
    public int getControllerMode(){
        return  controllerLinkManager.getControllerMode();
    }

    @SoloControllerUnits.ControllerUnit
    public String getControllerUnit(){
        return controllerLinkManager.getControllerUnit();
    }

    public String getSoloMacAddress(){
        return soloLinkMgr.getMacAddress();
    }

    public String getControllerMacAddress(){
        return controllerLinkManager.getMacAddress();
    }

    public String getAutopilotVersion() {
        return soloLinkMgr.getPixhawkVersion();
    }

    public String getGimbalVersion(){
        return soloLinkMgr.getGimbalVersion();
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

    public void sendSoloLinkMessage(TLVPacket message, ICommandListener listener){
        soloLinkMgr.sendTLVPacket(message, listener);
    }

    public void updateWifiSettings(final String wifiSsid, final String wifiPassword,
                                   final ICommandListener listener){
            postAsyncTask(new Runnable() {
                @Override
                public void run() {
                    if (soloLinkMgr.updateSololinkWifi(wifiSsid, wifiPassword)
                            && controllerLinkManager.updateSololinkWifi(wifiSsid, wifiPassword)) {
                        Timber.d("Sololink wifi update successful.");

                        if (listener != null) {
                            postSuccessEvent(listener);
                        }
                    } else {
                        Timber.d("Sololink wifi update failed.");
                        if (listener != null) {
                            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                        }
                    }
                }
            });
    }

    public void pushButtonSettings(SoloButtonSettingSetter buttonSettings, ICommandListener listener){
        soloLinkMgr.pushPresetButtonSettings(buttonSettings, listener);
    }

    public void updateControllerMode(@SoloControllerMode.ControllerMode final int selectedMode, ICommandListener listener){
        controllerLinkManager.updateControllerMode(selectedMode, listener);
    }

    public void updateControllerUnit(@SoloControllerUnits.ControllerUnit final String selectedUnit, ICommandListener listener){
        controllerLinkManager.updateControllerUnit(selectedUnit, listener);
    }

    public void updateEUTxPowerCompliance(boolean isCompliant, ICommandListener listener) {
        controllerLinkManager.setEUTxPowerCompliance(isCompliant, listener);
    }

    public void startVideoStream(String appId, String newVideoTag, Surface videoSurface, final ICommandListener listener){
        Timber.d("Video stream start request from %s. Video owner is %s.", appId, videoOwnerId.get());
        if(TextUtils.isEmpty(appId)){
            postErrorEvent(CommandExecutionError.COMMAND_DENIED, listener);
            return;
        }

        if(videoSurface == null){
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        if(newVideoTag == null)
            newVideoTag = "";

        if(appId.equals(videoOwnerId.get())){
            String currentVideoTag = videoTagRef.get();
            if(currentVideoTag == null)
                currentVideoTag = "";

            if(newVideoTag.equals(currentVideoTag)){
                postSuccessEvent(listener);
                return;
            }
        }

        if (videoOwnerId.compareAndSet(NO_VIDEO_OWNER, appId)){
            videoTagRef.set(newVideoTag);

            Timber.d("Setting video surface layer.");
            controllerLinkManager.startDecoding(videoSurface, new DecoderListener() {
                @Override
                public void onDecodingStarted() {
                    Timber.d("Video decoding started.");
                    postSuccessEvent(listener);
                }

                @Override
                public void onDecodingError() {
                    Timber.d("Video decoding failed.");
                    postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                    resetVideoOwner();
                }

                @Override
                public void onDecodingEnded() {
                    Timber.d("Video decoding ended successfully.");
                    resetVideoOwner();
                }
            });
        }
        else{
            postErrorEvent(CommandExecutionError.COMMAND_DENIED, listener);
        }
    }

    public void stopVideoStream(String appId, String currentVideoTag, final ICommandListener listener){
        Timber.d("Video stream stop request from %s. Video owner is %s.", appId, videoOwnerId.get());
        if(TextUtils.isEmpty(appId)){
            Timber.w("Owner id is empty.");
            postErrorEvent(CommandExecutionError.COMMAND_DENIED, listener);
            return;
        }

        final String currentVideoOwner = videoOwnerId.get();
        if(NO_VIDEO_OWNER.equals(currentVideoOwner)){
            Timber.d("No video owner set. Nothing to do.");
            postSuccessEvent(listener);
            return;
        }

        if(currentVideoTag == null)
            currentVideoTag = "";

        if(appId.equals(currentVideoOwner) && currentVideoTag.equals(videoTagRef.get())
                && videoOwnerId.compareAndSet(currentVideoOwner, NO_VIDEO_OWNER)){
            videoTagRef.set("");

            Timber.d("Stopping video decoding. Current owner is %s.", currentVideoOwner);
            controllerLinkManager.stopDecoding(new DecoderListener() {
                @Override
                public void onDecodingStarted() {
                }

                @Override
                public void onDecodingError() {
                    postSuccessEvent(listener);
                }

                @Override
                public void onDecodingEnded() {
                    postSuccessEvent(listener);
                }
            });

        }
        else{
            postErrorEvent(CommandExecutionError.COMMAND_DENIED, listener);
        }
    }

    public void tryStoppingVideoStream(String parentId){
        if(TextUtils.isEmpty(parentId))
            return;

        final String videoOwner = videoOwnerId.get();
        if(NO_VIDEO_OWNER.equals(videoOwner))
            return;

        if(videoOwner.equals(parentId)){
            Timber.d("Stopping video owned by %s", parentId);
            stopVideoStream(parentId, videoTagRef.get(), null);
        }
    }

    private void resetVideoOwner(){
        Timber.d("Resetting video tag (%s) and owner id (%s)", videoTagRef.get(), videoOwnerId.get());
        videoTagRef.set("");
        videoOwnerId.set(NO_VIDEO_OWNER);
        controllerLinkManager.stopDecoding(null);
    }

    protected void postAsyncTask(Runnable task){
        if(asyncExecutor != null && !asyncExecutor.isShutdown()){
            asyncExecutor.execute(task);
        }
    }

    protected void postSuccessEvent(final ICommandListener listener){
        if(handler != null && listener != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onSuccess();
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    protected void postTimeoutEvent(final ICommandListener listener){
        if(handler != null && listener != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onTimeout();
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    protected void postErrorEvent(final int error, final ICommandListener listener){
        if(handler != null && listener != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onError(error);
                    } catch (RemoteException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        }
    }

    public void enableFollowDataConnection(){
        soloLinkMgr.enableFollowDataConnection();
    }

    public void disableFollowDataConnection(){
        soloLinkMgr.disableFollowDataConnection();
    }

    public void updateFollowCenter(SoloMessageLocation location){
        soloLinkMgr.sendTLVPacket(location, true, null);
    }

}
