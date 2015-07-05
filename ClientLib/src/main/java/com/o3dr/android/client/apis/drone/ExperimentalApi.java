package com.o3dr.android.client.apis.drone;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_EPM_COMMAND;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SEND_MAVLINK_MESSAGE;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_RELAY;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_ROI;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_SERVO;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_TRIGGER_CAMERA;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_EPM_RELEASE;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_IS_RELAY_ON;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_MAVLINK_MESSAGE;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_RELAY_NUMBER;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SERVO_CHANNEL;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SERVO_PWM;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SET_ROI_LAT_LONG_ALT;


/**
 * Contains drone commands with no defined interaction model yet.
 */
public class ExperimentalApi {

    public static void triggerCamera(Drone drone) {
        drone.performAsyncAction(new Action(ACTION_TRIGGER_CAMERA));
    }

    /**
     * Specify a region of interest for the vehicle to point at.
     *
     * @param drone    target vehicle
     * @param roi      Region of interest coordinate.
     */
    public static void setROI(Drone drone, LatLongAlt roi) {
        setROI(drone, roi, null);
    }

    /**
     * Specify a region of interest for the vehicle to point at.
     *
     * @param drone target vehicle
     * @param roi   Region of interest coordinate.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public static void setROI(Drone drone, LatLongAlt roi, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_SET_ROI_LAT_LONG_ALT, roi);
        Action epmAction = new Action(ACTION_SET_ROI, params);
        drone.performAsyncActionOnDroneThread(epmAction, listener);
    }

    public static void epmCommand(Drone drone, boolean release) {
        epmCommand(drone, release, null);
    }

    public static void epmCommand(Drone drone, boolean release, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_EPM_RELEASE, release);
        Action epmAction = new Action(ACTION_EPM_COMMAND, params);
        drone.performAsyncActionOnDroneThread(epmAction, listener);
    }

    /**
     * This is an advanced/low-level method to send raw mavlink to the vehicle.
     * <p/>
     * This method is included as an ‘escape hatch’ to allow developers to make progress if we’ve
     * somehow missed providing some essential operation in the rest of this API. Callers do
     * not need to populate sysId/componentId/crc in the packet, this method will take care of that
     * before sending.
     * <p/>
     * If you find yourself needing to use this method please contact the drone-platform google
     * group and we’ll see if we can support the operation you needed in some future revision of
     * the API.
     *
     * @param messageWrapper A MAVLinkMessage wrapper instance. No need to fill in
     *                       sysId/compId/seqNum - the API will take care of that.
     */
    public static void sendMavlinkMessage(Drone drone, MavlinkMessageWrapper messageWrapper) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MAVLINK_MESSAGE, messageWrapper);
        drone.performAsyncAction(new Action(ACTION_SEND_MAVLINK_MESSAGE, params));
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param drone       target vehicle
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     */
    public static void setRelay(Drone drone, int relayNumber, boolean enabled) {
        setRelay(drone, relayNumber, enabled, null);
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param drone       target vehicle
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     * @param listener    Register a callback to receive update of the command execution state.
     */
    public static void setRelay(Drone drone, int relayNumber, boolean enabled, AbstractCommandListener listener) {
        Bundle params = new Bundle(2);
        params.putInt(EXTRA_RELAY_NUMBER, relayNumber);
        params.putBoolean(EXTRA_IS_RELAY_ON, enabled);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_RELAY, params), listener);
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param drone       target vehicle
     * @param channel     the output channel the servo is attached to
     * @param pwm         PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     */
    public static void setServo(Drone drone, int channel, int pwm) {
        setServo(drone, channel, pwm, null);
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param drone    target vehicle
     * @param channel  the output channel the servo is attached to
     * @param pwm      PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     * @param listener Register a callback to receive update of the command execution state.
     */
    public static void setServo(Drone drone, int channel, int pwm, AbstractCommandListener listener) {
        Bundle params = new Bundle(2);
        params.putInt(EXTRA_SERVO_CHANNEL, channel);
        params.putInt(EXTRA_SERVO_PWM, pwm);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_SERVO, params), listener);
    }
}
