package org.droidplanner.services.android.impl.core.drone.autopilot.apm;

import android.content.Context;
import android.os.Handler;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.enums.MAV_TYPE;
import com.github.zafarkhaja.semver.Version;
import com.o3dr.android.client.apis.CapabilityApi;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.drone.LogMessageListener;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduPlane extends ArduPilot {

    private static final Version ARDU_PLANE_V3_3 = Version.forIntegers(3,3,0);
    private static final Version ARDU_PLANE_V3_4 = Version.forIntegers(3,4,0);
    private static final Version COMPASS_CALIBRATION_MIN_VERSION = ARDU_PLANE_V3_4;

    public ArduPlane(String droneId, Context context, DataLink.DataLinkProvider<MAVLinkMessage> mavClient, Handler handler, AutopilotWarningParser warningParser, LogMessageListener logListener) {
        super(droneId, context, mavClient, handler, warningParser, logListener);
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
    protected boolean isFeatureSupported(String featureId){
        switch(featureId){
            case CapabilityApi.FeatureIds.COMPASS_CALIBRATION:
                return getFirmwareVersionNumber().greaterThanOrEqualTo(COMPASS_CALIBRATION_MIN_VERSION);

            default:
                return super.isFeatureSupported(featureId);
        }
    }


}
