package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

import org.droidplanner.services.android.impl.core.mission.commands.RawMissionCommandImpl;

public class RawMissionCommand extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private float param1;
    private float param2;
    private float param3;
    private float param4;
    private float x;
    private float y;
    private float z;
    private int command;
    private short target_system;
    private short target_component;

    public RawMissionCommand() {
        super(MissionItemType.RAW_COMMAND);
    }

    public RawMissionCommand(RawMissionCommand in) {
        this();
        command = in.command;
        param1 = in.param1;
        param2 = in.param2;
        param3 = in.param3;
        param4 = in.param4;
        x = in.x;
        y = in.y;
        z = in.z;
        target_component = in.target_component;
        target_system = in.target_system;
    }

    public RawMissionCommand setTo(RawMissionCommandImpl s) {
        command = s.getCommand();
        param1 = s.getParam1();
        param2 = s.getParam2();
        param3 = s.getParam3();
        param4 = s.getParam4();
        x = s.getX();
        y = s.getY();
        z = s.getZ();
        target_system = s.getTarget_system();
        target_component = s.getTarget_component();

        return this;
    }

    public float getParam1() { return param1; }
    public RawMissionCommand setParam1(float param1) { this.param1 = param1; return this; }

    public float getParam2() { return param2; }
    public RawMissionCommand setParam2(float param2) { this.param2 = param2; return this; }

    public float getParam3() { return param3; }
    public RawMissionCommand setParam3(float param3) { this.param3 = param3; return this; }

    public float getParam4() { return param4; }
    public RawMissionCommand setParam4(float param4) { this.param4 = param4; return this; }

    public float getX() { return x; }
    public RawMissionCommand setX(float x) { this.x = x; return this; }

    public float getY() { return y; }
    public RawMissionCommand setY(float y) { this.y = y; return this; }

    public float getZ() { return z; }
    public RawMissionCommand setZ(float z) { this.z = z; return this; }

    public int getCommand() { return command; }
    public RawMissionCommand setCommand(int command) { this.command = command; return this; }

    public short getTarget_system() { return target_system; }
    public RawMissionCommand setTarget_system(short target_system) { this.target_system = target_system; return this; }

    public short getTarget_component() { return target_component; }
    public RawMissionCommand setTarget_component(short target_component) { this.target_component = target_component; return this; }

    @Override
    public MissionItem clone() {
        return new RawMissionCommand(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.param1);
        dest.writeFloat(this.param2);
        dest.writeFloat(this.param3);
        dest.writeFloat(this.param4);
        dest.writeFloat(this.x);
        dest.writeFloat(this.y);
        dest.writeFloat(this.z);
        dest.writeInt(this.command);
        dest.writeInt(this.target_system);
        dest.writeInt(this.target_component);
    }

    protected RawMissionCommand(Parcel in) {
        super(in);
        this.param1 = in.readFloat();
        this.param2 = in.readFloat();
        this.param3 = in.readFloat();
        this.param4 = in.readFloat();
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.z = in.readFloat();
        this.command = in.readInt();
        this.target_system = (short) in.readInt();
        this.target_component = (short) in.readInt();
    }

    public static final Creator<RawMissionCommand> CREATOR = new Creator<RawMissionCommand>() {
        @Override
        public RawMissionCommand createFromParcel(Parcel source) {
            return new RawMissionCommand(source);
        }

        @Override
        public RawMissionCommand[] newArray(int size) {
            return new RawMissionCommand[size];
        }
    };

    @Override
    public String toString() {
        return "RawMissionCommand{" +
                "param1=" + param1 +
                ", param2=" + param2 +
                ", param3=" + param3 +
                ", param4=" + param4 +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", command=" + command +
                ", target_system=" + target_system +
                ", target_component=" + target_component +
                '}';
    }
}
