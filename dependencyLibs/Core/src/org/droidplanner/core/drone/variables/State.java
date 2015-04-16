package org.droidplanner.core.drone.variables;

import org.droidplanner.core.MAVLink.MavLinkModes;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.AutopilotWarningParser;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.ApmModes;

public class State extends DroneVariable {
	private static final long ERROR_ON_SCREEN_TIMEOUT = 5000;

    private final AutopilotWarningParser warningParser;

	private String errorType;
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;

	// flightTimer
	// ----------------
	private long startTime = 0;
	private final Clock clock;

	public final Handler watchdog;
	public final Runnable watchdogCallback = new Runnable() {
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
        this.errorType = warningParser.getDefaultWarning();
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

	public String getErrorType() {
		return errorType;
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

        if (!parsedError.equals(this.errorType)) {
            this.errorType = parsedError;
            myDrone.notifyDroneEvent(DroneEventsType.AUTOPILOT_WARNING);
        }

        watchdog.removeCallbacks(watchdogCallback);
        this.watchdog.postDelayed(watchdogCallback, ERROR_ON_SCREEN_TIMEOUT);
        return true;
    }

    public void repeatWarning(){
        if(errorType == null || errorType.length() == 0 || errorType.equals(warningParser.getDefaultWarning()))
            return;

        watchdog.removeCallbacks(watchdogCallback);
        this.watchdog.postDelayed(watchdogCallback, ERROR_ON_SCREEN_TIMEOUT);
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

	protected void resetWarning() {
		String defaultWarning = warningParser.getDefaultWarning();
        if(defaultWarning == null)
            defaultWarning = "";

        if (!defaultWarning.equals(this.errorType)) {
            this.errorType = defaultWarning;
            myDrone.notifyDroneEvent(DroneEventsType.AUTOPILOT_WARNING);
        }
	}

	// flightTimer
	// ----------------

	public void resetFlightStartTime() {
		startTime = clock.elapsedRealtime();
	}

	public long getFlightStartTime() {
        return startTime;
	}

}