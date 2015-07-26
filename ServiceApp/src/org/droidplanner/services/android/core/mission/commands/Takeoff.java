package org.droidplanner.services.android.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.core.mission.Mission;
import org.droidplanner.services.android.core.mission.MissionItem;
import org.droidplanner.services.android.core.mission.MissionItemType;

import java.util.List;

public class Takeoff extends MissionCMD {

    public static final double DEFAULT_TAKEOFF_ALTITUDE = 10.0;

    private double finishedAlt = 10;
    private double minPitch = 0;

    public Takeoff(MissionItem item) {
        super(item);
    }

    public Takeoff(msg_mission_item msg, Mission mission) {
        super(mission);
        unpackMAVMessage(msg);
    }

    public Takeoff(Mission mission, double altitude) {
        super(mission);
        finishedAlt = altitude;
        setMinPitch(0);
    }

    public Takeoff(Mission mission, double altitude, double minPitch) {
        super(mission);
        finishedAlt = altitude;
        setMinPitch(minPitch);
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.z = (float) finishedAlt;
        if (minPitch > 0)
            mavMsg.param1 = (float)getMinPitch();
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        finishedAlt = (mavMsg.z);
        minPitch = mavMsg.param1;
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.TAKEOFF;
    }

    public double getFinishedAlt() {
        return finishedAlt;
    }

    public void setFinishedAlt(double finishedAlt) {
        this.finishedAlt = finishedAlt;
    }

    public double getMinPitch() {
        return minPitch;
    }

    public void setMinPitch(double minPitch) {
        this.minPitch = minPitch;
    }
}
