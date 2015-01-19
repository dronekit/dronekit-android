package com.o3dr.services.android.lib.drone.mission.action;

import android.os.Bundle;

import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class MissionActions {

    public static final String ACTION_GENERATE_DRONIE = Utils.PACKAGE_NAME + ".action.GENERATE_DRONIE";

    public static final String ACTION_SET_MISSION = Utils.PACKAGE_NAME + ".action.SET_MISSION";

    public static final String EXTRA_MISSION = "extra_mission";
    public static final String EXTRA_PUSH_TO_DRONE = "extra_push_to_drone";

    public static final String ACTION_LOAD_WAYPOINTS = Utils.PACKAGE_NAME + ".action.LOAD_WAYPOINTS";

    /**
     * Generate action to create a dronie mission, and upload it to the connected drone.
     */
    public static Action buildDronie(){
        return new Action(ACTION_GENERATE_DRONIE);
    }

    /**
     * Generate action to update the mission property for the drone model in memory.
     * @param mission mission to upload to the drone.
     * @param pushToDrone if true, upload the mission to the connected device.
     */
    public static Action buildMission(Mission mission, boolean pushToDrone){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MISSION, mission);
        params.putBoolean(EXTRA_PUSH_TO_DRONE, pushToDrone);
        return new Action(ACTION_SET_MISSION, params);
    }

    public static Action buildWaypointsLoader(){
        return new Action(ACTION_LOAD_WAYPOINTS);
    }
}
