package org.droidplanner.services.android.utils.prefs;

import java.util.UUID;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.utils.file.IO.VehicleProfileReader;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.firmware.FirmwareType;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Provides structured access to Droidplanner preferences
 * 
 * Over time it might be good to move the various places that are doing
 * prefs.getFoo(blah, default) here - to collect prefs in one place and avoid
 * duplicating string constants (which tend to become stale as code evolves).
 * This is called the DRY (don't repeat yourself) principle of software
 * development.
 * 
 * 
 */
public class DroidPlannerPrefs implements org.droidplanner.core.drone.Preferences {

	/*
	 * Default preference value
	 */
	public static final boolean DEFAULT_USAGE_STATISTICS = true;

	// Public for legacy usage
	public SharedPreferences prefs;
	private Context context;

	public DroidPlannerPrefs(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Return a unique ID for the vehicle controlled by this tablet. FIXME,
	 * someday let the users select multiple vehicles
	 */
	public String getVehicleId() {
		String r = prefs.getString("vehicle_id", "").trim();

		// No ID yet - pick one
		if (r.isEmpty()) {
			r = UUID.randomUUID().toString();

			prefs.edit().putString("vehicle_id", r).apply();
		}
		return r;
	}

	@Override
	public FirmwareType getVehicleType() {
		String str = prefs.getString("pref_vehicle_type", FirmwareType.ARDU_COPTER.toString());
		return FirmwareType.firmwareFromString(str);
	}

	@Override
	public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
		return VehicleProfileReader.load(context, firmwareType);
	}

	/**
	 * @return true if google analytics reporting is enabled.
	 */
	public boolean isUsageStatisticsEnabled() {
		return prefs.getBoolean(context.getString(R.string.pref_usage_statistics_key),
				DEFAULT_USAGE_STATISTICS);
	}

}
