package org.droidplanner.services.android.core.drone.variables;

import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.DroneVariable;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public class Altitude extends DroneVariable {
	private double altitude = 0;
	private double targetAltitude = 0;

	public Altitude(MavLinkDrone myDrone) {
		super(myDrone);
	}

	public double getAltitude() {
		return altitude;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public void setAltitude(double altitude) {
        if(this.altitude != altitude) {
            this.altitude = altitude;
            myDrone.notifyDroneEvent(DroneInterfaces.DroneEventsType.ALTITUDE);
        }
	}

	public void setAltitudeError(double alt_error) {
		targetAltitude = alt_error + altitude;
	}

}