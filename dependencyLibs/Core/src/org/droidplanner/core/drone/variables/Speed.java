package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

public class Speed extends DroneVariable {
	public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
	public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
	public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;

	private double verticalSpeed = 0;
	private double groundSpeed = 0;
	private double airSpeed = 0;
	private double targetSpeed = 0;

	public Speed(Drone myDrone) {
		super(myDrone);
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	public double getGroundSpeed() {
		return groundSpeed;
	}

	public double getAirSpeed() {
		return airSpeed;
	}

	public double getTargetSpeed() {
		return targetSpeed;
	}

	public void setSpeedError(double aspd_error) {
		targetSpeed = (aspd_error + airSpeed);
	}

	public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed, double climb) {
        boolean speedUpdated = false;
        if(this.groundSpeed != groundSpeed) {
            this.groundSpeed = groundSpeed;
            speedUpdated = true;
        }

        if(this.airSpeed != airSpeed){
            this.airSpeed = airSpeed;
            speedUpdated = true;
        }

        if(this.verticalSpeed != climb){
            this.verticalSpeed = climb;
            speedUpdated = true;
        }

        if(speedUpdated) {
            myDrone.notifyDroneEvent(DroneInterfaces.DroneEventsType.SPEED);
        }

		checkCollisionIsImminent();
	}

	public double getSpeedParameter(){
		Parameter param = myDrone.getParameters().getParameter("WPNAV_SPEED");
		if (param == null ) {
			return -1;
		}else{
			return (param.value/100);
		}
			
	}
	
	/**
	 * if drone will crash in 2 seconds at constant climb rate and climb rate <
	 * -3 m/s and altitude > 1 meter
	 */
	private void checkCollisionIsImminent() {

		double altitude = myDrone.getAltitude().getAltitude();
		if (altitude + (verticalSpeed * COLLISION_SECONDS_BEFORE_COLLISION) < 0
				&& verticalSpeed < COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND
				&& altitude > COLLISION_SAFE_ALTITUDE_METERS) {
			myDrone.getAltitude().setCollisionImminent(true);
		} else {
			myDrone.getAltitude().setCollisionImminent(false);
		}
	}

}
