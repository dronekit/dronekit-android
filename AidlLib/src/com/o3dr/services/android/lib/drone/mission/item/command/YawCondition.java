package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/10/14.
 */
public class YawCondition extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private double angle;
    private double angularSpeed;
    private boolean isRelative;

    public YawCondition(){
        super(MissionItemType.YAW_CONDITION);
    }

    public YawCondition(YawCondition copy){
        this();
        angle = copy.angle;
        angularSpeed = copy.angularSpeed;
        isRelative = copy.isRelative;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngularSpeed() {
        return angularSpeed;
    }

    public void setAngularSpeed(double angularSpeed) {
        this.angularSpeed = angularSpeed;
    }

    public boolean isRelative() {
        return isRelative;
    }

    public void setRelative(boolean isRelative) {
        this.isRelative = isRelative;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.angle);
        dest.writeDouble(this.angularSpeed);
        dest.writeByte(isRelative ? (byte) 1 : (byte) 0);
    }

    private YawCondition(Parcel in) {
        super(in);
        this.angle = in.readDouble();
        this.angularSpeed = in.readDouble();
        this.isRelative = in.readByte() != 0;
    }

    @Override
    public MissionItem clone() {
        return new YawCondition(this);
    }

    public static final Creator<YawCondition> CREATOR = new Creator<YawCondition>() {
        public YawCondition createFromParcel(Parcel source) {
            return new YawCondition(source);
        }

        public YawCondition[] newArray(int size) {
            return new YawCondition[size];
        }
    };
}
