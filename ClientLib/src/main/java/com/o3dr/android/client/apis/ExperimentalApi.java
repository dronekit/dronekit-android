package com.o3dr.android.client.apis;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.utils.connection.IpConnectionListener;
import com.o3dr.android.client.utils.connection.UdpConnection;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.CameraActions.ACTION_START_VIDEO_STREAM_FOR_OBSERVER;
import static com.o3dr.services.android.lib.drone.action.CameraActions.ACTION_STOP_VIDEO_STREAM_FOR_OBSERVER;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_TAG;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SEND_MAVLINK_MESSAGE;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_RELAY;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_ROI;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_SERVO;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_TRIGGER_CAMERA;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_IS_RELAY_ON;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_MAVLINK_MESSAGE;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_RELAY_NUMBER;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SERVO_CHANNEL;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SERVO_PWM;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SET_ROI_LAT_LONG_ALT;

/**
 * Contains drone commands with no defined interaction model yet.
 */
public class ExperimentalApi extends Api {
    private static final ConcurrentHashMap<Drone, ExperimentalApi> experimentalApiCache = new ConcurrentHashMap<>();
    private static final Builder<ExperimentalApi> apiBuilder = new Builder<ExperimentalApi>() {
        @Override
        public ExperimentalApi build(Drone drone) {
            return new ExperimentalApi(drone);
        }
    };

    private final CapabilityApi capabilityChecker;

    /**
     * Retrieves an ExperimentalApi instance.
     *
     * @param drone target vehicle.
     * @return a ExperimentalApi instance.
     */
    public static ExperimentalApi getApi(final Drone drone) {
        return getApi(drone, experimentalApiCache, apiBuilder);
    }

    private final Drone drone;

    private ExperimentalApi(Drone drone) {
        this.drone = drone;
        this.capabilityChecker = CapabilityApi.getApi(drone);
    }

    /**
     * Triggers the camera.
     */
    public void triggerCamera() {
        drone.performAsyncAction(new Action(ACTION_TRIGGER_CAMERA));
    }

    /**
     * Specify a region of interest for the vehicle to point at.
     *
     * @param roi Region of interest coordinate.
     */
    public void setROI(LatLongAlt roi) {
        setROI(roi, null);
    }

    /**
     * Specify a region of interest for the vehicle to point at.
     *
     * @param roi      Region of interest coordinate.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void setROI(LatLongAlt roi, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_SET_ROI_LAT_LONG_ALT, roi);
        Action epmAction = new Action(ACTION_SET_ROI, params);
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
    public void sendMavlinkMessage(MavlinkMessageWrapper messageWrapper) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MAVLINK_MESSAGE, messageWrapper);
        drone.performAsyncAction(new Action(ACTION_SEND_MAVLINK_MESSAGE, params));
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     */
    public void setRelay(int relayNumber, boolean enabled) {
        setRelay(relayNumber, enabled, null);
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     * @param listener    Register a callback to receive update of the command execution state.
     */
    public void setRelay(int relayNumber, boolean enabled, AbstractCommandListener listener) {
        Bundle params = new Bundle(2);
        params.putInt(EXTRA_RELAY_NUMBER, relayNumber);
        params.putBoolean(EXTRA_IS_RELAY_ON, enabled);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_RELAY, params), listener);
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param channel the output channel the servo is attached to
     * @param pwm     PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     */
    public void setServo(int channel, int pwm) {
        setServo(channel, pwm, null);
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param channel  the output channel the servo is attached to
     * @param pwm      PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void setServo(int channel, int pwm, AbstractCommandListener listener) {
        Bundle params = new Bundle(2);
        params.putInt(EXTRA_SERVO_CHANNEL, channel);
        params.putInt(EXTRA_SERVO_PWM, pwm);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_SERVO, params), listener);
    }

    /**
     * Attempt to grab ownership and start the video stream from the connected drone. Can fail if
     * the video stream is already owned by another client.
     *
     * @param tag       Video tag.
     * @param listener  Register a callback to receive update of the command execution status.
     *
     * @since 2.5.0
     */
    public void startVideoStream(final String tag, final AbstractCommandListener listener) {
        capabilityChecker.checkFeatureSupport(CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING,
            new CapabilityApi.FeatureSupportListener() {
                @Override
                public void onFeatureSupportResult(String featureId, int result, Bundle resultInfo) {
                    switch (result) {
                        case CapabilityApi.FEATURE_SUPPORTED:
                            startVideoStreamForObserver(tag, listener);
                            break;

                        case CapabilityApi.FEATURE_UNSUPPORTED:
                            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                            break;

                        default:
                            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                            break;
                    }
                }
            });
    }

    /**
     * Stop the video stream from the connected drone, and release ownership.
     *
     * @param tag       Video tag.
     * @param listener  Register a callback to receive update of the command execution status.
     *
     * @since 2.5.0
     */
    public void stopVideoStream(final String tag, final AbstractCommandListener listener) {
        capabilityChecker.checkFeatureSupport(CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING,
            new CapabilityApi.FeatureSupportListener() {
                @Override
                public void onFeatureSupportResult(String featureId, int result, Bundle resultInfo) {
                    switch (result) {
                        case CapabilityApi.FEATURE_SUPPORTED:
                            stopVideoStreamForObserver(tag, listener);
                            break;

                        case CapabilityApi.FEATURE_UNSUPPORTED:
                            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                            break;

                        default:
                            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                            break;
                    }
                }
            });
    }

    /**
     * Attempt to grab ownership and start the video stream from the connected drone. Can fail if
     * the video stream is already owned by another client.
     *
     * @param tag       Video tag.
     * @param listener  Register a callback to receive update of the command execution status.
     * @since 2.6.8
     */
    public void startVideoStreamForObserver(final String tag, final AbstractCommandListener listener) {
        final Bundle params = new Bundle();
        params.putString(EXTRA_VIDEO_TAG, tag);

        drone.performAsyncActionOnDroneThread(new Action(ACTION_START_VIDEO_STREAM_FOR_OBSERVER,
            params), listener);
    }

    /**
     * Stop the video stream from the connected drone, and release ownership.
     *
     * @param tag      Video tag.
     * @param listener Register a callback to receive update of the command execution status.
     * @since 2.6.8
     */
    public void stopVideoStreamForObserver(final String tag, final AbstractCommandListener listener) {
        final Bundle params = new Bundle();
        params.putString(EXTRA_VIDEO_TAG, tag);

        drone.performAsyncActionOnDroneThread(new Action(ACTION_STOP_VIDEO_STREAM_FOR_OBSERVER, params),
            listener);
    }

    /**
     * Observer for vehicle video stream.
     */
    public static class VideoStreamObserver implements IpConnectionListener {
        private final String TAG = VideoStreamObserver.class.getSimpleName();

        private static final int UDP_BUFFER_SIZE = 1500;
        private static final long RECONNECT_COUNTDOWN_IN_MILLIS = 1000l;
        private static final int SOLO_STREAM_UDP_PORT = 5600;

        private UdpConnection linkConn;
        private Handler handler;

        private ExperimentalApi.IVideoStreamCallback callback;

        public VideoStreamObserver(Handler handler, ExperimentalApi.IVideoStreamCallback callback) {
            this.handler = handler;
            this.callback = callback;
        }

        private final Runnable reconnectTask = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(reconnectTask);
                if(linkConn != null)
                    linkConn.connect();
            }
        };

        public void start() {
            if (this.linkConn == null) {
                this.linkConn = new UdpConnection(handler, SOLO_STREAM_UDP_PORT,
                    UDP_BUFFER_SIZE, true, 42);
                this.linkConn.setIpConnectionListener(this);
            }

            handler.removeCallbacks(reconnectTask);

            Log.d(TAG, "Connecting to video stream...");
            this.linkConn.connect();
        }

        public void stop() {
            Log.d(TAG, "Stopping video manager");

            handler.removeCallbacks(reconnectTask);

            if (this.linkConn != null) {
                // Break the link.
                this.linkConn.disconnect();
                this.linkConn = null;
            }
        }

        @Override
        public void onIpConnected() {
            Log.d(TAG, "Connected to video stream");

            handler.removeCallbacks(reconnectTask);
        }

        @Override
        public void onIpDisconnected() {
            Log.d(TAG, "Video stream disconnected");

            handler.postDelayed(reconnectTask, RECONNECT_COUNTDOWN_IN_MILLIS);
        }

        @Override
        public void onPacketReceived(ByteBuffer packetBuffer) {
            callback.onVideoStreamPacketRecieved(packetBuffer.array(), packetBuffer.limit());
        }
    }

    /**
     * Callback for retrieving video packets from VideoStreamObserver.
     */
    public interface IVideoStreamCallback {
        void onVideoStreamPacketRecieved(byte[] data, int dataSize);
    }
}
