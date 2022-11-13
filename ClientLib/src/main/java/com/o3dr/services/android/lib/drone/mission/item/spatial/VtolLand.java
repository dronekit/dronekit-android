package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Created by tajisoft on 25/7/22.
 */
public class VtolLand extends BaseSpatialItem implements android.os.Parcelable {

    public VtolLand(){
        super(MissionItemType.VTOL_LAND, new LatLongAlt(0.0, 0.0, 0.0));
    }

    public VtolLand(VtolLand copy){
        super(copy);
    }

    private VtolLand(Parcel in) {
        super(in);
    }

    @Override
    public String toString() {
        return "VtolLand{ " + super.toString() + " }";
    }

    @Override
    public MissionItem clone() {
        return new VtolLand(this);
    }

    public static final Creator<VtolLand> CREATOR = new Creator<VtolLand>() {
        public VtolLand createFromParcel(Parcel source) {
            return new VtolLand(source);
        }

        public VtolLand[] newArray(int size) {
            return new VtolLand[size];
        }
    };
}
