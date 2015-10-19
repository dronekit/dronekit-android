package org.droidplanner.services.android.core.drone.companion.solo.controller;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;

import com.github.zafarkhaja.semver.Version;
import com.o3dr.android.client.utils.connection.IpConnectionListener;
import com.o3dr.android.client.utils.connection.TcpConnection;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageParser;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.core.drone.companion.solo.AbstractLinkManager;
import org.droidplanner.services.android.core.drone.companion.solo.SoloComp;
import org.droidplanner.services.android.utils.NetworkUtils;
import org.droidplanner.services.android.utils.connection.SshConnection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Handles artoo link related logic.
 */
public class ControllerLinkManager extends AbstractLinkManager<ControllerLinkListener> {

    /**
     * This is the minimum version that provides an api for the controller mode update.
     */
    private static final Version CONTROLLER_MODE_MIN_VERSION = Version.forIntegers(1, 1, 13);

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
    private final AtomicInteger controllerMode = new AtomicInteger(SoloControllerMode.UNKNOWN_MODE);

    private final AtomicReference<String> controllerUnits = new AtomicReference<>("");

    private final AtomicReference<Pair<String, String>> sololinkWifiInfo = new AtomicReference<>(Pair.create("", ""));

    private final Runnable reconnectBatteryTask = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);
            batteryConnection.connect();
        }
    };

    private final Runnable reconnectVideoHandshake = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);
            videoHandshake.connect();
        }
    };


    private final AtomicBoolean isVideoHandshakeStarted = new AtomicBoolean(false);
    private final AtomicBoolean isBatteryStarted = new AtomicBoolean(false);

    private final TcpConnection videoHandshake;
    private final TcpConnection batteryConnection;

    protected static final SshConnection sshLink = new SshConnection(ARTOO_IP, SoloComp.SSH_USERNAME,
            SoloComp.SSH_PASSWORD);

    private final Runnable artooVersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(ARTOO_VERSION_FILENAME);
            if (version != null)
                controllerVersion.set(version);

            updateControllerModeIfPossible();
            updateControllerUnitIfPossible();

            onVersionsUpdated();
        }
    };

    private final Runnable stm32VersionRetriever = new Runnable() {
        @Override
        public void run() {
            final String version = retrieveVersion(STM32_VERSION_FILENAME);
            if (version != null)
                stm32Version.set(version);

            onVersionsUpdated();
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
                isCompliant = !sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --get-wifi-country").trim().equals("US");
                if (linkListener != null)
                    linkListener.onEUTxPowerComplianceUpdated(isCompliant);
            } catch (IOException e) {
                Timber.e(e, "Error occurred while querying wifi country.");
                isCompliant = false; //because most users will have it disabled by default
            }

            isEUTxPowerCompliant.set(isCompliant);
        }
    };

    private final Runnable artooModeRetriever = new Runnable() {
        @Override
        public void run() {
            Timber.i("Retrieving controller mode");
            try {
                final String response = sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --get-ui-mode");
                final String trimmedResponse = TextUtils.isEmpty(response) ? "" : response.trim();
                switch (trimmedResponse) {
                    case "1":
                        setControllerMode(SoloControllerMode.MODE_1);
                        break;

                    case "2":
                        setControllerMode(SoloControllerMode.MODE_2);
                        break;

                    default:
                        Timber.w("Unable to parse received controller mode.");
                        setControllerMode(SoloControllerMode.UNKNOWN_MODE);
                        break;
                }

            } catch (IOException e) {
                Timber.e(e, "Error occurred while getting controller mode.");
            }
        }
    };

    private final Runnable unitsRetriever = new Runnable() {
        @Override
        public void run() {
            Timber.d("Retrieving controller units.");
            try {
                final String response = sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --get-ui-units");
                @SoloControllerUnits.ControllerUnit final String trimmedResponse = TextUtils.isEmpty(response) ? SoloControllerUnits.UNKNOWN : response.trim();
                switch (trimmedResponse) {
                    case SoloControllerUnits.METRIC:
                    case SoloControllerUnits.IMPERIAL:
                    case SoloControllerUnits.UNKNOWN:
                        setControllerUnit(trimmedResponse);
                        break;

                    default:
                        Timber.w("Received unknown value for controller unit: %s", trimmedResponse);
                        break;
                }
            } catch (IOException e) {
                Timber.e(e, "Error occurred while retrieving the controller units.");
            }
        }
    };

    private ControllerLinkListener linkListener;
    private final AtomicBoolean streamingPermission = new AtomicBoolean(false);

    public ControllerLinkManager(Context context, final Handler handler, ExecutorService asyncExecutor) {
        super(context, new TcpConnection(handler, ARTOO_IP, ARTOO_BUTTON_PORT), handler, asyncExecutor);

        videoHandshake = new TcpConnection(handler, ARTOO_IP, ARTOO_VIDEO_HANDSHAKE_PORT);
        videoHandshake.setIpConnectionListener(new IpConnectionListener() {

            @Override
            public void onIpConnected() {
                handler.removeCallbacks(reconnectVideoHandshake);
                Timber.d("Artoo link connected. Starting video stream...");

                streamingPermission.set(true);
            }

            @Override
            public void onIpDisconnected() {
                streamingPermission.set(false);

                if (isVideoHandshakeStarted.get())
                    handler.postDelayed(reconnectVideoHandshake, RECONNECT_COUNTDOWN);
            }

            @Override
            public void onPacketReceived(ByteBuffer packetBuffer) {
            }
        });

        batteryConnection = new TcpConnection(handler, ARTOO_IP, ARTOO_BATTERY_PORT);
        batteryConnection.setIpConnectionListener(new IpConnectionListener() {

            @Override
            public void onIpConnected() {
                handler.removeCallbacks(reconnectBatteryTask);
            }

            @Override
            public void onIpDisconnected() {
                //Try to connect
                if (isBatteryStarted.get()) {
                    handler.postDelayed(reconnectBatteryTask, RECONNECT_COUNTDOWN);

                }
            }

            @Override
            public void onPacketReceived(ByteBuffer packetBuffer) {

                List<TLVPacket> tlvMsgs = TLVMessageParser.parseTLVPacket(packetBuffer);
                if (tlvMsgs.isEmpty())
                    return;

                for (TLVPacket tlvMsg : tlvMsgs) {
                    final int messageType = tlvMsg.getMessageType();
                    Timber.d("Received tlv message: " + messageType);

                    if (linkListener != null)
                        linkListener.onTlvPacketReceived(tlvMsg);
                }
            }
        });

    }

    public boolean hasStreamingPermission(){
        return streamingPermission.get();
    }

    public boolean areVersionsSet() {
        return !TextUtils.isEmpty(controllerVersion.get()) && !TextUtils.isEmpty(stm32Version.get());
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
    public boolean isEUTxPowerCompliant() {
        return isEUTxPowerCompliant.get();
    }

    /**
     * Return the current controller mode
     *
     * @return MODE_1 or MODE_2
     */
    public
    @SoloControllerMode.ControllerMode
    int getControllerMode() {
        final @SoloControllerMode.ControllerMode int mode = controllerMode.get();
        return mode;
    }

    /**
     * Return the current controller unit
     *
     * @return @see {@link com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits.ControllerUnit}
     */
    public
    @SoloControllerUnits.ControllerUnit
    String getControllerUnit() {
        final @SoloControllerUnits.ControllerUnit String unit = controllerUnits.get();
        return unit;
    }

    private void startVideoManager() {
        handler.removeCallbacks(reconnectVideoHandshake);
        isVideoHandshakeStarted.set(true);
        videoHandshake.connect();
    }

    private void stopVideoManager() {
        handler.removeCallbacks(reconnectVideoHandshake);
        isVideoHandshakeStarted.set(false);
        videoHandshake.disconnect();
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
            restartController();
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
    public void start(ControllerLinkListener listener) {
        this.linkListener = listener;

        if(!isStarted()) {
            Timber.i("Starting artoo link manager");
        }

        super.start(listener);

        //TODO: update when battery info is available
//        handler.removeCallbacks(reconnectBatteryTask);
        //isBatteryStarted.set(true);
        //batteryConnection.connect();

    }

    public void stop() {
        if(isStarted()) {
            Timber.i("Stopping artoo link manager");
        }

        //TODO: update when battery info is available
        /*handler.removeCallbacks(reconnectBatteryTask);
        isBatteryStarted.set(false);
        batteryConnection.disconnect();*/

        super.stop();
    }

    @Override
    public boolean isLinkConnected() {
        return NetworkUtils.isOnSololinkNetwork(context);
    }

    @Override
    public void refreshState() {
        Timber.d("Artoo link connected.");

        //Load the mac address for the vehicle.
        loadMacAddress();

        startVideoManager();

        //Update sololink wifi
        loadSololinkWifiInfo();

        refreshControllerVersions();

        //Update the tx power compliance
        loadCurrentEUTxPowerComplianceMode();
    }

    @Override
    protected SshConnection getSshLink() {
        return sshLink;
    }

    private void onVersionsUpdated() {
        if (linkListener != null && areVersionsSet())
            linkListener.onVersionsUpdated();
    }

    private void updateControllerUnitIfPossible() {
        if (doesSupportControllerMode()) {
            Timber.d("Updating current controller unit.");
            loadControllerUnit();
        } else {
            Timber.w("This controller version doesn't support controller unit retrieval.");
        }
    }

    private void updateControllerModeIfPossible() {
        if (doesSupportControllerMode()) {
            //load current controller mode
            Timber.d("Updating current controller mode.");
            loadCurrentControllerMode();
        } else {
            Timber.w("This controller version doesn't support controller mode retrieval.");
        }
    }

    private boolean doesSupportControllerMode() {
        final String version = controllerVersion.get();
        if (TextUtils.isEmpty(version))
            return false;

        try {
            final Version currentVersion = Version.valueOf(version);
            return CONTROLLER_MODE_MIN_VERSION.lessThanOrEqualTo(currentVersion);
        } catch (Exception e) {
            Timber.e(e, "Unable to parse controller version.");
            return false;
        }
    }

    @Override
    public void onIpDisconnected() {
        Timber.d("Artoo link disconnected.");
        stopVideoManager();
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

    public void updateControllerUnit(@SoloControllerUnits.ControllerUnit final String unit, final ICommandListener listener) {
        postAsyncTask(new Runnable() {
            @Override
            public void run() {
                final boolean supportControllerMode = doesSupportControllerMode();
                if (!supportControllerMode) {
                    postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                    return;
                }

                Timber.d("Switching controller unit to %s", unit);
                try {
                    final String command = SOLOLINK_SSID_CONFIG_PATH + " --set-ui-units %s";
                    final String response = sshLink.execute(String.format(Locale.US, command, unit));
                    Timber.d("Response from unit change was: %s", response);
                    postSuccessEvent(listener);

                    setControllerUnit(unit);
                } catch (IOException e) {
                    Timber.e(e, "Error occurred while changing controller unit.");
                    postTimeoutEvent(listener);
                }
            }
        });
    }

    private void setControllerUnit(@SoloControllerUnits.ControllerUnit String unit) {
        controllerUnits.set(unit);
        if (linkListener != null)
            linkListener.onControllerUnitUpdated(unit);
    }

    public void updateControllerMode(@SoloControllerMode.ControllerMode final int mode, final ICommandListener listener) {
        postAsyncTask(new Runnable() {
            @Override
            public void run() {
                Timber.d("Switching controller to mode %d", mode);
                try {
                    final boolean supportControllerMode = doesSupportControllerMode();
                    final String command = supportControllerMode
                            ? SOLOLINK_SSID_CONFIG_PATH + " --set-ui-mode %d"
                            : "runStickMapperMode%d.sh";
                    final String response;
                    switch (mode) {
                        case SoloControllerMode.MODE_1:
                            response = sshLink.execute(String.format(Locale.US, command, mode));
                            postSuccessEvent(listener);
                            break;

                        case SoloControllerMode.MODE_2:
                            response = sshLink.execute(String.format(Locale.US, command, mode));
                            postSuccessEvent(listener);
                            break;

                        default:
                            response = "No response.";
                            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                            break;
                    }
                    Timber.d("Response from switch mode command was: %s", response);

                    if (supportControllerMode) {
                        setControllerMode(mode);
                    }
                } catch (IOException e) {
                    Timber.e(e, "Error occurred while changing controller modes.");
                    postTimeoutEvent(listener);
                }

            }
        });
    }

    private void setControllerMode(@SoloControllerMode.ControllerMode int mode) {
        controllerMode.set(mode);
        if (linkListener != null)
            linkListener.onControllerModeUpdated();
    }

    public void setEUTxPowerCompliance(final boolean compliant, final ICommandListener listener) {
        postAsyncTask(new Runnable() {
            @Override
            public void run() {
                Timber.d("%s EU Tx power compliance mode", compliant ? "Enabling" : "Disabling");
                try {
                    final boolean currentCompliance = !sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --get-wifi-country").trim().equals("US");
                    if (currentCompliance != compliant) {
                        final String response;
                        if (compliant) {
                            response = sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --set-wifi-country FR; echo $?");

                        } else {
                            response = sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --set-wifi-country US; echo $?");
                        }
                        if (response.trim().equals("0")) {
                            restartController();
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

    private void loadCurrentEUTxPowerComplianceMode() {
        postAsyncTask(checkEUTxPowerCompliance);
    }

    private void loadCurrentControllerMode() {
        postAsyncTask(artooModeRetriever);
    }

    private void loadControllerUnit() {
        postAsyncTask(unitsRetriever);
    }

    private void restartController() {
        try {
            sshLink.execute(SOLOLINK_SSID_CONFIG_PATH + " --reboot");
        } catch (IOException e) {
            Timber.e(e, "Error occurred while restarting hostpad service on Artoo.");
        }
    }

    /**
     * Refresh the vehicle's components versions
     */
    public void refreshControllerVersions() {
        updateArtooVersion();
        updateStm32Version();
    }
}
