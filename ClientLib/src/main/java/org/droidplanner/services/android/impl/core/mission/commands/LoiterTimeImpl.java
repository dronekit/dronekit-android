package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.impl.core.mission.Mission;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class LoiterTimeImpl extends MissionCMD {
	private double lat;
	private double lng;
	private double alt;
	private long delay;
	private double radius;

	public LoiterTimeImpl(MissionItemImpl item) {
		super(item);
	}

	public LoiterTimeImpl(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public LoiterTimeImpl(Mission mission, double lat, double lng, double alt, long delay, double radius) {
		super(mission);
		this.lat = lat;
		this.lng = lng;
		this.alt = alt;
		this.delay = delay;
		this.radius = radius;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LOITER_TIME;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.param1 = delay; // No heading required
        mavMsg.param2 = 0; // 0 radius (use standard loiter radius)
		mavMsg.param3 = (float)radius;
        mavMsg.param4 = 0; // Center wp of loiter
        mavMsg.x = (float)this.lat;
        mavMsg.y = (float)this.lng;
        mavMsg.z = (float)this.alt;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
        lat = mavMsg.x;
        lng = mavMsg.y;
        alt = mavMsg.z;
        delay = (long)mavMsg.param1;
        radius = mavMsg.param3;
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.LOITER_TIME;
	}

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
}
