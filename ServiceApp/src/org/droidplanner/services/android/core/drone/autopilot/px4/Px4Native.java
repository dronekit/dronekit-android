package org.droidplanner.services.android.core.drone.autopilot.px4;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.drone.autopilot.CommonMavLinkDrone;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.core.drone.profiles.Parameters;
import org.droidplanner.services.android.core.drone.profiles.VehicleProfile;
import org.droidplanner.services.android.core.drone.variables.Altitude;
import org.droidplanner.services.android.core.drone.variables.Camera;
import org.droidplanner.services.android.core.drone.variables.GPS;
import org.droidplanner.services.android.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.core.drone.variables.Home;
import org.droidplanner.services.android.core.drone.variables.Magnetometer;
import org.droidplanner.services.android.core.drone.variables.MissionStats;
import org.droidplanner.services.android.core.drone.variables.Navigation;
import org.droidplanner.services.android.core.drone.variables.Orientation;
import org.droidplanner.services.android.core.drone.variables.RC;
import org.droidplanner.services.android.core.drone.variables.State;
import org.droidplanner.services.android.core.drone.variables.StreamRates;
import org.droidplanner.services.android.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.mission.Mission;

/**
 * Created by Fredia Huya-Kouadio on 9/10/15.
 */
public class Px4Native extends CommonMavLinkDrone {

    public Px4Native(DroneInterfaces.Handler handler, MAVLinkStreams.MAVLinkOutputStream mavClient) {
        super(handler, mavClient);
    }

    @Override
    public boolean isConnectionAlive() {
        return false;
    }

    @Override
    public int getMavlinkVersion() {
        return 0;
    }

    @Override
    public GPS getGps() {
        return null;
    }

    @Override
    public byte getSysid() {
        return 0;
    }

    @Override
    public byte getCompid() {
        return 0;
    }

    @Override
    public void onHeartbeat(msg_heartbeat msg_heart) {

    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public Parameters getParameters() {
        return null;
    }

    @Override
    public void setType(int type) {

    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public FirmwareType getFirmwareType() {
        return null;
    }

    @Override
    public void loadVehicleProfile() {

    }

    @Override
    public VehicleProfile getVehicleProfile() {
        return null;
    }

    @Override
    public Preferences getPreferences() {
        return null;
    }

    @Override
    public WaypointManager getWaypointManager() {
        return null;
    }

    @Override
    public Home getHome() {
        return null;
    }

    @Override
    public Altitude getAltitude() {
        return null;
    }

    @Override
    public Orientation getOrientation() {
        return null;
    }

    @Override
    public Navigation getNavigation() {
        return null;
    }

    @Override
    public Mission getMission() {
        return null;
    }

    @Override
    public StreamRates getStreamRates() {
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
    public RC getRC() {
        return null;
    }

    @Override
    public Magnetometer getMagnetometer() {
        return null;
    }

    @Override
    public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed, double climb) {

    }

    @Override
    public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error) {

    }

    @Override
    public String getFirmwareVersion() {
        return null;
    }

    @Override
    public void setFirmwareVersion(String message) {

    }

    @Override
    public Camera getCamera() {
        return null;
    }

    @Override
    public void logMessage(int mavSeverity, String message) {

    }

    @Override
    public void executeAsyncAction(Action action, ICommandListener listener) {

    }
}
