package com.o3dr.android.client.apis.mission;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.model.action.Action;

import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_BUILD_COMPLEX_MISSION_ITEM;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_GENERATE_DRONIE;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_LOAD_WAYPOINTS;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_SET_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_PUSH_TO_DRONE;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class MissionApi {

    /**
     * Generate action to create a dronie mission, and upload it to the connected drone.
     */
    public static void generateDronie(Drone drone) {
        drone.performAsyncAction(new Action(ACTION_GENERATE_DRONIE));
    }

    /**
     * Generate action to update the mission property for the drone model in memory.
     *
     * @param mission     mission to upload to the drone.
     * @param pushToDrone if true, upload the mission to the connected device.
     */
    public static void setMission(Drone drone, Mission mission, boolean pushToDrone) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MISSION, mission);
        params.putBoolean(EXTRA_PUSH_TO_DRONE, pushToDrone);
        drone.performAsyncAction(new Action(ACTION_SET_MISSION, params));
    }

    public static void loadWaypoints(Drone drone) {
        drone.performAsyncAction(new Action(ACTION_LOAD_WAYPOINTS));
    }

    /**
     * Build and return complex mission item.
     * @param itemBundle bundle containing the complex mission item to update.
     */
    private static Action buildComplexMissionItem(Drone drone, Bundle itemBundle) {
        Action payload = new Action(ACTION_BUILD_COMPLEX_MISSION_ITEM, itemBundle);
        boolean result = drone.performAction(payload);
        if(result)
            return payload;
        else
            return null;
    }

    public static <T extends MissionItem> T buildMissionItem(Drone drone,
                                                                      MissionItem.ComplexItem<T> complexItem){
        T missionItem = (T) complexItem;
        Bundle payload = missionItem.getType().storeMissionItem(missionItem);
        if (payload == null)
            return null;

        Action result = buildComplexMissionItem(drone, payload);
        if(result != null){
            T updatedItem = MissionItemType.restoreMissionItemFromBundle(result.getData());
            complexItem.copy(updatedItem);
            return (T) complexItem;
        }
        else
            return null;
    }
}
