package org.droidplanner.services.android.impl.core.drone.autopilot;

import com.MAVLink.Messages.MAVLinkMessage;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.impl.core.drone.profiles.ParameterManager;
import org.droidplanner.services.android.impl.core.drone.variables.Camera;
import org.droidplanner.services.android.impl.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.impl.core.drone.variables.MissionStats;
import org.droidplanner.services.android.impl.core.drone.variables.State;
import org.droidplanner.services.android.impl.core.drone.variables.StreamRates;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.core.mission.MissionImpl;

import com.o3dr.services.android.lib.coordinate.*;

public interface MavLinkDrone extends Drone {

    String PACKAGE_NAME = "org.droidplanner.services.android.core.drone.autopilot";

    String ACTION_REQUEST_HOME_UPDATE = PACKAGE_NAME + ".action.REQUEST_HOME_UPDATE";

    boolean isConnectionAlive();

    int getMavlinkVersion();

    void onMavLinkMessageReceived(MAVLinkMessage message);

    short getSysid();

    short getCompid();

    State getState();

    Frame getFrame();

    void setFrame(Frame frame);

    ParameterManager getParameterManager();

    int getType();

    FirmwareType getFirmwareType();

    DataLink.DataLinkProvider<MAVLinkMessage> getMavClient();

    WaypointManager getWaypointManager();

    MissionImpl getMission();

    StreamRates getStreamRates();

    MissionStats getMissionStats();

    GuidedPoint getGuidedPoint();

    AccelCalibration getCalibrationSetup();

    MagnetometerCalibrationImpl getMagnetometerCalibration();

    String getFirmwareVersion();

    Camera getCamera();

}
