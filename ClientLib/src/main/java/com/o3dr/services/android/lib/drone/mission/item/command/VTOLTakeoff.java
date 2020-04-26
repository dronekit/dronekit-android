package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * MAV_CMD_NAV_VTOL_TAKEOFF
 *
 * Takeoff in VTOL mode
 */
public class VTOLTakeoff extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    public enum TransitionHeading {
        Default(0),
        NextWaypoint(1),
        TakeoffHeading(2),
        UseSpecified(3),
        AnyHeading(4)
        ;

        private int value;

        TransitionHeading(int value) {
            this.value = value;
        }

        public int getValue() { return value; }

        public static TransitionHeading fromValue(int value) {
            for(TransitionHeading t: values()) {
                if(t.value == value) {
                    return t;
                }
            }

            return Default;
        }
    }

    private TransitionHeading transitionHeading = TransitionHeading.Default;
    private double yawAngle;
    private LatLongAlt coordinate;

    public VTOLTakeoff(){
        super(MissionItemType.VTOL_TAKEOFF);
    }

    public VTOLTakeoff(VTOLTakeoff src) {
        super(MissionItemType.VTOL_TAKEOFF);
        transitionHeading = src.transitionHeading;
        yawAngle = src.yawAngle;
        coordinate = src.coordinate;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(transitionHeading.getValue());
        dest.writeDouble(yawAngle);
        dest.writeDouble(coordinate.getLatitude());
        dest.writeDouble(coordinate.getLongitude());
        dest.writeDouble(coordinate.getAltitude());
    }

    private VTOLTakeoff(Parcel in) {
        super(in);
        transitionHeading = TransitionHeading.fromValue(in.readInt());
        yawAngle = in.readDouble();
        double lat = in.readDouble();
        double lng = in.readDouble();
        double alt = in.readDouble();
        this.coordinate = new LatLongAlt(lat, lng, alt);
    }

    public TransitionHeading getTransitionHeading() { return transitionHeading; }
    public void setTransitionHeading(TransitionHeading transitionHeading) { this.transitionHeading = transitionHeading; }

    public double getYawAngle() { return yawAngle; }
    public void setYawAngle(double yawAngle) { this.yawAngle = yawAngle; }

    public LatLongAlt getCoordinate() { return coordinate; }
    public void setCoordinate(LatLongAlt coordinate) { this.coordinate = coordinate; }

    @Override
    public MissionItem clone() {
        return new VTOLTakeoff(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "transitionHeading=" + transitionHeading +
                ", yawAngle=" + yawAngle +
                ", coordinate=" + coordinate +
                '}';
    }

    public static final Creator<VTOLTakeoff> CREATOR = new Creator<VTOLTakeoff>() {
        public VTOLTakeoff createFromParcel(Parcel source) {
            return new VTOLTakeoff(source);
        }

        public VTOLTakeoff[] newArray(int size) {
            return new VTOLTakeoff[size];
        }
    };
}
