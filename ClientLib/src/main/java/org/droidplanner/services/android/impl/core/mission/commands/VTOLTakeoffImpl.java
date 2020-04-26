package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.impl.core.mission.Mission;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class VTOLTakeoffImpl extends MissionCMD {

    public static final double DEFAULT_TAKEOFF_ALTITUDE = 10.0;

    private int frontTransitionHeading;
    private double yawAngle;
    private double lat;
    private double lng;
    private double alt;

    public VTOLTakeoffImpl(MissionItemImpl item) {
        super(item);
    }

    public VTOLTakeoffImpl(msg_mission_item msg, Mission mission) {
        super(mission);
        unpackMAVMessage(msg);
    }

    public VTOLTakeoffImpl(Mission mission, int frontTransitionHeading, double yawAngle, double lat, double lng, double alt) {
        super(mission);
        this.frontTransitionHeading = frontTransitionHeading;
        this.yawAngle = yawAngle;
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_VTOL_TAKEOFF;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;

        mavMsg.param1 = 0;
        mavMsg.param2 = frontTransitionHeading;
        mavMsg.param3 = 0;
        mavMsg.param4 = (float)yawAngle;
        mavMsg.x = (float)lat;
        mavMsg.y = (float)lng;
        mavMsg.z = (float)alt;

        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        frontTransitionHeading = (int)mavMsg.param2;
        yawAngle = mavMsg.param4;
        lat = mavMsg.x;
        lng = mavMsg.y;
        alt = mavMsg.z;
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.VTOL_TAKEOFF;
    }

    public int getFrontTransitionHeading() {
        return frontTransitionHeading;
    }

    public double getYawAngle() { return yawAngle; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getAlt() { return alt; }
}
