package org.droidplanner.services.android.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.coordinates.Coord3D;
import org.droidplanner.services.android.core.mission.Mission;
import org.droidplanner.services.android.core.mission.MissionItem;
import org.droidplanner.services.android.core.mission.MissionItemType;

import java.util.List;

public class LandImpl extends SpatialCoordItem {

    public LandImpl(MissionItem item) {
        super(item);
        setAltitude((0.0));
    }

    public LandImpl(Mission mission) {
        this(mission, new Coord2D(0, 0));
    }

    public LandImpl(Mission mMission, Coord2D coord) {
        super(mMission, new Coord3D(coord, (0)));
    }

    public LandImpl(msg_mission_item msg, Mission mission) {
        super(mission, null);
        unpackMAVMessage(msg);
    }


    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_LAND;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        super.unpackMAVMessage(mavMsg);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.LAND;
    }

}