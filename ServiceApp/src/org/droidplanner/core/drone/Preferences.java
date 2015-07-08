package org.droidplanner.core.drone;

import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.StreamRates;
import org.droidplanner.core.firmware.FirmwareType;

public interface Preferences {

	public abstract FirmwareType getVehicleType();

	public abstract VehicleProfile loadVehicleProfile(FirmwareType firmwareType);

    public StreamRates.Rates getRates();
}
