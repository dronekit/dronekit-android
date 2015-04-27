package org.droidplanner.core.drone.camera;


import com.MAVLink.MAVLinkPacket;
import com.MAVLink.ardupilotmega.msg_gopro_get_request;
import com.MAVLink.ardupilotmega.msg_gopro_get_response;
import com.MAVLink.ardupilotmega.msg_gopro_heartbeat;
import com.MAVLink.ardupilotmega.msg_gopro_set_request;
import com.MAVLink.ardupilotmega.msg_gopro_set_response;
import com.MAVLink.enums.GOPRO_COMMAND;
import com.MAVLink.enums.GOPRO_HEARTBEAT_STATUS;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.model.Drone;

import java.util.HashMap;

/**
 * Created by Fredia Huya-Kouadio on 4/7/15.
 */
public class GoProImpl implements DroneInterfaces.OnDroneListener {

    private static final long HEARTBEAT_TIMEOUT = 5000l; //ms

    private int status = GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_DISCONNECTED;
    private boolean isRecording;

    private final Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            onHeartbeatTimeout();
        }
    };

    private final HashMap<Integer, GetResponseHandler> getResponsesFutures = new HashMap<>();
    private final HashMap<Integer, SetResponseHandler> setResponsesFutures = new HashMap<>();

    private final msg_gopro_get_request scratchGetRequest;
    private final msg_gopro_set_request scratchSetRequest;

    private final Drone drone;
    private final Handler watchdog;

    public GoProImpl(Drone drone, Handler handler) {
        this.drone = drone;
        this.watchdog = handler;

        this.scratchGetRequest = new msg_gopro_get_request();
        this.scratchGetRequest.sysid = 255;
        this.scratchGetRequest.compid = 0;
        this.scratchGetRequest.target_system = 0;

        this.scratchSetRequest = new msg_gopro_set_request();
        this.scratchSetRequest.sysid = 255;
        this.scratchSetRequest.compid = 0;
        this.scratchSetRequest.target_system = 0;

        if (drone.isConnected()) {
            updateRequestTarget();
        }
    }

    public void onHeartBeat(msg_gopro_heartbeat heartBeat) {
        this.scratchSetRequest.target_component = (byte) heartBeat.compid;
        this.scratchGetRequest.target_component = (byte) heartBeat.compid;

        if (status != heartBeat.status) {
            status = heartBeat.status;
            drone.notifyDroneEvent(DroneEventsType.GOPRO_STATUS_UPDATE);

            if (!isConnected()) {
                resetFutures();
            }
        }

        restartWatchdog();
    }

    /**
     * Handles responses from gopro set requests.
     *
     * @param response
     */
    public void onResponseReceived(msg_gopro_set_response response) {
        if (response == null)
            return;

        final SetResponseHandler responseHandler = setResponsesFutures.remove((int) response.cmd_id);
        if (responseHandler != null) {
            responseHandler.onResponse(response.cmd_id, response.result == 1);
        }
    }

    /**
     * Handles responses from gopro get requests.
     *
     * @param response
     */
    public void onResponseReceived(msg_gopro_get_response response) {
        if (response == null)
            return;

        final GetResponseHandler responseHandler = getResponsesFutures.remove((int) response.cmd_id);
        if (responseHandler != null) {
            responseHandler.onResponse(response.cmd_id, response.value);
        }
    }

    private void onHeartbeatTimeout() {
        if (status == GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_DISCONNECTED)
            return;

        status = GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_DISCONNECTED;
        resetFutures();

        drone.notifyDroneEvent(DroneEventsType.GOPRO_STATUS_UPDATE);
    }

    private void restartWatchdog() {
        //re-start watchdog
        watchdog.removeCallbacks(watchdogCallback);
        watchdog.postDelayed(watchdogCallback, HEARTBEAT_TIMEOUT);
    }

    public boolean isConnected() {
        return status == GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_CONNECTED
                || status == GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_RECORDING;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void startRecording() {
        if (!isConnected() || isRecording())
            return;

        //Start recording
        sendSetRequest(GOPRO_COMMAND.GOPRO_COMMAND_SHUTTER, 1, new SetResponseHandler() {
            @Override
            public void onResponse(byte commandId, boolean success) {
                if(success != isRecording) {
                    isRecording = success;
                    drone.notifyDroneEvent(DroneEventsType.GOPRO_STATUS_UPDATE);
                }
            }
        });
    }

    public void stopRecording() {
        if (!isConnected() || !isRecording())
            return;

        //Stop recording
        sendSetRequest(GOPRO_COMMAND.GOPRO_COMMAND_SHUTTER, 0, new SetResponseHandler() {
            @Override
            public void onResponse(byte commandId, boolean success) {
                if(success == isRecording) {
                    isRecording = !success;
                    drone.notifyDroneEvent(DroneEventsType.GOPRO_STATUS_UPDATE);
                }
            }
        });
    }

    private void sendSetRequest(int commandId, int value, SetResponseHandler future) {
        if (future != null)
            setResponsesFutures.put(commandId, future);
        scratchSetRequest.cmd_id = (byte) commandId;
        scratchSetRequest.value = (byte) value;
        sendMavlinkPacket(scratchSetRequest.pack());
    }

    private void sendGetRequest(int commandId, GetResponseHandler future) {
        if (future != null)
            getResponsesFutures.put(commandId, future);

        scratchGetRequest.cmd_id = (byte) commandId;
        sendMavlinkPacket(scratchGetRequest.pack());
    }

    private void sendMavlinkPacket(MAVLinkPacket packet) {
        packet.sysid = 255;
        packet.compid = 0;
        drone.getMavClient().sendMavPacket(packet);
    }

    private void resetFutures() {
        getResponsesFutures.clear();
        setResponsesFutures.clear();
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch (event) {
            case HEARTBEAT_FIRST:
            case HEARTBEAT_RESTORED:
                updateRequestTarget();
                break;
        }
    }

    private void updateRequestTarget() {
        scratchGetRequest.target_component = scratchSetRequest.target_component = drone.getCompid();
        scratchGetRequest.target_system = scratchSetRequest.target_system = drone.getSysid();
    }

    /**
     * Callable used to handle the response from a request.
     */
    private static abstract class GetResponseHandler {

        public abstract void onResponse(byte commandId, byte value);
    }

    private static abstract class SetResponseHandler {
        public abstract void onResponse(byte commandId, boolean success);
    }
}
