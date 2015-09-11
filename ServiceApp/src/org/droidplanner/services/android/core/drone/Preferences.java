package org.droidplanner.services.android.core.drone;

import org.droidplanner.services.android.core.drone.profiles.VehicleProfile;
import org.droidplanner.services.android.core.drone.variables.StreamRates;
import org.droidplanner.services.android.core.firmware.FirmwareType;

public interface Preferences {

	public abstract VehicleProfile loadVehicleProfile(FirmwareType firmwareType);

    public StreamRates.Rates getRates();
}
