package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class CameraTrigger extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private double triggerDistance;

    public CameraTrigger(){
        super(MissionItemType.CAMERA_TRIGGER);
    }

    public CameraTrigger(CameraTrigger copy){
        super(MissionItemType.CAMERA_TRIGGER);
        triggerDistance = copy.triggerDistance;
    }

    public double getTriggerDistance() {
        return triggerDistance;
    }

    public void setTriggerDistance(double triggerDistance) {
        this.triggerDistance = triggerDistance;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.triggerDistance);
    }

    private CameraTrigger(Parcel in) {
        super(in);
        this.triggerDistance = in.readDouble();
    }

    @Override
    public MissionItem clone() {
        return new CameraTrigger(this);
    }

    public static final Creator<CameraTrigger> CREATOR = new Creator<CameraTrigger>() {
        public CameraTrigger createFromParcel(Parcel source) {
            return new CameraTrigger(source);
        }

        public CameraTrigger[] newArray(int size) {
            return new CameraTrigger[size];
        }
    };
}
