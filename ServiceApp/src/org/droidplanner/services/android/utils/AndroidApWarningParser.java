package org.droidplanner.services.android.utils;

import android.content.Context;

import org.droidplanner.core.drone.variables.Type;
import org.droidplanner.core.model.AutopilotWarningParser;
import org.droidplanner.core.model.Drone;

/**
 * Created by fhuya on 12/16/14.
 */
public class AndroidApWarningParser implements AutopilotWarningParser {

    private final Context context;

    public AndroidApWarningParser(Context context){
        this.context = context;
    }

    @Override
    public String parseWarning(Drone drone, String warning) {
        final int droneType = drone.getType();
        if(Type.isCopter(droneType)){
            return parseCopterWarning(warning);
        }
        else if(Type.isPlane(droneType)){
            return parsePlaneWarning(warning);
        }
        else if(Type.isRover(droneType)){
            return parseRoverWarning(warning);
        }
        else {
            return warning;
        }
    }

    private String parseRoverWarning(String warning) {
        //TODO: complete mapping from ArduRover warnings to DP warnings.
        return warning;
    }

    private String parsePlaneWarning(String warning) {
        //TODO: complete mapping from ArduPlane warnings to DP warnings.
        return warning;
    }

    private String parseCopterWarning(String warning) {
        //TODO: complete mapping from ArduCopter warnings to DP warning
        return warning;
    }
}
