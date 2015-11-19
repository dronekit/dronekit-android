package org.droidplanner.services.android.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.services.android.core.mission.Mission;
import org.droidplanner.services.android.core.mission.MissionItemImpl;
import org.droidplanner.services.android.core.mission.MissionItemType;

import java.util.List;

public class DoLandStartImpl extends SpatialCoordItem {

    public DoLandStartImpl(MissionItemImpl item) {
        super(item);
        setAltitude((0.0));
    }

    public DoLandStartImpl(Mission mission) {
        this(mission, new LatLong(0, 0));
    }

    public DoLandStartImpl(Mission mMission, LatLong coord) {
        super(mMission, new LatLongAlt(coord, (0)));
    }

    public DoLandStartImpl(msg_mission_item msg, Mission mission) {
        super(mission, null);
        unpackMAVMessage(msg);
    }


    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_DO_LAND_START;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        super.unpackMAVMessage(mavMsg);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.DO_LAND_START;
    }

}