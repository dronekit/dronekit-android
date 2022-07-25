package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class VtolLandImpl extends SpatialCoordItem {

    public VtolLandImpl(MissionItemImpl item) {
        super(item);
        setAltitude((0.0));
    }

    public VtolLandImpl(MissionImpl missionImpl) {
        this(missionImpl, new LatLong(0, 0));
    }

    public VtolLandImpl(MissionImpl mMissionImpl, LatLong coord) {
        super(mMissionImpl, new LatLongAlt(coord, 0));
    }

    public VtolLandImpl(msg_mission_item msg, MissionImpl missionImpl) {
        super(missionImpl, null);
        unpackMAVMessage(msg);
    }


    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_VTOL_LAND;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        super.unpackMAVMessage(mavMsg);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.VTOL_LAND;
    }

}