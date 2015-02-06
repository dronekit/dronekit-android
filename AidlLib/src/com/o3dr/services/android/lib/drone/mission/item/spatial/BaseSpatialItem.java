package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public abstract class BaseSpatialItem extends MissionItem implements MissionItem.SpatialItem, android.os.Parcelable {

    private LatLongAlt coordinate;

    protected BaseSpatialItem(MissionItemType type) {
        super(type);
    }

    protected BaseSpatialItem(BaseSpatialItem copy){
        this(copy.getType());
        coordinate = copy.coordinate == null ? null : new LatLongAlt(copy.coordinate);
    }

    @Override
    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    @Override
    public void setCoordinate(LatLongAlt coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.coordinate, flags);
    }

    protected BaseSpatialItem(Parcel in) {
        super(in);
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
    }
}
