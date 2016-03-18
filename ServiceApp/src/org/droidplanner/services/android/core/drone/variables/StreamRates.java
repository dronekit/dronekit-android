package org.droidplanner.services.android.core.drone.variables;

import org.droidplanner.services.android.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.services.android.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.core.drone.DroneVariable;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public class StreamRates extends DroneVariable<MavLinkDrone> implements OnDroneListener<MavLinkDrone> {

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
		
		public Rates(){}
		
		public Rates(int rate){
			this.extendedStatus = rate;
			this.extra1 = rate;
			this.extra2 = rate;
			this.extra3 = rate;
			this.position = rate;
			this.rcChannels = rate;
			this.rawSensors = rate;
			this.rawController = rate;
		}
    }

}
