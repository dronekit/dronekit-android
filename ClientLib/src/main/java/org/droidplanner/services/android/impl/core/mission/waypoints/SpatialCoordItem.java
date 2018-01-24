package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;

import com.o3dr.services.android.lib.coordinate.Frame;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.util.List;

public abstract class SpatialCoordItem extends MissionItemImpl {

    protected LatLongAlt coordinate;

    public SpatialCoordItem(MissionImpl missionImpl, LatLongAlt coord) {
        super(missionImpl);
        this.coordinate = coord;
    }

    public SpatialCoordItem(MissionItemImpl item) {
        super(item);
        if (item instanceof SpatialCoordItem) {
            coordinate = ((SpatialCoordItem) item).getCoordinate();
        } else {
            coordinate = new LatLongAlt(0, 0, 0, Frame.GLOBAL_RELATIVE); // TODO lets use null to mean no location
        }
    }

    public void setCoordinate(LatLongAlt coordNew) {
        coordinate = coordNew;
    }

    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.frame = (short) coordinate.getFrame().asInt();
        mavMsg.x = (float) coordinate.getLatitude();
        mavMsg.y = (float) coordinate.getLongitude();
        mavMsg.z = (float) coordinate.getAltitude();
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        setCoordinate(new LatLongAlt(mavMsg.x, mavMsg.y, mavMsg.z, Frame.getFrame(mavMsg.frame)));
    }

    public void setAltitude(double altitude) {
        coordinate.setAltitude(altitude);
    }

    public void setPosition(LatLong position) {
        coordinate.set(position);
    }

}