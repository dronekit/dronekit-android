package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * MAV_CMD_NAV_LOITER_TO_ALT
 *
 * Begin loiter at the specified coordinate, and don't consider this waypoint complete
 * until the target coordinate's altitude has been reached.
 */
public class LoiterToAlt extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private LatLongAlt coordinate;

    public LoiterToAlt(){
        super(MissionItemType.LOITER_TO_ALT);
    }

    public LoiterToAlt(LoiterToAlt src){
        super(MissionItemType.LOITER_TO_ALT);
        coordinate = src.coordinate;
    }

    public LatLongAlt getCoordinate() { return coordinate; }
    public void setCoordinate(LatLongAlt coord) {
        coordinate = coord;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.coordinate.getLatitude());
        dest.writeDouble(this.coordinate.getLongitude());
        dest.writeDouble(this.coordinate.getAltitude());
    }

    private LoiterToAlt(Parcel in) {
        super(in);
        double lat = in.readDouble();
        double lng = in.readDouble();
        double alt = in.readDouble();
        this.coordinate = new LatLongAlt(lat, lng, alt);
    }

    @Override
    public MissionItem clone() {
        return new LoiterToAlt(this);
    }

    public static final Creator<LoiterToAlt> CREATOR = new Creator<LoiterToAlt>() {
        public LoiterToAlt createFromParcel(Parcel source) {
            return new LoiterToAlt(source);
        }

        public LoiterToAlt[] newArray(int size) {
            return new LoiterToAlt[size];
        }
    };
}
