package org.droidplanner.services.android.core.mission.waypoints;

import java.util.List;

import org.droidplanner.services.android.core.mission.Mission;
import org.droidplanner.services.android.core.mission.MissionItemImpl;
import org.droidplanner.services.android.core.mission.MissionItemType;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

public class RegionOfInterestImpl extends SpatialCoordItem {

	public RegionOfInterestImpl(MissionItemImpl item) {
		super(item);
	}
	
	public RegionOfInterestImpl(Mission mission, LatLongAlt coord) {
		super(mission,coord);
	}

	public RegionOfInterestImpl(msg_mission_item msg, Mission mission) {
		super(mission, null);
		unpackMAVMessage(msg);
	}

	/**
	 * @return True if this roi cancels a previously set roi.
	 */
	public boolean isReset(){
		return coordinate.getLatitude() == 0 && coordinate.getLongitude() == 0 && coordinate.getAltitude() == 0;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_SET_ROI;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.ROI;
	}

}