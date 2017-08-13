package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.services.android.impl.core.mission.Mission;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class TakePictureImpl extends MissionCMD {
    public TakePictureImpl(MissionItemImpl item) {
        super(item);
    }

    public TakePictureImpl(msg_mission_item msg, Mission mission) {
        super(mission);
        unpackMAVMessage(msg);
    }

    public TakePictureImpl(Mission mission, double triggerDistance) {
        super(mission);
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_DO_DIGICAM_CONTROL;
        mavMsg.x = 1; // Yes, this is correct. AP_Mission.cpp:717
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
//        distance = (mavMsg.param1);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.CAMERA_TRIGGER;
    }
}