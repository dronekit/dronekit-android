package org.droidplanner.services.android.core.drone.autopilot;

import android.text.TextUtils;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_radio_status;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.drone.DroneEvents;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.utils.CommonApiUtils;

/**
 * Base drone implementation. Supports mavlink messages belonging to the common set: https://pixhawk.ethz.ch/mavlink/
 * Created by Fredia Huya-Kouadio on 9/10/15.
 */
public abstract class CommonMavLinkDrone implements MavLinkDrone {

    private final MAVLinkStreams.MAVLinkOutputStream MavClient;
    private final DroneEvents events;

    protected final Altitude altitude = new Altitude();
    protected final Speed speed = new Speed();
    protected final Battery battery = new Battery();
    protected final Signal signal = new Signal();
    protected final Attitude attitude = new Attitude();

    protected CommonMavLinkDrone(DroneInterfaces.Handler handler, MAVLinkStreams.MAVLinkOutputStream mavClient) {
        this.MavClient = mavClient;
        events = new DroneEvents(this, handler);
    }

    @Override
    public boolean isConnected() {
        return MavClient.isConnected();
    }

    @Override
    public void addDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.addDroneListener(listener);
    }

    @Override
    public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.removeDroneListener(listener);
    }

    @Override
    public void notifyDroneEvent(final DroneInterfaces.DroneEventsType event) {
        switch (event) {
            case DISCONNECTED:
                signal.setValid(false);
                break;
        }

        events.notifyDroneEvent(event);
    }

    @Override
    public MAVLinkStreams.MAVLinkOutputStream getMavClient() {
        return MavClient;
    }

    @Override
    public DroneAttribute getAttribute(String attributeType) {
        if (TextUtils.isEmpty(attributeType))
            return null;

        switch (attributeType) {
            case AttributeType.SPEED:
                return speed;

            case AttributeType.BATTERY:
                return battery;

            case AttributeType.SIGNAL:
                return signal;

            case AttributeType.ATTITUDE:
                return attitude;

            case AttributeType.ALTITUDE:
                return altitude;
        }

        return null;
    }

    @Override
    public void onMavLinkMessageReceived(MAVLinkMessage message) {
        switch (message.msgid) {
            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                msg_radio_status m_radio_status = (msg_radio_status) message;
                processSignalUpdate(m_radio_status.rxerrors, m_radio_status.fixed, m_radio_status.rssi,
                        m_radio_status.remrssi, m_radio_status.txbuf, m_radio_status.noise, m_radio_status.remnoise);
                break;

            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
                msg_attitude m_att = (msg_attitude) message;
                processAttitude(m_att);
                break;
        }
    }

    private void processAttitude(msg_attitude m_att) {
        attitude.setRoll(CommonApiUtils.fromRadToDeg(m_att.roll));
        attitude.setRollSpeed(CommonApiUtils.fromRadToDeg(m_att.rollspeed));

        attitude.setPitch(CommonApiUtils.fromRadToDeg(m_att.pitch));
        attitude.setPitchSpeed(CommonApiUtils.fromRadToDeg(m_att.pitchspeed));

        attitude.setYaw(CommonApiUtils.fromRadToDeg(m_att.yaw));
        attitude.setYawSpeed(CommonApiUtils.fromRadToDeg(m_att.yawspeed));

        notifyDroneEvent(DroneInterfaces.DroneEventsType.ATTITUDE);
    }

    protected void processSignalUpdate(int rxerrors, int fixed, short rssi, short remrssi, short txbuf,
                                       short noise, short remnoise) {
        signal.setValid(true);
        signal.setRxerrors(rxerrors & 0xFFFF);
        signal.setFixed(fixed & 0xFFFF);
        signal.setRssi(SikValueToDB(rssi & 0xFF));
        signal.setRemrssi(SikValueToDB(remrssi & 0xFF));
        signal.setNoise(SikValueToDB(noise & 0xFF));
        signal.setRemnoise(SikValueToDB(remnoise & 0xFF));
        signal.setTxbuf(txbuf & 0xFF);

        signal.setSignalStrength(MathUtils.getSignalStrength(signal.getFadeMargin(), signal.getRemFadeMargin()));

        notifyDroneEvent(DroneInterfaces.DroneEventsType.RADIO);
    }

    /**
     * Scalling done at the Si1000 radio More info can be found at:
     * http://copter.ardupilot.com/wiki/common-using-the-3dr-radio-for-telemetry-with-apm-and-px4/#Power_levels
     */
    protected double SikValueToDB(int value) {
        return (value / 1.9) - 127;
    }

}
