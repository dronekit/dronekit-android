package org.droidplanner.services.android.core.drone.autopilot;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.drone.profiles.Parameters;
import org.droidplanner.services.android.core.drone.profiles.VehicleProfile;
import org.droidplanner.services.android.core.drone.variables.Altitude;
import org.droidplanner.services.android.core.drone.variables.Battery;
import org.droidplanner.services.android.core.drone.variables.Camera;
import org.droidplanner.services.android.core.drone.variables.GPS;
import org.droidplanner.services.android.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.core.drone.variables.Home;
import org.droidplanner.services.android.core.drone.variables.Magnetometer;
import org.droidplanner.services.android.core.drone.variables.MissionStats;
import org.droidplanner.services.android.core.drone.variables.Navigation;
import org.droidplanner.services.android.core.drone.variables.Orientation;
import org.droidplanner.services.android.core.drone.variables.RC;
import org.droidplanner.services.android.core.drone.variables.Speed;
import org.droidplanner.services.android.core.drone.variables.State;
import org.droidplanner.services.android.core.drone.variables.StreamRates;
import org.droidplanner.services.android.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.mission.Mission;

public interface MavLinkDrone extends Drone {

    boolean isConnectionAlive();

    int getMavlinkVersion();

    void onMavLinkMessageReceived(MAVLinkMessage message);

    public void addDroneListener(DroneInterfaces.OnDroneListener listener);

    public void removeDroneListener(DroneInterfaces.OnDroneListener listener);

    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event);

    public GPS getGps();

    public byte getSysid();

    public byte getCompid();

    public void onHeartbeat(msg_heartbeat msg_heart);

    public State getState();

    public Parameters getParameters();

    public void setType(int type);

    public int getType();

    public FirmwareType getFirmwareType();

    public void loadVehicleProfile();

    public VehicleProfile getVehicleProfile();

    public MAVLinkStreams.MAVLinkOutputStream getMavClient();

    public Preferences getPreferences();

    public WaypointManager getWaypointManager();

    public Speed getSpeed();

    public Battery getBattery();

    public Home getHome();

    public Altitude getAltitude();

    public Orientation getOrientation();

    public Navigation getNavigation();

    public Mission getMission();

    public StreamRates getStreamRates();

    public MissionStats getMissionStats();

    public GuidedPoint getGuidedPoint();

    public AccelCalibration getCalibrationSetup();

    public MagnetometerCalibrationImpl getMagnetometerCalibration();

    public RC getRC();

    public Magnetometer getMagnetometer();

    public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed, double climb);

    public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error);

    public String getFirmwareVersion();

    public void setFirmwareVersion(String message);

    public Camera getCamera();

    public void logMessage(int mavSeverity, String message);

}
