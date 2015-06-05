package org.droidplanner.core.drone.variables.calibration;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_statustext;

import org.droidplanner.core.MAVLink.MavLinkCalibration;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

public class AccelCalibration extends DroneVariable implements DroneInterfaces.OnDroneListener {
    private String mavMsg;
    private boolean calibrating;

    public AccelCalibration(Drone drone) {
        super(drone);
        drone.addDroneListener(this);
    }

    public boolean startCalibration() {
        if(calibrating)
            return true;

        if (myDrone.getState().isFlying()) {
            calibrating = false;
        } else {
            calibrating = true;
            mavMsg = "";
            MavLinkCalibration.startAccelerometerCalibration(myDrone);
        }
        return calibrating;
    }

    public void sendAck(int step) {
        if(calibrating)
            MavLinkCalibration.sendCalibrationAckMessage(step, myDrone);
    }

    public void processMessage(MAVLinkMessage msg) {
        if (calibrating && msg.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT) {
            msg_statustext statusMsg = (msg_statustext) msg;
            final String message = statusMsg.getText();

            if (message != null && (message.startsWith("Place vehicle") || message.startsWith("Calibration"))) {
                mavMsg = message;
                if (message.startsWith("Calibration"))
                    calibrating = false;

                myDrone.notifyDroneEvent(DroneEventsType.CALIBRATION_IMU);
            }
        }
    }

    public String getMessage() {
        return mavMsg;
    }

    public boolean isCalibrating() {
        return calibrating;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch (event) {
            case HEARTBEAT_TIMEOUT:
            case DISCONNECTED:
                if (calibrating)
                    cancelCalibration();
                break;
        }
    }

    public void cancelCalibration() {
        mavMsg = "";
        calibrating = false;
    }
}
