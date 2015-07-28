package org.droidplanner.core.drone.variables;

import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.services.android.drone.autopilot.MavLinkDrone;

public class StreamRates extends DroneVariable implements OnDroneListener {

    private Rates rates;

	public StreamRates(MavLinkDrone myDrone) {
		super(myDrone);
		myDrone.addDroneListener(this);
	}

    public void setRates(Rates rates) {
        this.rates = rates;
    }

    @Override
	public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
		switch (event) {
        case CONNECTED:
		case HEARTBEAT_FIRST:
		case HEARTBEAT_RESTORED:
			setupStreamRatesFromPref();
			break;
		default:
			break;
		}
	}

	public void setupStreamRatesFromPref() {
        if(rates == null)
            return;

		MavLinkStreamRates.setupStreamRates(myDrone.getMavClient(), myDrone.getSysid(),
				myDrone.getCompid(), rates.extendedStatus, rates.extra1, rates.extra2,
				rates.extra3, rates.position, rates.rcChannels, rates.rawSensors,
				rates.rawController);
	}

    public static class Rates {
        public int extendedStatus;
        public int extra1;
        public int extra2;
        public int extra3;
        public int position;
        public int rcChannels;
        public int rawSensors;
        public int rawController;
    }

}
