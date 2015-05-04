package org.droidplanner.core.drone.variables.calibration;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;

import org.droidplanner.core.MAVLink.MavLinkCalibration;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Fredia Huya-Kouadio on 5/3/15.
 */
public class MagnetometerCalibration extends DroneVariable {

    public interface OnMagnetometerCalibrationListener {
        void onCalibrationCancelled();

        void onCalibrationProgress(msg_mag_cal_progress progress);

        void onCalibrationReport(msg_mag_cal_report report);

        void onCalibrationCompleted();

        void onCalibrationError(String error);
    }

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isWaitingForConfirmation = new AtomicBoolean(false);

    private OnMagnetometerCalibrationListener listener;

    public MagnetometerCalibration(Drone myDrone) {
        super(myDrone);
    }

    public void setListener(OnMagnetometerCalibrationListener listener) {
        this.listener = listener;
    }

    public void startCalibration(boolean retryOnFailure, boolean saveAutomatically, int startDelay){
        MavLinkCalibration.startMagnetometerCalibration(myDrone, retryOnFailure, saveAutomatically, startDelay);
    }

    public void cancelCalibration(){
        if(isRunning.compareAndSet(true, false)){
            MavLinkCalibration.cancelMagnetometerCalibration(myDrone);
            if(listener != null)
                listener.onCalibrationCancelled();
        }
    }

    public void acceptCalibration(){
        if(isWaitingForConfirmation.compareAndSet(true, false)){
            isRunning.set(false);

            MavLinkCalibration.acceptMagnetometerCalibration(myDrone);

            if(listener != null)
                listener.onCalibrationCompleted();
        }
    }

    public void processCalibrationMessage(MAVLinkMessage message){
        if(listener == null)
            return;

        switch(message.msgid){
            case msg_mag_cal_progress.MAVLINK_MSG_ID_MAG_CAL_PROGRESS:
                listener.onCalibrationProgress((msg_mag_cal_progress) message);
                break;

            case msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT:
                listener.onCalibrationReport((msg_mag_cal_report) message);
                break;
        }
    }
}
