package org.droidplanner.services.android.core.drone.autopilot.apm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.MAVLink.common.msg_statustext;
import com.MAVLink.enums.MAV_TYPE;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes;
import com.o3dr.services.android.lib.drone.companion.solo.SoloEventExtras;
import com.o3dr.services.android.lib.drone.companion.solo.SoloEvents;
import com.o3dr.services.android.lib.drone.companion.solo.action.SoloActions;
import com.o3dr.services.android.lib.drone.companion.solo.action.SoloConfigActions;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.LogMessageListener;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.drone.companion.solo.SoloComp;
import org.droidplanner.services.android.core.drone.variables.HeartBeat;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;
import org.droidplanner.services.android.utils.SoloApiUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduSolo extends ArduCopter {

    private static final String PIXHAWK_SERIAL_NUMBER_REGEX = ".*PX4v2 (([0-9A-F]{8}) ([0-9A-F]{8}) ([0-9A-F]{8}))";
    private static final Pattern PIXHAWK_SERIAL_NUMBER_PATTERN = Pattern.compile(PIXHAWK_SERIAL_NUMBER_REGEX);

    private static final String SERIAL_NUMBER_LABEL = "serial_number";

    private final Runnable disconnectSoloCompTask = new Runnable() {
        @Override
        public void run() {
            if (soloComp != null && soloComp.isConnected()) {
                soloComp.stop();
            }

            handler.removeCallbacks(disconnectSoloCompTask);
        }
    };

    private String pixhawkSerialNumber;

    private final SoloComp soloComp;

    public ArduSolo(Context context, MAVLinkStreams.MAVLinkOutputStream mavClient, Handler handler,
                    Preferences pref, AutopilotWarningParser warningParser, LogMessageListener logListener, DroneInterfaces.AttributeEventListener listener) {
        super(context, mavClient, handler, pref, warningParser, logListener, listener);
        this.soloComp = new SoloComp(context, handler);
        this.soloComp.setListener(new SoloComp.SoloCompListener() {
            @Override
            public void onConnected() {
                if (isConnected()) {
                    notifyDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED);
                }
            }

            @Override
            public void onDisconnected() {
                notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED);
            }

            @Override
            public void onTlvPacketReceived(TLVPacket packet) {
                //TODO: filter the message that are broadcast.
                switch (packet.getMessageType()) {
                    case TLVMessageTypes.TYPE_ARTOO_INPUT_REPORT_MESSAGE:
                        //Drop this message as only the battery info is enabled, and that info is already
                        //available from the autopilot.
                        break;

                    case TLVMessageTypes.TYPE_SOLO_GET_BUTTON_SETTING:
                    case TLVMessageTypes.TYPE_SOLO_SET_BUTTON_SETTING:
                        //Drop these messages as they are already being handled by the 'onPresetButtonLoaded(...)' method.
                        break;

                    case TLVMessageTypes.TYPE_SOLO_GOPRO_STATE:
                        notifyAttributeListener(SoloEvents.SOLO_GOPRO_STATE_UPDATED);
                        break;

                    default:
                        final Bundle messageInfo = new Bundle();
                        messageInfo.putParcelable(SoloEventExtras.EXTRA_SOLO_MESSAGE_DATA, packet);

                        notifyAttributeListener(SoloEvents.SOLO_MESSAGE_RECEIVED, messageInfo, true);
                        break;
                }
            }

            @Override
            public void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings) {
                notifyAttributeListener(SoloEvents.SOLO_BUTTON_SETTINGS_UPDATED, null, true);
            }

            @Override
            public void onWifiInfoUpdated(String wifiName, String wifiPassword) {
                notifyAttributeListener(SoloEvents.SOLO_WIFI_SETTINGS_UPDATED, null, true);
            }

            @Override
            public void onButtonPacketReceived(ButtonPacket packet) {
                final Bundle eventInfo = new Bundle();
                eventInfo.putParcelable(SoloEventExtras.EXTRA_SOLO_BUTTON_EVENT, packet);
                notifyAttributeListener(SoloEvents.SOLO_BUTTON_EVENT_RECEIVED, eventInfo, true);
            }

            @Override
            public void onEUTxPowerComplianceUpdated(boolean isCompliant) {
                final Bundle eventInfo = new Bundle(1);
                eventInfo.putBoolean(SoloEventExtras.EXTRA_SOLO_EU_TX_POWER_COMPLIANT, isCompliant);
                notifyAttributeListener(SoloEvents.SOLO_EU_TX_POWER_COMPLIANCE_UPDATED, eventInfo, true);
            }

            @Override
            public void onVersionsUpdated() {
                final Bundle eventInfo = new Bundle();
                eventInfo.putString(SoloEventExtras.EXTRA_SOLO_VEHICLE_VERSION, soloComp.getVehicleVersion());
                eventInfo.putString(SoloEventExtras.EXTRA_SOLO_AUTOPILOT_VERSION, soloComp.getAutopilotVersion());
                eventInfo.putString(SoloEventExtras.EXTRA_SOLO_GIMBAL_VERSION, soloComp.getGimbalVersion());
                eventInfo.putString(SoloEventExtras.EXTRA_SOLO_CONTROLLER_VERSION, soloComp.getControllerVersion());
                eventInfo.putString(SoloEventExtras.EXTRA_SOLO_CONTROLLER_FIRMWARE_VERSION, soloComp.getControllerFirmwareVersion());

                notifyAttributeListener(SoloEvents.SOLO_VERSIONS_UPDATED, eventInfo, true);
            }

            @Override
            public void onControllerEvent(String event, Bundle eventInfo) {
                notifyAttributeListener(event, eventInfo, true);
            }
        });
    }

    public SoloComp getSoloComp() {
        return soloComp;
    }

    @Override
    public int getType() {
        return MAV_TYPE.MAV_TYPE_QUADROTOR;
    }

    @Override
    public void setType(int type) {
    }

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.ARDU_SOLO;
    }

    @Override
    public boolean isConnected(){
        return soloComp.isConnected() && super.isConnected();
    }

    @Override
    public DroneAttribute getAttribute(String attributeType) {
        switch(attributeType){
            case SoloAttributes.SOLO_STATE:
                return SoloApiUtils.getSoloLinkState(this);

            case SoloAttributes.SOLO_GOPRO_STATE:
                return soloComp.getGoproState();

            case AttributeType.STATE:
                final State stateAttr = (State) super.getAttribute(attributeType);
                stateAttr.addToVehicleUid(SERIAL_NUMBER_LABEL, pixhawkSerialNumber);
                stateAttr.addToVehicleUid("solo_mac_address", soloComp.getSoloMacAddress());
                stateAttr.addToVehicleUid("controller_mac_address", soloComp.getControllerMacAddress());
                return stateAttr;

            default:
                return super.getAttribute(attributeType);
        }
    }

    protected void resetVideoManager(){
        videoMgr.reset();
    }

    @Override
    public void startVideoStream(Bundle videoProps, String appId, String newVideoTag, Surface videoSurface, final ICommandListener listener){
        if(!soloComp.hasStreamingPermission()){
            postErrorEvent(CommandExecutionError.COMMAND_DENIED, listener);
            return;
        }

        super.startVideoStream(videoProps, appId, newVideoTag, videoSurface, listener);
    }

    protected void postErrorEvent(final int error, final ICommandListener listener) {
        if (handler != null && listener != null) {
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

    @Override
    public void notifyDroneEvent(final DroneInterfaces.DroneEventsType event) {
        switch (event) {
            case HEARTBEAT_FIRST:
            case CONNECTED:
                Timber.i("Vehicle " + event.name().toLowerCase());
                //Try connecting the companion computer
                if (!soloComp.isConnected()) {
                    resetVideoManager();
                    soloComp.start();
                    return;
                }
                break;

            case DISCONNECTED:
                Timber.i("Vehicle disconnected.");
                if (soloComp.isConnected()) {
                    soloComp.stop();
                    resetVideoManager();
                    return;
                }
                break;

            case HEARTBEAT_TIMEOUT:
                Timber.i("Vehicle heartbeat timed out.");
                if (soloComp.isConnected()) {
                    //Start a countdown at the conclusion of which, disconnect the solo companion computer.
                    handler.postDelayed(disconnectSoloCompTask, HeartBeat.HEARTBEAT_NORMAL_TIMEOUT);
                }
                break;

            case HEARTBEAT_RESTORED:
                Timber.i("Vehicle heartbeat restored.");
                //Dismiss the countdown to disconnect the solo companion computer.
                handler.removeCallbacks(disconnectSoloCompTask);
                if (!soloComp.isConnected())
                    soloComp.start();
                else {
                    soloComp.refreshState();
                }
                break;
        }

        super.notifyDroneEvent(event);
    }

    @Override
    public boolean executeAsyncAction(Action action, final ICommandListener listener){
        final String type = action.getType();
        Bundle data = action.getData();

        switch(type){
            //************ SOLOLINK ACTIONS *************//
            case SoloActions.ACTION_SEND_MESSAGE:
                final TLVPacket messageData = data.getParcelable(SoloActions.EXTRA_MESSAGE_DATA);
                if (messageData != null) {
                    SoloApiUtils.sendSoloLinkMessage(this, messageData, listener);
                }
                return true;

            case SoloConfigActions.ACTION_UPDATE_WIFI_SETTINGS:
                final String wifiSsid = data.getString(SoloConfigActions.EXTRA_WIFI_SSID);
                final String wifiPassword = data.getString(SoloConfigActions.EXTRA_WIFI_PASSWORD);
                SoloApiUtils.updateSoloLinkWifiSettings(this, wifiSsid, wifiPassword, listener);
                return true;

            case SoloConfigActions.ACTION_UPDATE_BUTTON_SETTINGS:
                final SoloButtonSettingSetter buttonSettings = data.getParcelable(SoloConfigActions.EXTRA_BUTTON_SETTINGS);
                if (buttonSettings != null) {
                    SoloApiUtils.updateSoloLinkButtonSettings(this, buttonSettings, listener);
                }
                return true;

            case SoloConfigActions.ACTION_UPDATE_CONTROLLER_MODE:
                final @SoloControllerMode.ControllerMode int mode = data.getInt(SoloConfigActions.EXTRA_CONTROLLER_MODE);
                SoloApiUtils.updateSoloLinkControllerMode(this, mode, listener);
                return true;

            case SoloConfigActions.ACTION_UPDATE_EU_TX_POWER_COMPLIANCE:
                final boolean isCompliant = data.getBoolean(SoloConfigActions.EXTRA_EU_TX_POWER_COMPLIANT, false);
                SoloApiUtils.updateSoloLinkEUTxPowerCompliance(this, isCompliant, listener);
                return true;

            case SoloConfigActions.ACTION_REFRESH_SOLO_VERSIONS:
                soloComp.refreshSoloVersions();
                return true;

            case SoloConfigActions.ACTION_UPDATE_CONTROLLER_UNIT:
                final @SoloControllerUnits.ControllerUnit String unit = data.getString(SoloConfigActions.EXTRA_CONTROLLER_UNIT);
                SoloApiUtils.updateSoloControllerUnit(this, unit, listener);
                return true;

            default:
                return super.executeAsyncAction(action, listener);
        }
    }

    @Override
    protected void processSignalUpdate(int rxerrors, int fixed, short rssi, short remrssi, short txbuf,
                                       short noise, short remnoise) {
        final double unsignedRemRssi = remrssi & 0xFF;

        signal.setValid(true);
        signal.setRxerrors(rxerrors & 0xFFFF);
        signal.setFixed(fixed & 0xFFFF);
        signal.setRssi(rssi & 0xFF);
        signal.setRemrssi(unsignedRemRssi);
        signal.setNoise(noise & 0xFF);
        signal.setRemnoise(remnoise & 0xFF);
        signal.setTxbuf(txbuf & 0xFF);

        final double signalStrength = unsignedRemRssi <= 127 ? unsignedRemRssi : unsignedRemRssi - 256;
        signal.setSignalStrength(signalStrength);

        notifyDroneEvent(DroneInterfaces.DroneEventsType.RADIO);
    }

    @Override
    protected void processStatusText(msg_statustext statusText) {
        super.processStatusText(statusText);

        final String message = statusText.getText();
        if (!TextUtils.isEmpty(message)) {

            //Parse pixhawk serial number.
            final Matcher matcher = PIXHAWK_SERIAL_NUMBER_PATTERN.matcher(message);
            if (matcher.matches()) {
                Timber.i("Received serial number: %s", message);

                final String serialNumber = matcher.group(2) + matcher.group(3) + matcher.group(4);
                if (!serialNumber.equalsIgnoreCase(pixhawkSerialNumber)) {
                    pixhawkSerialNumber = serialNumber;

                    notifyAttributeListener(AttributeEvent.STATE_VEHICLE_UID);
                }
            }
        }
    }
}
