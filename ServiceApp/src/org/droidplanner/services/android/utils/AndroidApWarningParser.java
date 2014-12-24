package org.droidplanner.services.android.utils;

import android.content.Context;

import org.droidplanner.core.drone.variables.Type;
import org.droidplanner.core.model.AutopilotWarningParser;
import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.R;

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

    /**
     * Maps the ArduRover warnings set to the 3DR Services warnings set.
     * @param warning warning originating from the ArduRover autopilot
     * @return equivalent 3DR Services warning
     */
    private String parseRoverWarning(String warning) {
        switch(warning){
            case "Arm: Thr below FS":
                return context.getString(R.string.warning_arm_thr_below_fs);

            default:
                return warning;
        }
    }

    /**
     * Maps the ArduPlane warnings set to the 3DR Services warnings set.
     * @param warning warning originating from the ArduPlane autopilot
     * @return equivalent 3DR Services warning
     */
    private String parsePlaneWarning(String warning) {
        switch(warning){
            case "Arm: Thr below FS":
                return context.getString(R.string.warning_arm_thr_below_fs);

            default:
                return warning;
        }
    }

    /**
     * Maps the ArduCopter warnings set to the 3DR Services warnings set.
     * @param warning warning originating from the ArduCopter autopilot
     * @return equivalent 3DR Services warning
     */
    private String parseCopterWarning(String warning) {
        switch(warning){
            case "Arm: Thr below FS":
                return context.getString(R.string.warning_arm_thr_below_fs);

            default:
                return warning;
        }
    }
}
