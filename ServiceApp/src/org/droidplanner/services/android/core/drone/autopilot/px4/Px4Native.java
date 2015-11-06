package org.droidplanner.services.android.core.drone.autopilot.px4;

import android.content.Context;
import android.os.Handler;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.core.drone.profiles.ParameterManager;
import org.droidplanner.services.android.core.drone.profiles.VehicleProfile;
import org.droidplanner.services.android.core.drone.variables.Camera;
import org.droidplanner.services.android.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.core.drone.variables.Magnetometer;
import org.droidplanner.services.android.core.drone.variables.MissionStats;
import org.droidplanner.services.android.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.mission.Mission;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 9/10/15.
 */
public class Px4Native extends GenericMavLinkDrone {

    public Px4Native(Context context, Handler handler, MAVLinkStreams.MAVLinkOutputStream mavClient, AutopilotWarningParser warningParser, DroneInterfaces.AttributeEventListener listener) {
        super(context, handler, mavClient, warningParser, listener);
    }

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.PX4_NATIVE;
    }

    @Override
    public WaypointManager getWaypointManager() {
        return null;
    }

    @Override
    public Mission getMission() {
        return null;
    }

    @Override
    public MissionStats getMissionStats() {
        return null;
    }

    @Override
    public GuidedPoint getGuidedPoint() {
        return null;
    }

    @Override
    public AccelCalibration getCalibrationSetup() {
        return null;
    }

    @Override
    public MagnetometerCalibrationImpl getMagnetometerCalibration() {
        return null;
    }

    @Override
    public Magnetometer getMagnetometer() {
        return null;
    }

    @Override
    public String getFirmwareVersion() {
        return null;
    }

    @Override
    public Camera getCamera() {
        return null;
    }

    @Override
    public void logMessage(int mavSeverity, String message) {

    }
}
