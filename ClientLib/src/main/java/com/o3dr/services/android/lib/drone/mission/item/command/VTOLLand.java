package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * MAV_CMD_NAV_VTOL_LANDING
 *
 * Land in VTOL mode
 */
public class VTOLLand extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private double approachAltitude;
    private double yawAngle;
    private LatLongAlt coordinate;

    public VTOLLand(){
        super(MissionItemType.VTOL_LAND);
    }

    public VTOLLand(VTOLLand src) {
        super(MissionItemType.VTOL_TAKEOFF);
        approachAltitude = src.approachAltitude;
        yawAngle = src.yawAngle;
        coordinate = src.coordinate;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(approachAltitude);
        dest.writeDouble(yawAngle);
        dest.writeDouble(coordinate.getLatitude());
        dest.writeDouble(coordinate.getLongitude());
        dest.writeDouble(coordinate.getAltitude());
    }

    private VTOLLand(Parcel in) {
        super(in);
        approachAltitude = in.readDouble();
        yawAngle = in.readDouble();
        double lat = in.readDouble();
        double lng = in.readDouble();
        double alt = in.readDouble();
        this.coordinate = new LatLongAlt(lat, lng, alt);
    }

    @Override
    public MissionItem clone() {
        return new VTOLLand(this);
    }

    public double getApproachAltitude() { return approachAltitude; }
    public void setApproachAltitude(double approachAltitude) { this.approachAltitude = approachAltitude; }

    public double getYawAngle() { return yawAngle; }
    public void setYawAngle(double yawAngle) { this.yawAngle = yawAngle; }

    public LatLongAlt getCoordinate() { return coordinate; }
    public void setCoordinate(LatLongAlt coordinate) { this.coordinate = coordinate; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "approachAltitude=" + approachAltitude +
                ", yawAngle=" + yawAngle +
                ", coordinate=" + coordinate +
                '}';
    }

    public static final Creator<VTOLLand> CREATOR = new Creator<VTOLLand>() {
        public VTOLLand createFromParcel(Parcel source) {
            return new VTOLLand(source);
        }

        public VTOLLand[] newArray(int size) {
            return new VTOLLand[size];
        }
    };
}
