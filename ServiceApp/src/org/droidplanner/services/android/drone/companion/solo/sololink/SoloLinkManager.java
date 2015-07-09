package org.droidplanner.services.android.drone.companion.solo.sololink;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import org.droidplanner.services.android.drone.companion.solo.AbstractLinkManager;
import org.droidplanner.services.android.drone.companion.solo.SoloComp;
import org.droidplanner.services.android.drone.companion.solo.artoo.ArtooLinkManager;
import org.droidplanner.services.android.drone.companion.solo.artoo.button.ButtonTypes;
import org.droidplanner.services.android.drone.companion.solo.sololink.tlv.SoloButtonSetting;
import org.droidplanner.services.android.drone.companion.solo.sololink.tlv.SoloButtonSettingGetter;
import org.droidplanner.services.android.drone.companion.solo.sololink.tlv.SoloButtonSettingSetter;
import org.droidplanner.services.android.drone.companion.solo.sololink.tlv.SoloMessageShotManagerError;
import org.droidplanner.services.android.drone.companion.solo.sololink.tlv.TLVMessageParser;
import org.droidplanner.services.android.drone.companion.solo.sololink.tlv.TLVMessageTypes;
import org.droidplanner.services.android.drone.companion.solo.sololink.tlv.TLVPacket;
import org.droidplanner.services.android.utils.Utils;
import org.droidplanner.services.android.utils.connection.SshConnection;
import org.droidplanner.services.android.utils.connection.TcpConnection;
import org.droidplanner.services.android.utils.connection.UdpConnection;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Handles solo link related logic.
 */
public class SoloLinkManager extends AbstractLinkManager {

    public static final String SOLO_LINK_DEFAULT_PASSWORD = "sololink";

    public static final String SOLO_LINK_IP = "10.1.1.10";
    public static final int SOLO_LINK_TCP_PORT = 5507;

    private static final int SHOT_FOLLOW_UDP_PORT = 14558;

    public static final String ACTION_PRESET_BUTTON_LOADED = Utils.PACKAGE_NAME + ".action" +
            ".PRESET_BUTTON_LOADED";
    public static final String EXTRA_PRESET_BUTTON_TYPE = "extra_preset_button_type";

    public static final String ACTION_SOLO_LINK_CONNECTED = Utils.PACKAGE_NAME + ".action.SOLO_LINK_CONNECTED";

    public static final String ACTION_SOLO_LINK_DISCONNECTED = Utils.PACKAGE_NAME + ".action.SOLO_LINK_DISCONNECTED";

    private static final String SOLO_VERSION_FILENAME = "/VERSION";
    private static final String PIXHAWK_VERSION_FILENAME = "/PIX_VERSION";

    private final LocalBroadcastManager lbm;

    private final UdpConnection followDataConn;

    private static final SshConnection sshLink = new SshConnection(getSoloLinkIp(), SoloComp.SSH_USERNAME, SoloComp.SSH_PASSWORD);

    private final SoloButtonSettingGetter presetButtonAGetter = new SoloButtonSettingGetter(ButtonTypes.BUTTON_A,
            ButtonTypes.BUTTON_EVENT_PRESS);

    private final SoloButtonSettingGetter presetButtonBGetter = new SoloButtonSettingGetter(ButtonTypes.BUTTON_B,
            ButtonTypes.BUTTON_EVENT_PRESS);

    private final AtomicReference<SoloButtonSetting> loadedPresetButtonA = new AtomicReference<>();
    private final AtomicReference<SoloButtonSetting> loadedPresetButtonB = new AtomicReference<>();

    private final AtomicReference<String> vehicleVersion = new AtomicReference<>("");
    private final AtomicReference<String> pixhawkVersion = new AtomicReference<>("");

    private final Runnable soloLinkVersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(SOLO_VERSION_FILENAME);
            if(version != null)
                vehicleVersion.set(version);
        }
    };

    private final Runnable pixhawkVersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(PIXHAWK_VERSION_FILENAME);
            if(version != null)
                pixhawkVersion.set(version);
        }
    };

    public SoloLinkManager(Context context, Handler handler, ExecutorService asyncExecutor) {
        super(context, new TcpConnection(getSoloLinkIp(), SOLO_LINK_TCP_PORT), handler, asyncExecutor);

        lbm = LocalBroadcastManager.getInstance(context);

        UdpConnection dataConn = null;
        try {
            dataConn = new UdpConnection(getSoloLinkIp(), SHOT_FOLLOW_UDP_PORT, 14557);
        } catch (UnknownHostException e) {
            Timber.e(e, "Error while creating follow udp connection.");
        }

        followDataConn = dataConn;

    }

    public static SshConnection getSshLink() {
        return sshLink;
    }

    public static String getSoloLinkIp() {
            return SOLO_LINK_IP;
    }

    @Override
    public void start(LinkListener listener) {
        Timber.d("Starting solo link manager");
        super.start(listener);
    }

    @Override
    public void stop() {
        Timber.d("Stopping solo link manager");
        super.stop();
    }

    @Override
    public void onIpConnected() {
        lbm.sendBroadcast(new Intent(ACTION_SOLO_LINK_CONNECTED));
        Timber.d("Connected to sololink.");
        super.onIpConnected();

        loadPresetButtonSettings();

        //Refresh the vehicle's components versions
        updateSoloLinkVersion();
        updatePixhawkVersion();
    }

    @Override
    public void onIpDisconnected() {
        lbm.sendBroadcast(new Intent(ACTION_SOLO_LINK_DISCONNECTED));
        Timber.d("Disconnected from sololink.");

        super.onIpDisconnected();
    }

    @Override
    public void onPacketReceived(ByteBuffer packetBuffer) {
        TLVPacket tlvMsg = TLVMessageParser.parseTLVPacket(packetBuffer);
        if (tlvMsg == null)
            return;

        final int messageType = tlvMsg.getMessageType();
        Timber.d("Received tlv message: " + messageType);

        //Have shot manager examine the received message first.
            switch (messageType) {
                case TLVMessageTypes.TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR:
                    Timber.w(((SoloMessageShotManagerError) tlvMsg).getExceptionInfo());
                    break;

                case TLVMessageTypes.TYPE_SOLO_GET_BUTTON_SETTING:
                    final SoloButtonSettingGetter receivedPresetButton = (SoloButtonSettingGetter) tlvMsg;
                    handleReceivedPresetButton(receivedPresetButton);
                    break;
            }

        lbm.sendBroadcast(new Intent(ACTION_TLV_PACKET_RECEIVED)
                .putExtra(EXTRA_TLV_PACKET_TYPE, messageType)
                .putExtra(EXTRA_TLV_PACKET_BYTES, tlvMsg.toBytes()));
    }

    private void sendPacket(byte[] payload, int payloadSize) {
        linkConn.sendPacket(payload, payloadSize);
    }

    private void sendFollowPacket(byte[] payload, int payloadSize) {
        if (followDataConn == null) {
            throw new IllegalStateException("Unable to send follow data.");
        }

        followDataConn.sendPacket(payload, payloadSize);
    }

    public void sendTLVPacket(TLVPacket packet) {
        sendTLVPacket(packet, false);
    }

    public void sendTLVPacket(TLVPacket packet, boolean useFollowLink) {
        if (packet == null)
            return;

        final byte[] messagePayload = packet.toBytes();
        if (useFollowLink) {
            sendFollowPacket(messagePayload, messagePayload.length);
        } else {
            sendPacket(messagePayload, messagePayload.length);
        }
    }

    public void loadPresetButtonSettings() {
        sendTLVPacket(presetButtonAGetter);
        sendTLVPacket(presetButtonBGetter);
    }

    private void handleReceivedPresetButton(SoloButtonSetting presetButton) {
        final int buttonType = presetButton.getButton();
        switch (buttonType) {
            case ButtonTypes.BUTTON_A:
                loadedPresetButtonA.set(presetButton);
                lbm.sendBroadcast(new Intent(ACTION_PRESET_BUTTON_LOADED)
                        .putExtra(EXTRA_PRESET_BUTTON_TYPE, buttonType));
                break;

            case ButtonTypes.BUTTON_B:
                loadedPresetButtonB.set(presetButton);
                lbm.sendBroadcast(new Intent(ACTION_PRESET_BUTTON_LOADED)
                        .putExtra(EXTRA_PRESET_BUTTON_TYPE, buttonType));
                break;
        }
    }

    public SoloButtonSetting getLoadedPresetButton(int buttonType) {
        switch (buttonType) {
            case ButtonTypes.BUTTON_A:
                return loadedPresetButtonA.get();

            case ButtonTypes.BUTTON_B:
                return loadedPresetButtonB.get();

            default:
                return null;
        }
    }

    /**
     * Update the vehicle preset button settings
     */
    public void pushPresetButtonSettings(SoloButtonSettingSetter buttonSetter) {
        if (!isLinkConnected() || buttonSetter == null)
            return;

        sendTLVPacket(buttonSetter);
        handleReceivedPresetButton(buttonSetter);
    }

    public void disableFollowDataConnection() {
        if (followDataConn != null)
            followDataConn.disconnect();
    }

    public void enableFollowDataConnection() {
        if (followDataConn != null)
            followDataConn.connect();
    }

    public boolean updateSololinkWifi(CharSequence wifiSsid, CharSequence password) {
        Timber.d(String.format(Locale.US, "Updating solo wifi ssid to %s with password %s", wifiSsid, password));
        try {
            String ssidUpdateResult = sshLink.execute(ArtooLinkManager.SOLOLINK_SSID_CONFIG_PATH + " --set-wifi-ssid " +
                    wifiSsid);
            String passwordUpdateResult = sshLink.execute(ArtooLinkManager.SOLOLINK_SSID_CONFIG_PATH + " --set-wifi-password " +
                    password);
            String restartResult = sshLink.execute(ArtooLinkManager.SOLOLINK_SSID_CONFIG_PATH + " --reboot");
            return true;
        } catch (IOException e) {
            Timber.e(e, "Error occurred while updating the sololink wifi ssid.");
            return false;
        }
    }

    private void updateSoloLinkVersion() {
        postAsyncTask(soloLinkVersionRetriever);
    }

    private void updatePixhawkVersion(){
        postAsyncTask(pixhawkVersionRetriever);
    }

    private String retrieveVersion(String versionFile) {
        try {
            String version = sshLink.execute("cat " + versionFile);
            if (TextUtils.isEmpty(version)) {
                Timber.d( "No version file was found");
                return "";
            } else {
                return version.split("\n")[0];
            }
        } catch (IOException e) {
            Timber.e( "Unable to retrieve the current version.", e);
        }

        return null;
    }

}
