package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Created by Toby on 7/31/2015.
 */
public class DoJump extends MissionItem implements MissionItem.Command, android.os.Parcelable {
    private int waypoint;
    private int repeatCount;

    public DoJump(){
        super(MissionItemType.DO_JUMP);
    }

    public DoJump(DoJump copy){
        this();
        this.waypoint = copy.waypoint;
        this.repeatCount = copy.repeatCount;
    }

    protected DoJump(MissionItemType type) {
        super(type);
    }

    protected DoJump(Parcel in) {
        super(in);
        waypoint = in.readInt();
        repeatCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(waypoint);
        dest.writeInt(repeatCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(int waypoint) {
        this.waypoint = waypoint;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public static final Creator<DoJump> CREATOR = new Creator<DoJump>() {
        @Override
        public DoJump createFromParcel(Parcel in) {
            return new DoJump(in);
        }

        @Override
        public DoJump[] newArray(int size) {
            return new DoJump[size];
        }
    };

    @Override
    public MissionItem clone() {
        return new DoJump(this);
    }
}
