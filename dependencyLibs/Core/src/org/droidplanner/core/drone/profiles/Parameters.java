package org.droidplanner.core.drone.profiles;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_param_value;

import org.droidplanner.core.MAVLink.MavLinkParameters;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to manage the communication of parameters to the MAV.
 * <p/>
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 */
public class Parameters extends DroneVariable implements OnDroneListener {

    private static final int TIMEOUT = 1000; //milliseconds

    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private int expectedParams;

    private final Map<Integer, Boolean> paramsRollCall = new HashMap<>();
    private final ConcurrentHashMap<String, Parameter> parameters = new ConcurrentHashMap<>();

    private DroneInterfaces.OnParameterManagerListener parameterListener;

    public Handler watchdog;
    public Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            onParameterStreamStopped();
        }
    };

    public Parameters(Drone myDrone, Handler handler) {
        super(myDrone);
        this.watchdog = handler;
        myDrone.addDroneListener(this);
    }

    public void refreshParameters() {
        if (isRefreshing.compareAndSet(false, true)) {
            expectedParams = 0;
            parameters.clear();
            paramsRollCall.clear();

            if (parameterListener != null)
                parameterListener.onBeginReceivingParameters();

            MavLinkParameters.requestParametersList(myDrone);
            resetWatchdog();
        }
    }

    public Map<String, Parameter> getParameters() {
        //Update the cache if it's stale. Parameters download is expensive, but we assume the caller knows what it's
        // doing.
        if (parameters.isEmpty())
            refreshParameters();

        return parameters;
    }

    /**
     * Try to process a Mavlink message if it is a parameter related message
     *
     * @param msg Mavlink message to process
     * @return Returns true if the message has been processed
     */
    public boolean processMessage(MAVLinkMessage msg) {
        if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
            processReceivedParam((msg_param_value) msg);
            return true;
        }
        return false;
    }

    private void processReceivedParam(msg_param_value m_value) {
        // collect params in parameter list
        Parameter param = new Parameter(m_value);

        parameters.put(param.name.toLowerCase(Locale.US), param);
        final int paramIndex = m_value.param_index;
        if (paramIndex == -1) {
            // update listener
            if (parameterListener != null)
                parameterListener.onParameterReceived(param, 0, 1);

            myDrone.notifyDroneEvent(DroneEventsType.PARAMETERS_DOWNLOADED);

            if (parameterListener != null) {
                parameterListener.onEndReceivingParameters();
            }
            return;
        }

        paramsRollCall.put(paramIndex, true);
        expectedParams = m_value.param_count;

        // update listener
        if (parameterListener != null)
            parameterListener.onParameterReceived(param, paramIndex, m_value.param_count);

        // Are all parameters here? Notify the listener with the parameters
        if (parameters.size() >= m_value.param_count) {
            killWatchdog();
            isRefreshing.set(false);
            myDrone.notifyDroneEvent(DroneEventsType.PARAMETERS_DOWNLOADED);

            if (parameterListener != null) {
                parameterListener.onEndReceivingParameters();
            }
        } else {
            resetWatchdog();
        }
    }

    private void reRequestMissingParams(int howManyParams) {
        for (int i = 0; i < howManyParams; i++) {
            Boolean isPresent = paramsRollCall.get(i);
            if (isPresent == null || !isPresent) {
                MavLinkParameters.readParameter(myDrone, i);
            }
        }
    }

    public void sendParameter(Parameter parameter) {
        MavLinkParameters.sendParameter(myDrone, parameter);
    }

    public void readParameter(String name) {
        MavLinkParameters.readParameter(myDrone, name);
    }

    public Parameter getParameter(String name) {
        if (name == null || name.length() == 0)
            return null;

        return parameters.get(name.toLowerCase(Locale.US));
    }

    private void onParameterStreamStopped() {
        if (expectedParams > 0) {
            reRequestMissingParams(expectedParams);
            resetWatchdog();
        } else {
            isRefreshing.set(false);
        }
    }

    private void resetWatchdog() {
        watchdog.removeCallbacks(watchdogCallback);
        watchdog.postDelayed(watchdogCallback, TIMEOUT);
    }

    private void killWatchdog() {
        watchdog.removeCallbacks(watchdogCallback);
        isRefreshing.set(false);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch (event) {
            case HEARTBEAT_FIRST:
                if (!drone.getState().isFlying()) {
                    refreshParameters();
                }
                break;

            case DISCONNECTED:
            case HEARTBEAT_TIMEOUT:
                killWatchdog();
                break;
            default:
                break;

        }
    }

    public void setParameterListener(DroneInterfaces.OnParameterManagerListener parameterListener) {
        this.parameterListener = parameterListener;
    }
}
