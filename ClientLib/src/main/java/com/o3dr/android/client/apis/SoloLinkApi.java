package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloLinkActions.*;

/**
 * Provides access to the sololink specific functionality
 * Created by Fredia Huya-Kouadio on 7/12/15.
 */
public class SoloLinkApi implements Api {

    private static final ConcurrentHashMap<Drone, SoloLinkApi> soloLinkApiCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a sololink api instance.
     * @param drone target vehicle
     * @return a SoloLinkApi instance.
     */
    public static SoloLinkApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, soloLinkApiCache, new Builder<SoloLinkApi>() {
            @Override
            public SoloLinkApi build() {
                return new SoloLinkApi(drone);
            }
        });
    }

    private final Drone drone;

    private SoloLinkApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Updates the wifi settings for the solo vehicle.
     * @param wifiSsid Updated wifi ssid
     * @param wifiPassword Updated wifi password
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void updateWifiSettings(String wifiSsid, String wifiPassword, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putString(EXTRA_WIFI_SSID, wifiSsid);
        params.putString(EXTRA_WIFI_PASSWORD, wifiPassword);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_WIFI_SETTINGS, params), listener);
    }

    /**
     * Updates the button settings for the solo vehicle.
     * @param buttonSettings Updated button settings.
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void updateButtonSettings(SoloButtonSettingSetter buttonSettings, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_BUTTON_SETTINGS, buttonSettings);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_BUTTON_SETTINGS, params), listener);
    }

    /**
     * Sends a message to the solo vehicle.
     * @param messagePacket TLV message data.
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void sendMessage(TLVPacket messagePacket, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MESSAGE_DATA, messagePacket);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SEND_MESSAGE, params), listener);
    }
}
