package org.droidplanner.services.android.core.drone.companion.solo.artoo;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Surface;

import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageParser;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.core.drone.companion.solo.AbstractLinkManager;
import org.droidplanner.services.android.core.drone.companion.solo.SoloComp;
import org.droidplanner.services.android.utils.NetworkUtils;
import org.droidplanner.services.android.utils.connection.IpConnectionListener;
import org.droidplanner.services.android.utils.connection.SshConnection;
import org.droidplanner.services.android.utils.connection.TcpConnection;
import org.droidplanner.services.android.utils.video.DecoderListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Handles artoo link related logic.
 */
public class ArtooLinkManager extends AbstractLinkManager<ArtooLinkListener> {

    private static final long RECONNECT_COUNTDOWN = 1000l; //ms

    public static final String SOLOLINK_SSID_CONFIG_PATH = "/usr/bin/sololink_config";

    private static final String ARTOO_VERSION_FILENAME = "/VERSION";
    private static final String STM32_VERSION_FILENAME = "/STM_VERSION";

    /**
     * Artoo link ip address
     */
    public static final String ARTOO_IP = "10.1.1.1";

    private static final int ARTOO_VIDEO_HANDSHAKE_PORT = 5502;
    private static final int ARTOO_BUTTON_PORT = 5016;
    private static final int ARTOO_BATTERY_PORT = 5021;

    public static final int ARTOO_UDP_PORT = 5600;

    private final AtomicReference<String> controllerVersion = new AtomicReference<>("");
    private final AtomicReference<String> stm32Version = new AtomicReference<>("");
    private final AtomicBoolean isEUTxPowerCompliant = new AtomicBoolean(false);

    private final AtomicReference<Pair<String, String>> sololinkWifiInfo = new AtomicReference<>(Pair.create("", ""));

    private final Runnable startVideoMgr = new Runnable() {
        @Override
        public void run() {
            videoMgr.start(null);
        }
    };

    private final Runnable reconnectBatteryTask = new Runnable() {
        @Override
        public void run() {
            batteryConnection.connect();
        }
    };

    private final Runnable reconnectVideoHandshake = new Runnable() {
        @Override
        public void run() {
            videoHandshake.connect();
        }
    };


    private final VideoManager videoMgr;

    private final AtomicBoolean isVideoHandshakeStarted = new AtomicBoolean(false);
    private final AtomicBoolean isBatteryStarted = new AtomicBoolean(false);

    private final TcpConnection videoHandshake;
    private final TcpConnection batteryConnection;

    private static final SshConnection sshLink = new SshConnection(ARTOO_IP, SoloComp.SSH_USERNAME,
            SoloComp.SSH_PASSWORD);

    private final Runnable artooVersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(ARTOO_VERSION_FILENAME);
            if (version != null)
                controllerVersion.set(version);
        }
    };

    private final Runnable stm32VersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(STM32_VERSION_FILENAME);
            if (version != null)
                stm32Version.set(version);
        }
    };

    private final Runnable loadWifiInfo = new Runnable() {
        @Override
        public void run() {
            try {
                String wifiName = sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --get-wifi-ssid");
                String wifiPassword = sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --get-wifi-password");

                if (!TextUtils.isEmpty(wifiName) && !TextUtils.isEmpty(wifiPassword)) {
                    Pair<String, String> wifiInfo = Pair.create(wifiName.trim(), wifiPassword.trim());
                    sololinkWifiInfo.set(wifiInfo);

                    if (linkListener != null)
                        linkListener.onWifiInfoUpdated(wifiInfo.first, wifiInfo.second);
                }

            } catch (IOException e) {
                Timber.e(e, "Unable to retrieve sololink wifi info.");
            }
        }
    };

    private final Runnable checkEUTxPowerCompliance = new Runnable() {
        @Override
        public void run() {
            boolean isCompliant;
            try {
                isCompliant = !sshLink.execute("sololink_config --get-wifi-country").trim().equals("US");
                if(linkListener != null)
                    linkListener.onEUTxPowerComplianceUpdated(isCompliant);
            } catch (IOException e) {
                Timber.e(e, "Error occurred while querying wifi country.");
                isCompliant = false; //because most users will have it disabled by default
            }

            isEUTxPowerCompliant.set(isCompliant);
        }
    };

    private ArtooLinkListener linkListener;

    public ArtooLinkManager(Context context, final Handler handler, ExecutorService asyncExecutor) {
        super(context, new TcpConnection(handler, ARTOO_IP, ARTOO_BUTTON_PORT), handler, asyncExecutor);

        this.videoMgr = new VideoManager(context, handler, asyncExecutor);

        videoHandshake = new TcpConnection(handler, ARTOO_IP, ARTOO_VIDEO_HANDSHAKE_PORT);
        videoHandshake.setIpConnectionListener(new IpConnectionListener() {

            private int disconnectTracker = 0;

            @Override
            public void onIpConnected() {
                disconnectTracker = 0;
                handler.removeCallbacks(reconnectVideoHandshake);

                Timber.d("Artoo link connected. Starting video stream...");
                //Connect the udp stream
                handler.post(startVideoMgr);
            }

            @Override
            public void onIpDisconnected() {
                if (isVideoHandshakeStarted.get())
                    handler.postDelayed(reconnectVideoHandshake, ++disconnectTracker * RECONNECT_COUNTDOWN);
            }

            @Override
            public void onPacketReceived(ByteBuffer packetBuffer) {
            }
        });

        batteryConnection = new TcpConnection(handler, ARTOO_IP, ARTOO_BATTERY_PORT);
        batteryConnection.setIpConnectionListener(new IpConnectionListener() {

            private int disconnectTracker = 0;

            @Override
            public void onIpConnected() {
                disconnectTracker = 0;
                handler.removeCallbacks(reconnectBatteryTask);
            }

            @Override
            public void onIpDisconnected() {
                //Try to connect
                if (isBatteryStarted.get()) {
                    handler.postDelayed(reconnectBatteryTask, ++disconnectTracker * RECONNECT_COUNTDOWN);

                }
            }

            @Override
            public void onPacketReceived(ByteBuffer packetBuffer) {

                TLVPacket tlvMsg = TLVMessageParser.parseTLVPacket(packetBuffer);
                if (tlvMsg == null)
                    return;

                final int messageType = tlvMsg.getMessageType();
                Timber.d("Received tlv message: " + messageType);

                if (linkListener != null)
                    linkListener.onTlvPacketReceived(tlvMsg);
            }
        });

    }

    /**
     * @return the controller version.
     */
    public String getArtooVersion() {
        return controllerVersion.get();
    }

    /**
     * @return the stm32 version
     */
    public String getStm32Version() {
        return stm32Version.get();
    }

    /**
     * @return true if the controller is compliant to the EX tx power levels.
     */
    public boolean isEUTxPowerCompliant(){
        return isEUTxPowerCompliant.get();
    }

    public void startVideoManager() {
        handler.removeCallbacks(reconnectVideoHandshake);
        isVideoHandshakeStarted.set(true);
        videoHandshake.connect();
    }

    public void stopVideoManager() {
        handler.removeCallbacks(startVideoMgr);
        this.videoMgr.stop();

        handler.removeCallbacks(reconnectVideoHandshake);
        isVideoHandshakeStarted.set(false);
        videoHandshake.disconnect();
    }

    public static SshConnection getSshLink() {
        return sshLink;
    }

    private void loadSololinkWifiInfo() {
        postAsyncTask(loadWifiInfo);
    }

    public boolean updateSololinkWifi(CharSequence wifiSsid, CharSequence password) {
        Timber.d(String.format(Locale.US, "Updating artoo wifi ssid to %s with password %s", wifiSsid, password));
        try {
            String ssidUpdateResult = sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --set-wifi-ssid " + wifiSsid);
            String passwordUpdateResult = sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --set-wifi-password " +
                    password);
            restartHostapdService();
            return true;
        } catch (IOException e) {
            Timber.e(e, "Error occurred while updating the sololink wifi ssid.");
            return false;
        }
    }

    public Pair<String, String> getSoloLinkWifiInfo() {
        return sololinkWifiInfo.get();
    }

    @Override
    public void start(ArtooLinkListener listener) {
        this.linkListener = listener;

        Timber.d("Starting artoo link manager");
        super.start(listener);

        handler.removeCallbacks(reconnectBatteryTask);
        //TODO: Connect to battery when available
        //isBatteryStarted.set(true);
        //batteryConnection.connect();

    }

    public void stop() {
        Timber.d("Stopping artoo link manager");

        stopVideoManager();

        handler.removeCallbacks(reconnectBatteryTask);
        isBatteryStarted.set(false);
        batteryConnection.disconnect();

        super.stop();
    }

    @Override
    public boolean isLinkConnected() {
        return NetworkUtils.isOnSololinkNetwork(context);
    }

    @Override
    public void onIpConnected() {
        super.onIpConnected();

        Timber.d("Artoo link connected.");

        //Update sololink wifi
        loadSololinkWifiInfo();

        //Refresh the vehicle's components versions
        updateArtooVersion();
        updateStm32Version();

        //Update the tx power compliance
        loadCurrentEUTxPowerComplianceMode();

    }

    @Override
    public void onIpDisconnected() {
        Timber.d("Artoo link disconnected.");

        handler.removeCallbacks(startVideoMgr);
        this.videoMgr.stop();

        handler.removeCallbacks(reconnectVideoHandshake);
        isVideoHandshakeStarted.set(false);
        videoHandshake.disconnect();

        super.onIpDisconnected();
    }

    @Override
    public void onPacketReceived(ByteBuffer packetBuffer) {
        ButtonPacket buttonPacket = ButtonPacket.parseButtonPacket(packetBuffer);
        if (buttonPacket == null)
            return;

        final int buttonId = buttonPacket.getButtonId();
        Timber.d("Button pressed: " + buttonId);

        if (linkListener != null)
            linkListener.onButtonPacketReceived(buttonPacket);
    }

    public void startDecoding(Surface surface, DecoderListener listener) {
        videoMgr.startDecoding(surface, listener);
    }

    public void stopDecoding(DecoderListener listener) {
        videoMgr.stopDecoding(listener);
    }

    private void updateArtooVersion() {
        postAsyncTask(artooVersionRetriever);
    }

    private void updateStm32Version() {
        postAsyncTask(stm32VersionRetriever);
    }

    private String retrieveVersion(String versionFile) {
        try {
            String version = sshLink.execute("cat " + versionFile);
            if (TextUtils.isEmpty(version)) {
                Timber.d("No version file was found");
                return "";
            } else {
                return version.split("\n")[0];
            }
        } catch (IOException e) {
            Timber.e("Unable to retrieve the current version.", e);
        }

        return null;
    }

    public void updateArtooMode(@SoloControllerMode.ControllerMode final int mode, final ICommandListener listener) {
        postAsyncTask(new Runnable() {
            @Override
            public void run() {
                Timber.d("Switching controller to mode %n", mode);
                try {
                    final String response;
                    switch (mode) {
                        case SoloControllerMode.MODE_1:
                            response = sshLink.execute("runStickMapperMode1.sh");
                            postSuccessEvent(listener);
                            break;

                        case SoloControllerMode.MODE_2:
                            response = sshLink.execute("runStickMapperMode2.sh");
                            postSuccessEvent(listener);
                            break;

                        default:
                            response = "No response.";
                            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                            break;
                    }
                    Timber.d("Response from switch mode command was: %s", response);
                } catch (IOException e) {
                    Timber.e(e, "Error occurred while changing artoo modes.");
                    postTimeoutEvent(listener);
                }

            }
        });

    }

    public void setEUTxPowerCompliance(final boolean compliant, final ICommandListener listener){
        postAsyncTask(new Runnable() {
                @Override
                public void run() {
                    Timber.d("%s EU Tx power compliance mode", compliant?"Enabling":"Disabling");
                    try {
                        final boolean currentCompliance = !sshLink.execute("sololink_config --get-wifi-country").trim().equals("US");
                        if(currentCompliance != compliant) {
                            final String response;
                            if (compliant) {
                                response = sshLink.execute("sololink_config --set-wifi-country FR; echo $?");

                            } else {
                                response = sshLink.execute("sololink_config --set-wifi-country US; echo $?");
                            }
                            if (response.trim().equals("0")) {
                                restartHostapdService();
                                Timber.d("wifi country successfully set, rebooting artoo");

                                isEUTxPowerCompliant.set(compliant);
                                postSuccessEvent(listener);

                            } else {
                                Timber.d("wifi country set failed: %s", response);
                                postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                            }
                        }
                    } catch (IOException e) {
                        Timber.e(e, "Error occurred while changing wifi country.");
                        postTimeoutEvent(listener);
                    }
                }
            });
    }

    private void loadCurrentEUTxPowerComplianceMode(){
        postAsyncTask(checkEUTxPowerCompliance);
    }

    private void restartHostapdService(){
        try {
            sshLink.execute("/etc/init.d/hostapd restart");
        } catch (IOException e) {
            Timber.e(e, "Error occurred while restarting hostpad service on Artoo.");
        }
    }
}
