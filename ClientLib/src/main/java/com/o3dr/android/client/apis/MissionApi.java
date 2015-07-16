package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.*;

/**
 * Provides access to missions specific functionality.
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class MissionApi implements Api {

    private static final ConcurrentHashMap<Drone, MissionApi> missionApiCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a MissionApi instance.
     * @param drone Target vehicle
     * @return a MissionApi instance.
     */
    public static MissionApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, missionApiCache, new Builder<MissionApi>() {
            @Override
            public MissionApi build() {
                return new MissionApi(drone);
            }
        });
    }

    private final Drone drone;

    private MissionApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Generate action to create a dronie mission, and upload it to the connected drone.
     */
    public void generateDronie() {
        drone.performAsyncAction(new Action(ACTION_GENERATE_DRONIE));
    }

    /**
     * Generate action to update the mission property for the drone model in memory.
     *
     * @param mission     mission to upload to the drone.
     * @param pushToDrone if true, upload the mission to the connected device.
     */
    public void setMission(Mission mission, boolean pushToDrone) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MISSION, mission);
        params.putBoolean(EXTRA_PUSH_TO_DRONE, pushToDrone);
        drone.performAsyncAction(new Action(ACTION_SET_MISSION, params));
    }

    /**
     * Starts the mission. The vehicle will only accept this command if armed and in Auto mode.
     * note: This command is only supported by APM:Copter V3.3 and newer.
     *
     * @param forceModeChange Change to Auto mode if not in Auto.
     * @param forceArm Arm the vehicle if it is disarmed.
     * @param listener
     */
    public void startMission(boolean forceModeChange, boolean forceArm, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_FORCE_MODE_CHANGE, forceModeChange);
        params.putBoolean(EXTRA_FORCE_ARM, forceArm);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_START_MISSION, params), listener);
    }

    /**
     * Load waypoints from the target vehicle.
     */
    public void loadWaypoints() {
        drone.performAsyncAction(new Action(ACTION_LOAD_WAYPOINTS));
    }

    /**
     * Build and return complex mission item.
     * @param itemBundle bundle containing the complex mission item to update.
     */
    private Action buildComplexMissionItem(Bundle itemBundle) {
        Action payload = new Action(ACTION_BUILD_COMPLEX_MISSION_ITEM, itemBundle);
        boolean result = drone.performAction(payload);
        if(result)
            return payload;
        else
            return null;
    }

    /**
     * Builds and validates a complex mission item against the target vehicle.
     * @param complexItem Mission item to build.
     * @return an updated mission item.
     */
    public <T extends MissionItem> T buildMissionItem(MissionItem.ComplexItem<T> complexItem){
        T missionItem = (T) complexItem;
        Bundle payload = missionItem.getType().storeMissionItem(missionItem);
        if (payload == null)
            return null;

        Action result = buildComplexMissionItem(payload);
        if(result != null){
            T updatedItem = MissionItemType.restoreMissionItemFromBundle(result.getData());
            complexItem.copy(updatedItem);
            return (T) complexItem;
        }
        else
            return null;
    }
}
