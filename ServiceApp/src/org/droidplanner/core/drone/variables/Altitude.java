package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

public class Altitude extends DroneVariable {
	private double altitude = 0;
	private double targetAltitude = 0;

	public Altitude(Drone myDrone) {
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