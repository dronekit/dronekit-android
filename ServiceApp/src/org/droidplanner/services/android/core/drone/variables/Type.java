package org.droidplanner.services.android.core.drone.variables;

import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.core.drone.DroneVariable;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

import com.MAVLink.enums.MAV_TYPE;

public class Type extends DroneVariable implements DroneInterfaces.OnDroneListener{

    private static final int DEFAULT_TYPE = MAV_TYPE.MAV_TYPE_GENERIC;

	private int type = DEFAULT_TYPE;
	private String firmwareVersion = null;

	public Type(MavLinkDrone myDrone) {
		super(myDrone);
        myDrone.addDroneListener(this);
	}

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			myDrone.notifyDroneEvent(DroneEventsType.TYPE);
			myDrone.loadVehicleProfile();
		}
	}

	public int getType() {
		return type;
	}

	public FirmwareType getFirmwareType() {
		if (myDrone.getMavClient().isConnected()) {
			switch (this.type) {

			case MAV_TYPE.MAV_TYPE_FIXED_WING:
				return FirmwareType.ARDU_PLANE;

			case MAV_TYPE.MAV_TYPE_GENERIC:
			case MAV_TYPE.MAV_TYPE_QUADROTOR:
			case MAV_TYPE.MAV_TYPE_COAXIAL:
			case MAV_TYPE.MAV_TYPE_HELICOPTER:
			case MAV_TYPE.MAV_TYPE_HEXAROTOR:
			case MAV_TYPE.MAV_TYPE_OCTOROTOR:
			case MAV_TYPE.MAV_TYPE_TRICOPTER:
				return FirmwareType.ARDU_COPTER;

			case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
			case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
				return FirmwareType.ARDU_ROVER;

			default:
				// unsupported - fall thru to offline condition
			}
		}
		return myDrone.getPreferences().getVehicleType(); // offline or
															// unsupported
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String message) {
        if(firmwareVersion == null || !firmwareVersion.equals(message)) {
            firmwareVersion = message;
            myDrone.notifyDroneEvent(DroneEventsType.FIRMWARE);
        }
	}

    public static boolean isCopter(int type){
        switch (type) {
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return true;

            default:
                return false;
        }
    }

    public static boolean isPlane(int type){
        return type == MAV_TYPE.MAV_TYPE_FIXED_WING;
    }

    public static boolean isRover(int type){
        return type == MAV_TYPE.MAV_TYPE_GROUND_ROVER;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch(event){
            case DISCONNECTED:
                setType(DEFAULT_TYPE);
                break;
        }
    }
}