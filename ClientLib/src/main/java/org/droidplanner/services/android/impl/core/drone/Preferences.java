package org.droidplanner.services.android.impl.core.drone;

import org.droidplanner.services.android.impl.core.drone.profiles.VehicleProfile;
import org.droidplanner.services.android.impl.core.drone.variables.StreamRates;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;

public interface Preferences {

	public abstract VehicleProfile loadVehicleProfile(FirmwareType firmwareType);

    public StreamRates.Rates getRates();
}
