package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fhuya on 10/28/14.
 */
public class Battery implements DroneAttribute {

    private static final int INT16_MAX = 65535;

    private double batteryVoltage;
    private double batteryRemain;
    private double batteryCurrent;
    private Double batteryDischarge;
    private int currentConsumed;
    private short temperature;
    private short batteryFunction;
    private int[] voltages;

    private boolean mHasCellVoltages;
    private boolean mHasTemperature;

    public Battery(){}

    public Battery(double batteryVoltage, double batteryRemain, double batteryCurrent,
                   Double batteryDischarge) {
        this.batteryVoltage = batteryVoltage;
        this.batteryRemain = batteryRemain;
        this.batteryCurrent = batteryCurrent;
        this.batteryDischarge = batteryDischarge;
    }

    public void setBatteryVoltage(double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public void setBatteryRemain(double batteryRemain) {
        this.batteryRemain = batteryRemain;
    }

    public void setBatteryCurrent(double batteryCurrent) {
        this.batteryCurrent = batteryCurrent;
    }

    public void setBatteryDischarge(Double batteryDischarge) {
        this.batteryDischarge = batteryDischarge;
    }

    public void setCurrentConsumed(int consumed) {
        currentConsumed = consumed;
    }

    public void setTemperature(short temperature) {
        this.temperature = temperature;
        this.mHasTemperature = (temperature > 0);
    }

    public void setCellVoltages(int[] voltages) {
        this.voltages = voltages;
        this.mHasCellVoltages = (voltages != null);
    }

    public double getBatteryVoltage() {
        return batteryVoltage;
    }

    public double getBatteryRemain() {
        return batteryRemain;
    }

    public double getBatteryCurrent() {
        return batteryCurrent;
    }

    public Double getBatteryDischarge() {
        return batteryDischarge;
    }

    public int getCurrentConsumed() { return currentConsumed; }

    public short getTemperature() { return temperature; }
    public boolean hasTemperature() { return mHasTemperature; }

    public int[] getCellVoltages() { return voltages; }
    public boolean hasCellVoltages() { return mHasCellVoltages; }

    public short getBatteryFunction() { return batteryFunction; }
    public void setBatteryFunction(short func) { batteryFunction = func; }

    public List<Integer> getValidCellVoltages() {
        final List<Integer> list = new ArrayList<>();

        if(voltages != null) {
            for(int i = 0, size = voltages.length; i < size; ++i) {
                if(voltages[i] < INT16_MAX) {
                    list.add(voltages[i]);
                }
            }
        }

        return list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.batteryVoltage);
        dest.writeDouble(this.batteryRemain);
        dest.writeDouble(this.batteryCurrent);
        dest.writeValue(this.batteryDischarge);

        dest.writeInt(this.currentConsumed);
        dest.writeInt(this.temperature);
        dest.writeInt(this.batteryFunction);

        int len = (this.voltages != null)? this.voltages.length: 0;
        dest.writeInt(len);
        dest.writeIntArray((this.voltages != null)? this.voltages: new int[0]);
    }

    private Battery(Parcel in) {
        this.batteryVoltage = in.readDouble();
        this.batteryRemain = in.readDouble();
        this.batteryCurrent = in.readDouble();
        this.batteryDischarge = (Double) in.readValue(Double.class.getClassLoader());
        this.currentConsumed = in.readInt();
        this.temperature = (short)in.readInt();
        this.batteryFunction = (short)in.readInt();

        this.voltages = new int[in.readInt()];
        in.readIntArray(this.voltages);
    }

    public static final Creator<Battery> CREATOR = new Creator<Battery>() {
        public Battery createFromParcel(Parcel source) {
            return new Battery(source);
        }

        public Battery[] newArray(int size) {
            return new Battery[size];
        }
    };

    @Override
    public String toString() {
        return "Battery{" +
                "batteryVoltage=" + batteryVoltage +
                ", batteryRemain=" + batteryRemain +
                ", batteryCurrent=" + batteryCurrent +
                ", batteryDischarge=" + batteryDischarge +
                ", currentConsumed=" + currentConsumed +
                ", temperature=" + temperature +
                ", batteryFunction=" + batteryFunction +
                ", voltages=" + Arrays.toString(voltages) +
                '}';
    }
}
