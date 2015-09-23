package org.droidplanner.services.android.core.drone.autopilot.apm;

import android.content.Context;
import android.text.TextUtils;

import com.MAVLink.common.msg_statustext;
import com.MAVLink.enums.MAV_TYPE;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.State;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.LogMessageListener;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;

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
    private String pixhawkSerialNumber;

    public ArduSolo(Context context, MAVLinkStreams.MAVLinkOutputStream mavClient, DroneInterfaces.Handler handler, Preferences pref, AutopilotWarningParser warningParser, LogMessageListener logListener, DroneInterfaces.AttributeEventListener listener) {
        super(context, mavClient, handler, pref, warningParser, logListener, listener);
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
    public DroneAttribute getAttribute(String attributeType){
        final DroneAttribute attribute = super.getAttribute(attributeType);
        if(attribute instanceof State){
            ((State) attribute).addToVehicleUid(SERIAL_NUMBER_LABEL, pixhawkSerialNumber);
        }

        return attribute;
    }

    @Override
    protected void processSignalUpdate(int rxerrors, int fixed, short rssi, short remrssi, short txbuf,
                                       short noise, short remnoise){
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
    protected void processStatusText(msg_statustext statusText){
        super.processStatusText(statusText);

        final String message = statusText.getText();
        if (!TextUtils.isEmpty(message)) {

            //Parse pixhawk serial number.
            final Matcher matcher = PIXHAWK_SERIAL_NUMBER_PATTERN.matcher(message);
            if(matcher.matches()){
                Timber.i("Received serial number: %s", message);

                final String serialNumber = matcher.group(2) + matcher.group(3) + matcher.group(4);
                if(!serialNumber.equalsIgnoreCase(pixhawkSerialNumber)){
                    pixhawkSerialNumber = serialNumber;

                    notifyAttributeListener(AttributeEvent.STATE_VEHICLE_UID);
                }
            }
        }
    }
}
