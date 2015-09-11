package org.droidplanner.services.android.core.drone.autopilot.apm;

import android.content.Context;

import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_TYPE;

import org.droidplanner.services.android.core.MAVLink.MAVLinkStreams;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.LogMessageListener;
import org.droidplanner.services.android.core.drone.Preferences;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduPlane extends ArduPilot {

    public ArduPlane(Context context, MAVLinkStreams.MAVLinkOutputStream mavClient, DroneInterfaces.Handler handler, Preferences pref, AutopilotWarningParser warningParser, LogMessageListener logListener, DroneInterfaces.AttributeEventListener listener) {
        super(context, mavClient, handler, pref, warningParser, logListener, listener);
    }

    @Override
    public int getType(){
        return MAV_TYPE.MAV_TYPE_FIXED_WING;
    }

    @Override
    protected void setType(int type){}

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.ARDU_PLANE;
    }

    @Override
    protected void processVfrHud(msg_vfr_hud vfrHud){
        //Nothing to do. Plane used GLOBAL_POSITION_INT to set altitude and speeds unlike copter
    }

    /**
     * Used to update the vehicle location, and altitude.
     * @param gpi
     */
    @Override
    protected void processGlobalPositionInt(msg_global_position_int gpi){
        if(gpi == null)
            return;

        super.processGlobalPositionInt(gpi);

        final double relativeAlt = gpi.relative_alt / 1000.0;

        final double groundSpeedX = gpi.vx / 100.0;
        final double groundSpeedY = gpi.vy / 100.0;
        final double groundSpeed = Math.sqrt(Math.pow(groundSpeedX, 2) + Math.pow(groundSpeedY, 2));

        final double climbRate = gpi.vz / 100.0;
        setAltitudeGroundAndAirSpeeds(relativeAlt, groundSpeed, groundSpeed, climbRate);
    }


}
