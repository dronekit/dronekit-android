package org.droidplanner.services.android.drone.companion.solo;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Surface;

import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.drone.companion.CompComp;
import org.droidplanner.services.android.drone.companion.solo.artoo.ArtooLinkListener;
import org.droidplanner.services.android.drone.companion.solo.artoo.ArtooLinkManager;
import org.droidplanner.services.android.drone.companion.solo.sololink.SoloLinkListener;
import org.droidplanner.services.android.drone.companion.solo.sololink.SoloLinkManager;
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
public class SoloComp implements CompComp, SoloLinkListener, ArtooLinkListener {

    public interface SoloCompListener {
        void onConnected();

        void onDisconnected();

        void onTlvPacketReceived(TLVPacket packet);

        void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings);

        void onWifiInfoUpdated(String wifiName, String wifiPassword);

        void onButtonPacketReceived(ButtonPacket packet);
    }

    private static final String NO_VIDEO_OWNER = "no_video_owner";

    public static final String SOLO_LINK_WIFI_PREFIX = "SoloLink_";

    public static final String SSH_USERNAME = "root";
    public static final String SSH_PASSWORD = "TjSDBkAu";

    private final ArtooLinkManager artooMgr;
    private final SoloLinkManager soloLinkMgr;

    private final Context context;
    private final Handler handler;
    private final ExecutorService asyncExecutor;

    private final AtomicReference<String> videoOwnerId = new AtomicReference<>(NO_VIDEO_OWNER);

    private SoloCompListener compListener;

    /**
     * Solo companion computer implementation
     *
     * @param context Application context
     */
    public SoloComp(Context context, Handler handler) {
        this.context = context;

        this.handler = handler;
        asyncExecutor = Executors.newCachedThreadPool();

        this.artooMgr = new ArtooLinkManager(context, handler, asyncExecutor);
        this.soloLinkMgr = new SoloLinkManager(context, handler, asyncExecutor);
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
    public void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings) {
        if (compListener != null)
            compListener.onPresetButtonLoaded(buttonType, buttonSettings);
    }

    @Override
    public void onLinkConnected() {
        tryStartingVideo();
        if (isConnected()) {
            if (compListener != null)
                compListener.onConnected();
        } else {
            if (!artooMgr.isLinkConnected())
                artooMgr.start(this);

            if (!soloLinkMgr.isLinkConnected())
                soloLinkMgr.start(this);
        }
    }

    @Override
    public void onLinkDisconnected() {
        if (compListener != null)
            compListener.onDisconnected();

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

    public void sendSoloLinkMessage(TLVPacket message, ICommandListener listener){
        soloLinkMgr.sendTLVPacket(message, listener);
    }

    public void updateWifiSettings(final String wifiSsid, final String wifiPassword,
                                   final ICommandListener listener){
        if(asyncExecutor != null && !asyncExecutor.isShutdown()){
            postAsyncTask(new Runnable() {
                @Override
                public void run() {
                    if(soloLinkMgr.updateSololinkWifi(wifiSsid, wifiPassword)
                            && artooMgr.updateSololinkWifi(wifiSsid, wifiPassword)){
                        Timber.d("Sololink wifi update successful.");

                        if(listener != null) {
                            postSuccessEvent(listener);
                        }
                    }
                    else{
                        Timber.d("Sololink wifi update failed.");
                        if(listener != null){
                            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                        }
                    }
                }
            });
        }
    }

    public void pushButtonSettings(SoloButtonSettingSetter buttonSettings, ICommandListener listener){
        soloLinkMgr.pushPresetButtonSettings(buttonSettings, listener);
    }

    public void updateControllerMode(@SoloControllerMode.ControllerMode final int selectedMode, ICommandListener listener){
        artooMgr.updateArtooMode(selectedMode, listener);
    }

    public void startVideoStream(String ownerId, Surface videoSurface, final ICommandListener listener){
        Timber.d("Video stream start request from %s. Video owner is %s.", ownerId, videoOwnerId.get());
        if(TextUtils.isEmpty(ownerId)){
            postErrorEvent(CommandExecutionError.COMMAND_DENIED, listener);
            return;
        }

        if(videoSurface == null){
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        if(ownerId.equals(videoOwnerId.get())){
            postSuccessEvent(listener);
            return;
        }

        if(videoOwnerId.compareAndSet(NO_VIDEO_OWNER, ownerId)){
            Timber.d("Setting video surface layer.");
            artooMgr.startDecoding(videoSurface, new DecoderListener() {
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

    public void stopVideoStream(String ownerId, final ICommandListener listener){
        Timber.d("Video stream stop request from %s. Video owner is %s.", ownerId, videoOwnerId.get());
        if(TextUtils.isEmpty(ownerId)){
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

        if(ownerId.equals(currentVideoOwner) && videoOwnerId.compareAndSet(currentVideoOwner, NO_VIDEO_OWNER)){
            Timber.d("Stopping video decoding. Current owner is %s.", videoOwnerId.get());
            artooMgr.stopDecoding(new DecoderListener() {
                @Override
                public void onDecodingStarted() {}

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

    private void resetVideoOwner(){
        Timber.d("Resetting video owner from %s", videoOwnerId.get());
        videoOwnerId.set(NO_VIDEO_OWNER);
        artooMgr.stopDecoding(null);
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

}
