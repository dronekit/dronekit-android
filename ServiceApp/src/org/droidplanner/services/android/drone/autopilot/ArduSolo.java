package org.droidplanner.services.android.drone.autopilot;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.LogMessageListener;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduSolo extends ArduCopter {

    public ArduSolo(MAVLinkStreams.MAVLinkOutputStream mavClient, DroneInterfaces.Clock clock, DroneInterfaces.Handler handler, Preferences pref, AutopilotWarningParser warningParser, LogMessageListener logListener) {
        super(mavClient, clock, handler, pref, warningParser, logListener);
    }
}
