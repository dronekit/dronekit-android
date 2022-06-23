package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 10/28/14.
 */
public enum VehicleMode implements DroneAttribute {

    PLANE_MANUAL(0, Type.TYPE_PLANE, "Manual"),
    PLANE_CIRCLE(1, Type.TYPE_PLANE, "Circle"),
    PLANE_STABILIZE(2, Type.TYPE_PLANE, "Stabilize"),
    PLANE_TRAINING(3, Type.TYPE_PLANE, "Training"),
    PLANE_ACRO(4, Type.TYPE_PLANE, "Acro"),
    PLANE_FLY_BY_WIRE_A(5, Type.TYPE_PLANE, "FBW A"),
    PLANE_FLY_BY_WIRE_B(6, Type.TYPE_PLANE, "FBW B"),
    PLANE_CRUISE(7, Type.TYPE_PLANE, "Cruise"),
    PLANE_AUTOTUNE(8, Type.TYPE_PLANE, "Autotune"),
    PLANE_AUTO(10, Type.TYPE_PLANE, "Auto"),
    PLANE_RTL(11, Type.TYPE_PLANE, "RTL"),
    PLANE_LOITER(12, Type.TYPE_PLANE, "Loiter"),
    PLANE_TAKEOFF(13, Type.TYPE_PLANE, "Takeoff"),
    PLANE_AVOID_ADSB(14, Type.TYPE_PLANE, "Avoid ADSB"),
    PLANE_GUIDED(15, Type.TYPE_PLANE, "Guided"),
    PLANE_INITIALISING(16, Type.TYPE_PLANE, "Initialising"),
    PLANE_QSTABILIZE(17, Type.TYPE_QUADPLANE, "QStabilize"),
    PLANE_QHOVER(18, Type.TYPE_QUADPLANE, "QHover"),
    PLANE_QLOITER(19, Type.TYPE_QUADPLANE, "QLoiter"),
    PLANE_QLAND(20, Type.TYPE_QUADPLANE, "QLand"),
    PLANE_QRTL(21, Type.TYPE_QUADPLANE, "QRTL"),
    PLANE_QAUTOTUNE(22, Type.TYPE_QUADPLANE, "QAutoTune"),
    PLANE_QACRO(23, Type.TYPE_QUADPLANE, "QAcro"),
    PLANE_THERMAL(24, Type.TYPE_PLANE, "Thermal"),
    PLANE_LOITER_ALT_QLAND(25, Type.TYPE_QUADPLANE, "LoiterAltQLand"),

    COPTER_STABILIZE(0, Type.TYPE_COPTER, "Stabilize"),
    COPTER_ACRO(1, Type.TYPE_COPTER, "Acro"),
    COPTER_ALT_HOLD(2, Type.TYPE_COPTER, "Alt Hold"),
    COPTER_AUTO(3, Type.TYPE_COPTER, "Auto"),
    COPTER_GUIDED(4, Type.TYPE_COPTER, "Guided"),
    COPTER_LOITER(5, Type.TYPE_COPTER, "Loiter"),
    COPTER_RTL(6, Type.TYPE_COPTER, "RTL"),
    COPTER_CIRCLE(7, Type.TYPE_COPTER, "Circle"),
    COPTER_LAND(9, Type.TYPE_COPTER, "Land"),
    COPTER_DRIFT(11, Type.TYPE_COPTER, "Drift"),
    COPTER_SPORT(13, Type.TYPE_COPTER, "Sport"),
    COPTER_FLIP(14, Type.TYPE_COPTER, "Flip"),
    COPTER_AUTOTUNE(15, Type.TYPE_COPTER, "Autotune"),
    COPTER_POSHOLD(16, Type.TYPE_COPTER, "PosHold"),
    COPTER_BRAKE(17,Type.TYPE_COPTER,"Brake"),
    COPTER_THROW(18,Type.TYPE_COPTER,"Throw"),
    COPTER_AVOID_ADSB(19,Type.TYPE_COPTER,"AvoidADSP"),
    COPTER_GUIDED_NOGPS(20,Type.TYPE_COPTER,"GuidedNoGPS"),
    COPTER_SMART_RTL(21,Type.TYPE_COPTER,"SmartRTL"),
    COPTER_FLOWHOLD(22,Type.TYPE_COPTER,"FollowHold"),
    COPTER_FOLLOW(23,Type.TYPE_COPTER,"Follow"),
    COPTER_ZIGZAG(24,Type.TYPE_COPTER,"ZigZag"),
    COPTER_SYSTEMID(25,Type.TYPE_COPTER,"SystemID"),
    COPTER_AUTOROTATE(26,Type.TYPE_COPTER,"AutoRotate"),
    COPTER_AUTO_RTL(27,Type.TYPE_COPTER,"AutoRTL"),
    COPTER_TURTLE(28,Type.TYPE_COPTER,"Turtle"),

    ROVER_MANUAL(0, Type.TYPE_ROVER, "Manual"),
    ROVER_ACRO(1, Type.TYPE_ROVER, "Acro"),
    ROVER_STEERING(3, Type.TYPE_ROVER, "Steering"),
    ROVER_HOLD(4, Type.TYPE_ROVER, "Hold"),
    ROVER_LOITER(5, Type.TYPE_ROVER, "Loiter"),
    ROVER_FOLLOW(6, Type.TYPE_ROVER, "Follow"),
    ROVER_SIMPLE(7, Type.TYPE_ROVER, "Simple"),
    ROVER_AUTO(10, Type.TYPE_ROVER, "Auto"),
    ROVER_RTL(11, Type.TYPE_ROVER, "RTL"),
    ROVER_SMART_RTL(12, Type.TYPE_ROVER, "SmartRTL"),
    ROVER_GUIDED(15, Type.TYPE_ROVER, "Guided"),
    ROVER_INITIALIZING(16, Type.TYPE_ROVER, "Initializing"),

    UNKNOWN(-1, Type.TYPE_UNKNOWN, "Unknown");


    private final int mode;
    private final int droneType;
    private final String label;

    VehicleMode(int mode, int droneType, String label){
        this.mode = mode;
        this.droneType = droneType;
        this.label = label;
    }

    public int getMode() {
        return mode;
    }

    public int getDroneType() {
        return droneType;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString(){
        return getLabel();
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags){
        dest.writeString(name());
    }

    public static final Creator<VehicleMode> CREATOR = new Creator<VehicleMode>() {
        @Override
        public VehicleMode createFromParcel(Parcel source) {
            return VehicleMode.valueOf(source.readString());
        }

        @Override
        public VehicleMode[] newArray(int size) {
            return new VehicleMode[size];
        }
    };

    public static List<VehicleMode> getVehicleModePerDroneType(int droneType){
        VehicleMode[] availableModes = VehicleMode.values();
        final List<VehicleMode> vehicleModes = new ArrayList<VehicleMode>(availableModes.length);

        for(VehicleMode vehicleMode: availableModes){
            if(vehicleMode.getDroneType() == droneType)
                vehicleModes.add(vehicleMode);
        }

        return vehicleModes;
    }
}

