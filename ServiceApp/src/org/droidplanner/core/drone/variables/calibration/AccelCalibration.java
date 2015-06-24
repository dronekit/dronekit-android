package org.droidplanner.core.drone.variables.calibration;

import android.os.RemoteException;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_statustext;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.droidplanner.core.MAVLink.MavLinkCalibration;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

import timber.log.Timber;

public class AccelCalibration extends DroneVariable implements DroneInterfaces.OnDroneListener {
    private String mavMsg;
    private boolean calibrating;

    public AccelCalibration(Drone drone) {
        super(drone);
        drone.addDroneListener(this);
    }

    public void startCalibration(final ICommandListener listener) {
        if(calibrating) {
            if(listener != null) {
                try {
                    listener.onSuccess();
                } catch (RemoteException e) {
                    Timber.e(e, e.getMessage());
                }
            }
            return;
        }

        if (myDrone.getState().isFlying()) {
            calibrating = false;
        } else {
            MavLinkCalibration.startAccelerometerCalibration(myDrone, new SimpleCommandListener(){
                @Override
                public void onSuccess(){
                    calibrating = true;
                    mavMsg = "";

                    if(listener != null) {
                        try {
                            listener.onSuccess();
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                }

                @Override
                public void onError(int executionError){
                    if(listener != null){
                        try {
                            listener.onError(executionError);
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                }

                @Override
                public void onTimeout(){
                    if(listener != null){
                        try {
                            listener.onTimeout();
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                }
            });
        }
    }

    public void sendAck(int step) {
        if(calibrating)
            MavLinkCalibration.sendCalibrationAckMessage(myDrone, step);
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
