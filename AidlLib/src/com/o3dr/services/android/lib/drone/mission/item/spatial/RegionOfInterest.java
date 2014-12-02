package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class RegionOfInterest extends BaseSpatialItem implements android.os.Parcelable {

    public RegionOfInterest(){
        super(MissionItemType.REGION_OF_INTEREST);
    }

    private RegionOfInterest(Parcel in) {
        super(in);
    }

    public static final Creator<RegionOfInterest> CREATOR = new Creator<RegionOfInterest>() {
        public RegionOfInterest createFromParcel(Parcel source) {
            return new RegionOfInterest(source);
        }

        public RegionOfInterest[] newArray(int size) {
            return new RegionOfInterest[size];
        }
    };
}
