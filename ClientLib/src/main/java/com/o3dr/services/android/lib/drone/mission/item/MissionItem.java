package com.o3dr.services.android.lib.drone.mission.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

import java.io.Serializable;

/**
 * Created by fhuya on 11/5/14.
 */
public abstract class MissionItem implements Cloneable, Parcelable {

    public interface Command {}

    public interface SpatialItem {
        LatLongAlt getCoordinate();

        void setCoordinate(LatLongAlt coordinate);
    }

    public interface ComplexItem<T extends MissionItem> {
        void copy(T source);
    }

    private final MissionItemType type;

    protected MissionItem(MissionItemType type) {
        this.type = type;
    }

    public MissionItemType getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type.ordinal());
    }

    protected MissionItem(Parcel in){
        this.type = MissionItemType.values()[in.readInt()];
    }

    @Override
    public abstract MissionItem clone();

}
