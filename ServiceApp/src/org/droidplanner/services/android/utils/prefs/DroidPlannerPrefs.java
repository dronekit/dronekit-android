package org.droidplanner.services.android.utils.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.StreamRates.Rates;
import org.droidplanner.core.firmware.FirmwareType;
import org.droidplanner.services.android.R;
import org.droidplanner.services.android.utils.file.IO.VehicleProfileReader;

import java.util.UUID;

/**
 * Provides structured access to Droidplanner preferences
 * <p/>
 * Over time it might be good to move the various places that are doing
 * prefs.getFoo(blah, default) here - to collect prefs in one place and avoid
 * duplicating string constants (which tend to become stale as code evolves).
 * This is called the DRY (don't repeat yourself) principle of software
 * development.
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

    @Override
    public Rates getRates() {
        final int defaultRate = 2;

        Rates rates = new Rates();

        rates.extendedStatus = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_ext_stat", "2"));
        rates.extra1 = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra1", "2"));
        rates.extra2 = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra2", "2"));
        rates.extra3 = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra3", "2"));
        rates.position = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_position", "2"));
        rates.rcChannels = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_rc_channels",
        //"2"));
        rates.rawSensors = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_raw_sensors",
        //"2"));
        rates.rawController = defaultRate; //Integer.parseInt(prefs.getString
        // ("pref_mavlink_stream_rate_raw_controller", "2"));

        return rates;
    }

    /**
     * @return true if google analytics reporting is enabled.
     */
    public boolean isUsageStatisticsEnabled() {
        return prefs.getBoolean(context.getString(R.string.pref_usage_statistics_key),
                DEFAULT_USAGE_STATISTICS);
    }

}
