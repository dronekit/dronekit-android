package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.impl.core.mission.Mission;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class VTOLTransitionImpl extends MissionCMD {
	private int targetState;

	public VTOLTransitionImpl(MissionItemImpl item) {
		super(item);
	}

	public VTOLTransitionImpl(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public VTOLTransitionImpl(Mission mission, int targetState) {
		super(mission);
		this.targetState = targetState;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_VTOL_TRANSITION;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.param1 = targetState;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
        targetState = (int)mavMsg.param1;
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.VTOL_TRANSITION;
	}

    public int getTargetState() { return targetState; }
    public void setTargetState(int state) { targetState = state; }
}
