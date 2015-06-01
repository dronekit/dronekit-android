package org.droidplanner.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import java.util.List;

public class DoLandStart extends SpatialCoordItem {

    public DoLandStart(MissionItem item) {
        super(item);
        setAltitude((0.0));
    }

    public DoLandStart(Mission mission) {
        this(mission, new Coord2D(0, 0));
    }

    public DoLandStart(Mission mMission, Coord2D coord) {
        super(mMission, new Coord3D(coord, (0)));
    }

    public DoLandStart(msg_mission_item msg, Mission mission) {
        super(mission, null);
        unpackMAVMessage(msg);
    }


    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_DO_LAND_START;
        mavMsg.x = (float)coordinate.getLat();
        mavMsg.y = (float) coordinate.getLng();
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