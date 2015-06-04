package org.droidplanner.core.drone.variables;

import org.droidplanner.core.MAVLink.MavLinkModes;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.AutopilotWarningParser;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.enums.EKF_STATUS_FLAGS;

public class State extends DroneVariable {
	private static final long ERROR_TIMEOUT = 5000l;

    private final AutopilotWarningParser warningParser;

	private msg_ekf_status_report ekfStatus;
	private boolean isEkfPositionOk;

	private String errorId;
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;

	// flightTimer
	// ----------------
	private long startTime = 0;
	private final Clock clock;

	private final Handler watchdog;
	private final Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			resetWarning();
		}
	};

	public State(Drone myDrone, Clock clock, Handler handler, AutopilotWarningParser warningParser) {
		super(myDrone);
		this.clock = clock;
		this.watchdog = handler;
        this.warningParser = warningParser;
        this.errorId = warningParser.getDefaultWarning();
		resetFlightStartTime();
	}

	public boolean isArmed() {
		return armed;
	}

	public boolean isFlying() {
		return isFlying;
	}

	public ApmModes getMode() {
		return mode;
	}

	public String getErrorId() {
		return errorId;
	}

	public void setIsFlying(boolean newState) {
		if (newState != isFlying) {
			isFlying = newState;
			myDrone.notifyDroneEvent(DroneEventsType.STATE);

			if (isFlying) {
				resetFlightStartTime();
			}
		}
	}

    public boolean parseAutopilotError(String errorMsg){
        String parsedError = warningParser.parseWarning(myDrone, errorMsg);
        if(parsedError == null || parsedError.trim().isEmpty())
            return false;

        if (!parsedError.equals(this.errorId)) {
            this.errorId = parsedError;
            myDrone.notifyDroneEvent(DroneEventsType.AUTOPILOT_WARNING);
        }

        watchdog.removeCallbacks(watchdogCallback);
        this.watchdog.postDelayed(watchdogCallback, ERROR_TIMEOUT);
        return true;
    }

    public void repeatWarning(){
        if(errorId == null || errorId.length() == 0 || errorId.equals(warningParser.getDefaultWarning()))
            return;

        watchdog.removeCallbacks(watchdogCallback);
        this.watchdog.postDelayed(watchdogCallback, ERROR_TIMEOUT);
    }

	public void setArmed(boolean newState) {
		if (this.armed != newState) {
			this.armed = newState;
			myDrone.notifyDroneEvent(DroneEventsType.ARMING);

			if (newState) {
				myDrone.getWaypointManager().getWaypoints();
			}else{
				if (mode == ApmModes.ROTOR_RTL || mode == ApmModes.ROTOR_LAND) {
					changeFlightMode(ApmModes.ROTOR_LOITER);  // When disarming set the mode back to loiter so we can do a takeoff in the future.					
				}
			}
		}

		checkEkfPositionState(this.ekfStatus);
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			myDrone.notifyDroneEvent(DroneEventsType.MODE);
		}
	}

	public void changeFlightMode(ApmModes mode) {
		if (ApmModes.isValid(mode)) {
			MavLinkModes.changeFlightMode(myDrone, mode);
		}
	}

	private void resetWarning() {
		String defaultWarning = warningParser.getDefaultWarning();
        if(defaultWarning == null)
            defaultWarning = "";

        if (!defaultWarning.equals(this.errorId)) {
            this.errorId = defaultWarning;
            myDrone.notifyDroneEvent(DroneEventsType.AUTOPILOT_WARNING);
        }
	}

	// flightTimer
	// ----------------

	private void resetFlightStartTime() {
		startTime = clock.elapsedRealtime();
	}

	public long getFlightStartTime() {
        return startTime;
	}

	public msg_ekf_status_report getEkfStatus() {
		return ekfStatus;
	}

	public void setEkfStatus(msg_ekf_status_report ekfState) {
		if(this.ekfStatus == null || !areEkfStatusEquals(this.ekfStatus, ekfState)) {
			this.ekfStatus = ekfState;
			myDrone.notifyDroneEvent(DroneEventsType.EKF_STATUS_UPDATE);
		}
	}

	private void checkEkfPositionState(msg_ekf_status_report ekfStatus){
		if(ekfStatus == null)
			return;

		final short flags = ekfStatus.flags;

		final boolean isOk = this.armed
				? (flags & EKF_STATUS_FLAGS.EKF_POS_HORIZ_ABS) != 0
				&& (flags & EKF_STATUS_FLAGS.EKF_CONST_POS_MODE) == 0
				: (flags & EKF_STATUS_FLAGS.EKF_POS_HORIZ_ABS) != 0
				|| (flags & EKF_STATUS_FLAGS.EKF_PRED_POS_HORIZ_ABS) != 0;

		if(isEkfPositionOk != isOk){
			isEkfPositionOk = isOk;
			myDrone.notifyDroneEvent(DroneEventsType.EKF_POSITION_STATE_UPDATE);
		}
	}

	private static boolean areEkfStatusEquals(msg_ekf_status_report one, msg_ekf_status_report two) {
        return one == two || !(one == null || two == null) && one.toString().equals(two.toString());
    }

	public boolean isEkfPositionOk() {
		return isEkfPositionOk;
	}
}