package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Created by fhuya on 11/6/14.
 */
public class TakePicture extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    public TakePicture(){
        super(MissionItemType.TAKE_PICTURE);
    }

    public TakePicture(TakePicture copy) {
        super(MissionItemType.TAKE_PICTURE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TakePicture)) return false;
        if (!super.equals(o)) return false;

        TakePicture that = (TakePicture) o;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(0);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TakePicture";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    private TakePicture(Parcel in) {
        super(in);
    }

    @Override
    public MissionItem clone() {
        return new TakePicture(this);
    }

    public static final Creator<TakePicture> CREATOR = new Creator<TakePicture>() {
        public TakePicture createFromParcel(Parcel source) {
            return new TakePicture(source);
        }

        public TakePicture[] newArray(int size) {
            return new TakePicture[size];
        }
    };
}
