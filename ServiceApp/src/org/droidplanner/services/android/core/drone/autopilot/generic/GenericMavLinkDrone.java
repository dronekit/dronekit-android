package org.droidplanner.services.android.core.drone.autopilot.generic;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Surface;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_radio_status;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vibration;
import com.MAVLink.enums.MAV_SYS_STATUS_SENSOR;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.action.StateActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.drone.property.Vibration;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.drone.DroneEvents;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.core.drone.variables.State;
import org.droidplanner.services.android.core.drone.variables.StreamRates;
import org.droidplanner.services.android.core.drone.variables.Type;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;
import org.droidplanner.services.android.utils.CommonApiUtils;
import org.droidplanner.services.android.utils.video.VideoManager;

/**
 * Base drone implementation.
 * Supports mavlink messages belonging to the common set: https://pixhawk.ethz.ch/mavlink/
 * <p/>
 * Created by Fredia Huya-Kouadio on 9/10/15.
 */
public abstract class GenericMavLinkDrone implements MavLinkDrone {

    private final MAVLinkStreams.MAVLinkOutputStream mavClient;

    protected final VideoManager videoMgr;

    private final DroneEvents events;
    protected final Type type;
    private final State state;
    private final StreamRates streamRates;

    private final DroneInterfaces.AttributeEventListener attributeListener;

    private final Gps vehicleGps = new Gps();
    protected final Altitude altitude = new Altitude();
    protected final Speed speed = new Speed();
    protected final Battery battery = new Battery();
    protected final Signal signal = new Signal();
    protected final Attitude attitude = new Attitude();
    protected final Vibration vibration = new Vibration();

    protected final Handler handler;

    protected GenericMavLinkDrone(Handler handler, MAVLinkStreams.MAVLinkOutputStream mavClient, AutopilotWarningParser warningParser, DroneInterfaces.AttributeEventListener listener) {
        this.handler = handler;
        this.mavClient = mavClient;

        events = new DroneEvents(this, handler);
        this.type = new Type(this);
        this.streamRates = new StreamRates(this);
        this.state = new State(this, handler, warningParser);

        this.attributeListener = listener;

        this.videoMgr = new VideoManager(handler);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean isConnected() {
        return mavClient.isConnected();
    }

    @Override
    public void addDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.addDroneListener(listener);
    }

    @Override
    public StreamRates getStreamRates() {
        return streamRates;
    }

    @Override
    public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.removeDroneListener(listener);
    }

    public void startVideoStream(Bundle videoProps, String appId, String newVideoTag, Surface videoSurface, final ICommandListener listener) {
        videoMgr.startVideoStream(videoProps, appId, newVideoTag, videoSurface, listener);
    }

    public void stopVideoStream(String appId, String currentVideoTag, final ICommandListener listener) {
        videoMgr.stopVideoStream(appId, currentVideoTag, listener);
    }

    /**
     * Stops the video stream if the current owner is the passed argument.
     *
     * @param appId
     */
    public void tryStoppingVideoStream(String appId) {
        videoMgr.tryStoppingVideoStream(appId);
    }

    protected void notifyAttributeListener(String attributeEvent) {
        notifyAttributeListener(attributeEvent, null);
    }

    protected void notifyAttributeListener(String attributeEvent, Bundle eventInfo) {
        notifyAttributeListener(attributeEvent, eventInfo, false);
    }

    protected void notifyAttributeListener(String attributeEvent, Bundle eventInfo, boolean checkForSololinkApi) {
        if (attributeListener != null) {
            attributeListener.onAttributeEvent(attributeEvent, eventInfo, checkForSololinkApi);
        }
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
        return mavClient;
    }

    @Override
    public boolean executeAsyncAction(Action action, ICommandListener listener) {
        final String type = action.getType();
        Bundle data = action.getData();

        switch (type) {
            //MISSION ACTIONS
            case MissionActions.ACTION_GOTO_WAYPOINT:
                int missionItemIndex = data.getInt(MissionActions.EXTRA_MISSION_ITEM_INDEX);
                CommonApiUtils.gotoWaypoint(this, missionItemIndex, listener);
                return true;

            //STATE ACTIONS
            case StateActions.ACTION_SET_VEHICLE_MODE:
                data.setClassLoader(VehicleMode.class.getClassLoader());
                VehicleMode newMode = data.getParcelable(StateActions.EXTRA_VEHICLE_MODE);
                CommonApiUtils.changeVehicleMode(this, newMode, listener);
                return true;

            default:
                CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                return true;
        }
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

            case AttributeType.STATE:
                return CommonApiUtils.getState(this, isConnected(), vibration);

            case AttributeType.GPS:
                return vehicleGps;
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

            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                msg_heartbeat msg_heart = (msg_heartbeat) message;
                setType(msg_heart.type);
                break;

            case msg_vibration.MAVLINK_MSG_ID_VIBRATION:
                msg_vibration vibrationMsg = (msg_vibration) message;
                processVibrationMessage(vibrationMsg);
                break;

            //*************** EKF State handling ******************//
            case msg_ekf_status_report.MAVLINK_MSG_ID_EKF_STATUS_REPORT:
                state.setEkfStatus((msg_ekf_status_report) message);
                break;

            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
                msg_sys_status m_sys = (msg_sys_status) message;
                processBatteryUpdate(m_sys.voltage_battery / 1000.0, m_sys.battery_remaining,
                        m_sys.current_battery / 100.0);
                checkControlSensorsHealth(m_sys);
                break;

            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
                processGlobalPositionInt((msg_global_position_int) message);
                break;

            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
                processGpsState((msg_gps_raw_int) message);
                break;
        }
    }

    private void checkControlSensorsHealth(msg_sys_status sysStatus) {
        boolean isRCFailsafe = (sysStatus.onboard_control_sensors_health & MAV_SYS_STATUS_SENSOR
                .MAV_SYS_STATUS_SENSOR_RC_RECEIVER) == 0;
        if (isRCFailsafe) {
            state.parseAutopilotError("RC FAILSAFE");
        }
    }

    protected void processBatteryUpdate(double voltage, double remain, double current) {
        if (battery.getBatteryVoltage() != voltage || battery.getBatteryRemain() != remain || battery.getBatteryCurrent() != current) {
            battery.setBatteryVoltage(voltage);
            battery.setBatteryRemain(remain);
            battery.setBatteryCurrent(current);

            notifyDroneEvent(DroneInterfaces.DroneEventsType.BATTERY);
        }
    }

    private void processVibrationMessage(msg_vibration vibrationMsg) {
        boolean wasUpdated = false;

        if (vibration.getVibrationX() != vibrationMsg.vibration_x) {
            vibration.setVibrationX(vibrationMsg.vibration_x);
            wasUpdated = true;
        }

        if (vibration.getVibrationY() != vibrationMsg.vibration_y) {
            vibration.setVibrationY(vibrationMsg.vibration_y);
            wasUpdated = true;
        }

        if (vibration.getVibrationZ() != vibrationMsg.vibration_z) {
            vibration.setVibrationZ(vibrationMsg.vibration_z);
            wasUpdated = true;
        }

        if (vibration.getFirstAccelClipping() != vibrationMsg.clipping_0) {
            vibration.setFirstAccelClipping(vibrationMsg.clipping_0);
            wasUpdated = true;
        }

        if (vibration.getSecondAccelClipping() != vibrationMsg.clipping_1) {
            vibration.setSecondAccelClipping(vibrationMsg.clipping_1);
            wasUpdated = true;
        }

        if (vibration.getThirdAccelClipping() != vibrationMsg.clipping_2) {
            vibration.setThirdAccelClipping(vibrationMsg.clipping_2);
            wasUpdated = true;
        }

        if (wasUpdated) {
            notifyAttributeListener(AttributeEvent.STATE_VEHICLE_VIBRATION);
        }
    }

    protected void setType(int type) {
        this.type.setType(type);
    }

    @Override
    public int getType() {
        return type.getType();
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

    /**
     * Used to update the vehicle location.
     *
     * @param gpi
     */
    protected void processGlobalPositionInt(msg_global_position_int gpi) {
        if (gpi == null)
            return;

        final double newLat = gpi.lat / 1E7;
        final double newLong = gpi.lon / 1E7;

        boolean positionUpdated = false;
        LatLong gpsPosition = vehicleGps.getPosition();
        if (gpsPosition == null) {
            gpsPosition = new LatLong(newLat, newLong);
            vehicleGps.setPosition(gpsPosition);
            positionUpdated = true;
        } else if (gpsPosition.getLatitude() != newLat || gpsPosition.getLongitude() != newLong) {
            gpsPosition.setLatitude(newLat);
            gpsPosition.setLongitude(newLong);
            positionUpdated = true;
        }

        if (positionUpdated) {
            notifyAttributeListener(AttributeEvent.GPS_POSITION);
        }
    }

    private void processGpsState(msg_gps_raw_int gpsState) {
        if (gpsState == null)
            return;

        final double newEph = gpsState.eph / 100.0; // convert from eph(cm) to gps_eph(m)
        if (vehicleGps.getSatellitesCount() != gpsState.satellites_visible
                || vehicleGps.getGpsEph() != newEph) {
            vehicleGps.setSatCount(gpsState.satellites_visible);
            vehicleGps.setGpsEph(newEph);
            notifyAttributeListener(AttributeEvent.GPS_COUNT);
        }

        if (vehicleGps.getFixType() != gpsState.fix_type) {
            vehicleGps.setFixType(gpsState.fix_type);
            notifyAttributeListener(AttributeEvent.GPS_FIX);
        }
    }

}
