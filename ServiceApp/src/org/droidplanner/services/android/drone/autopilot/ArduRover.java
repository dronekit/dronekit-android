package org.droidplanner.services.android.drone.autopilot;

import android.content.Context;

import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.LogMessageListener;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduRover extends ArduPilot {

    public ArduRover(Context context, MAVLinkStreams.MAVLinkOutputStream mavClient, DroneInterfaces.Handler handler, Preferences pref, AutopilotWarningParser warningParser, LogMessageListener logListener) {
        super(context, mavClient, handler, pref, warningParser, logListener);
    }
}
