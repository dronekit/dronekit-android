package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.o3dr.services.android.lib.drone.mission.item.command.RawMissionCommand;

import org.droidplanner.services.android.impl.core.mission.Mission;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class RawMissionCommandImpl extends MissionCMD {

    private int command;
    private float param1;
    private float param2;
    private float param3;
    private float param4;
    private float x;
    private float y;
    private float z;
    private short target_system;
    private short target_component;

    public RawMissionCommandImpl(MissionItemImpl input) {
        super(input);
    }

    public RawMissionCommandImpl(Mission mission) {
        super(mission);
    }

    public RawMissionCommandImpl(msg_mission_item msg, Mission mission) {
        super(mission);
        unpackMAVMessage(msg);
    }

    public RawMissionCommandImpl setTo(RawMissionCommand s) {
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

    public float getParam1() {
        return param1;
    }

    public void setParam1(float param1) {
        this.param1 = param1;
    }

    public float getParam2() {
        return param2;
    }

    public void setParam2(float param2) {
        this.param2 = param2;
    }

    public float getParam3() {
        return param3;
    }

    public void setParam3(float param3) {
        this.param3 = param3;
    }

    public float getParam4() {
        return param4;
    }

    public void setParam4(float param4) {
        this.param4 = param4;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public short getTarget_system() {
        return target_system;
    }

    public void setTarget_system(short target_system) {
        this.target_system = target_system;
    }

    public short getTarget_component() {
        return target_component;
    }

    public void setTarget_component(short target_component) {
        this.target_component = target_component;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item msg) {
        param1 = msg.param1;
        param2 = msg.param2;
        param3 = msg.param3;
        param4 = msg.param4;
        x = msg.x;
        y = msg.y;
        z = msg.z;
        command = msg.command;
        target_system = msg.target_system;
        target_component = msg.target_component;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        final List<msg_mission_item> list = super.packMissionItem();

        final msg_mission_item msg = list.get(0);
        msg.param1 = param1;
        msg.param2 = param2;
        msg.param3 = param3;
        msg.param4 = param4;
        msg.x = x;
        msg.y = y;
        msg.z = z;
        msg.command = command;
        msg.target_component = target_component;
        msg.target_system = target_system;

        return list;
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.RAW_COMMAND;
    }
}
