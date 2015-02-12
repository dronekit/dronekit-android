package org.droidplanner.core.drone.variables;

import org.droidplanner.core.MAVLink.MavLinkModes;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.AutopilotWarningParser;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.ApmModes;

public class State extends DroneVariable {
	private static final long failsafeOnScreenTimeout = 5000;
	private String warning = "";
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;
    private final AutopilotWarningParser warningParser;

	// flightTimer
	// ----------------
	private long startTime = 0;
	private Clock clock;

	public Handler watchdog;
	public Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			removeWarning();
		}
	};

	public State(Drone myDrone, Clock clock, Handler handler, AutopilotWarningParser warningParser) {
		super(myDrone);
		this.clock = clock;
		this.watchdog = handler;
        this.warningParser = warningParser;
		resetFlightStartTime();
	}

	public boolean isWarning() {
		return !warning.equals("");
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

	public String getWarning() {
		return warning;
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

	public void setWarning(String newFailsafe) {
        String parsedWarning = warningParser.parseWarning(myDrone, newFailsafe);
		if (!this.warning.equals(parsedWarning)) {
			this.warning = parsedWarning;
			myDrone.notifyDroneEvent(DroneEventsType.AUTOPILOT_WARNING);
		}
		watchdog.removeCallbacks(watchdogCallback);
		this.watchdog.postDelayed(watchdogCallback, failsafeOnScreenTimeout);
	}

    public void repeatWarning(){
        if(warning == null || warning.length() == 0)
            return;

        watchdog.removeCallbacks(watchdogCallback);
        this.watchdog.postDelayed(watchdogCallback, failsafeOnScreenTimeout);
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

	public void doTakeoff(Altitude alt) {
		myDrone.getGuidedPoint().doGuidedTakeoff(alt);
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

	protected void removeWarning() {
		setWarning("");
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