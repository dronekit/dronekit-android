package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class ReturnToLaunch extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private double returnAltitude;

    public ReturnToLaunch(){
        super(MissionItemType.RETURN_TO_LAUNCH);
    }

    public ReturnToLaunch(ReturnToLaunch copy){
        this();
        returnAltitude = copy.returnAltitude;
    }

    public double getReturnAltitude() {
        return returnAltitude;
    }

    public void setReturnAltitude(double returnAltitude) {
        this.returnAltitude = returnAltitude;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.returnAltitude);
    }

    private ReturnToLaunch(Parcel in) {
        super(in);
        this.returnAltitude = in.readDouble();
    }

    @Override
    public MissionItem clone() {
        return new ReturnToLaunch(this);
    }

    public static final Creator<ReturnToLaunch> CREATOR = new Creator<ReturnToLaunch>() {
        public ReturnToLaunch createFromParcel(Parcel source) {
            return new ReturnToLaunch(source);
        }

        public ReturnToLaunch[] newArray(int size) {
            return new ReturnToLaunch[size];
        }
    };
}
