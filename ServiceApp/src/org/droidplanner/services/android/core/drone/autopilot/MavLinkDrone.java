package org.droidplanner.services.android.core.drone.autopilot;

import com.MAVLink.Messages.MAVLinkMessage;

import org.droidplanner.services.android.communication.model.DataStreams;
import org.droidplanner.services.android.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.profiles.ParameterManager;
import org.droidplanner.services.android.core.drone.variables.Camera;
import org.droidplanner.services.android.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.core.drone.variables.MissionStats;
import org.droidplanner.services.android.core.drone.variables.State;
import org.droidplanner.services.android.core.drone.variables.StreamRates;
import org.droidplanner.services.android.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.mission.Mission;

public interface MavLinkDrone extends Drone {

    String PACKAGE_NAME = "org.droidplanner.services.android.core.drone.autopilot";

    String ACTION_REQUEST_HOME_UPDATE = PACKAGE_NAME + ".action.REQUEST_HOME_UPDATE";

    boolean isConnectionAlive();

    int getMavlinkVersion();

    void onMavLinkMessageReceived(MAVLinkMessage message);

    public void addDroneListener(DroneInterfaces.OnDroneListener listener);

    public void removeDroneListener(DroneInterfaces.OnDroneListener listener);

    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event);

    public byte getSysid();

    public byte getCompid();

    public State getState();

    public ParameterManager getParameterManager();

    public int getType();

    public FirmwareType getFirmwareType();

    public DataStreams.DataOutputStream<MAVLinkMessage> getMavClient();

    public WaypointManager getWaypointManager();

    public Mission getMission();

    public StreamRates getStreamRates();

    public MissionStats getMissionStats();

    public GuidedPoint getGuidedPoint();

    public AccelCalibration getCalibrationSetup();

    public MagnetometerCalibrationImpl getMagnetometerCalibration();

    public String getFirmwareVersion();

    public Camera getCamera();

}
