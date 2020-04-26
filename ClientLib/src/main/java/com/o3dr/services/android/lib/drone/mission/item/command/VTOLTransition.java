package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.MAVLink.enums.MAV_VTOL_STATE;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * MAV_CMD_DO_VTOL_TRANSITION
 *
 * Start a VTOL transition either to fixed-wing or multi-copter state
 */
public class VTOLTransition extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    public enum TargetState {
        Undefined(MAV_VTOL_STATE.MAV_VTOL_STATE_UNDEFINED),
        MultiCopter(MAV_VTOL_STATE.MAV_VTOL_STATE_MC),
        FixedWing(MAV_VTOL_STATE.MAV_VTOL_STATE_FW)
        ;

        final int state;
        TargetState(int state) {
            this.state = state;
        }

        public int getState() { return state; }
        public static TargetState fromOrdinal(int o) {
            for(TargetState s: values()) {
                if(s.ordinal() == o) {
                    return s;
                }
            }

            return Undefined;
        }

        public static TargetState fromValue(int v) {
            for(TargetState s: values()) {
                if(s.state == v) {
                    return s;
                }
            }

            return Undefined;
        }
    }

    private TargetState targetState = TargetState.Undefined;

    public VTOLTransition(){
        super(MissionItemType.VTOL_TRANSITION);
    }

    public VTOLTransition(VTOLTransition src) {
        super(MissionItemType.VTOL_TRANSITION);
        targetState = src.targetState;
    }

    public VTOLTransition(TargetState state) {
        this();
        this.targetState = state;
    }

    public TargetState getTargetState() { return targetState; }
    public void setTargetState(TargetState state) {
        targetState = state;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.targetState.ordinal());
    }

    private VTOLTransition(Parcel in) {
        super(in);
        this.targetState = TargetState.fromOrdinal(in.readInt());
    }

    @Override
    public MissionItem clone() {
        return new VTOLTransition(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "targetState=" + targetState +
                '}';
    }

    public static final Creator<VTOLTransition> CREATOR = new Creator<VTOLTransition>() {
        public VTOLTransition createFromParcel(Parcel source) {
            return new VTOLTransition(source);
        }

        public VTOLTransition[] newArray(int size) {
            return new VTOLTransition[size];
        }
    };
}
