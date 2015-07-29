package org.droidplanner.services.android.core.drone.autopilot.apm;

import android.content.Context;
import android.text.TextUtils;

import com.MAVLink.enums.MAV_TYPE;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.LogMessageListener;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduSolo extends ArduCopter {

    public ArduSolo(Context context, MAVLinkStreams.MAVLinkOutputStream mavClient, DroneInterfaces.Handler handler, Preferences pref, AutopilotWarningParser warningParser, LogMessageListener logListener) {
        super(context, mavClient, handler, pref, warningParser, logListener);
    }

    @Override
    public int getType(){
        return MAV_TYPE.MAV_TYPE_QUADROTOR;
    }

    @Override
    public void setType(int type){}

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.ARDU_SOLO;
    }

    @Override
    protected void processSignalUpdate(int rxerrors, int fixed, short rssi, short remrssi, short txbuf,
                                       short noise, short remnoise){
        signal.setValid(true);
        signal.setRxerrors(rxerrors & 0xFFFF);
        signal.setFixed(fixed & 0xFFFF);
        signal.setRssi(rssi & 0xFF);
        signal.setRemrssi(remrssi & 0xFF);
        signal.setNoise(noise & 0xFF);
        signal.setRemnoise(remnoise & 0xFF);
        signal.setTxbuf(txbuf & 0xFF);

        notifyDroneEvent(DroneInterfaces.DroneEventsType.RADIO);
    }
}
