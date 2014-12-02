package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class SetServo extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private int pwm;
    private int channel;

    public SetServo(){
        super(MissionItemType.SET_SERVO);
    }

    public int getPwm() {
        return pwm;
    }

    public void setPwm(int pwm) {
        this.pwm = pwm;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.pwm);
        dest.writeInt(this.channel);
    }

    private SetServo(Parcel in) {
        super(in);
        this.pwm = in.readInt();
        this.channel = in.readInt();
    }

    public static final Creator<SetServo> CREATOR = new Creator<SetServo>() {
        public SetServo createFromParcel(Parcel source) {
            return new SetServo(source);
        }

        public SetServo[] newArray(int size) {
            return new SetServo[size];
        }
    };
}
