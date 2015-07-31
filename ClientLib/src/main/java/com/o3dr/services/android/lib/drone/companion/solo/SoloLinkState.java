package com.o3dr.services.android.lib.drone.companion.solo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageParser;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;

/**
 * Stores state information for the sololink companion computer.
 * Created by Fredia Huya-Kouadio on 7/10/15.
 */
public class SoloLinkState implements DroneAttribute {

    private String wifiSsid;
    private String wifiPassword;

    private String controllerVersion;
    private String controllerFirmwareVersion;

    private String vehicleVersion;
    private String autopilotVersion;

    private boolean isEUTxPowerCompliant;

    private SparseArray<SoloButtonSetting> buttonSettings;

    public SoloLinkState(){}

    public SoloLinkState(String autopilotVersion, String controllerFirmwareVersion,
                         String controllerVersion, String vehicleVersion,
                         String wifiPassword, String wifiSsid, boolean isEUTxPowerCompliant,
                         SparseArray<SoloButtonSetting> buttonSettings) {
        this.autopilotVersion = autopilotVersion;
        this.controllerFirmwareVersion = controllerFirmwareVersion;
        this.controllerVersion = controllerVersion;
        this.vehicleVersion = vehicleVersion;
        this.wifiPassword = wifiPassword;
        this.wifiSsid = wifiSsid;
        this.isEUTxPowerCompliant = isEUTxPowerCompliant;
        this.buttonSettings = buttonSettings;
    }

    public String getAutopilotVersion() {
        return autopilotVersion;
    }

    public String getControllerFirmwareVersion() {
        return controllerFirmwareVersion;
    }

    public String getControllerVersion() {
        return controllerVersion;
    }

    public String getVehicleVersion() {
        return vehicleVersion;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public boolean isEUTxPowerCompliant() {
        return isEUTxPowerCompliant;
    }

    public SoloButtonSetting getButtonSetting(int buttonType){
        return buttonSettings.get(buttonType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.wifiSsid);
        dest.writeString(this.wifiPassword);
        dest.writeString(this.controllerVersion);
        dest.writeString(this.controllerFirmwareVersion);
        dest.writeString(this.vehicleVersion);
        dest.writeString(this.autopilotVersion);
        dest.writeByte(isEUTxPowerCompliant ? (byte) 1 : (byte) 0);
        dest.writeSparseArray((SparseArray) this.buttonSettings);
    }

    protected SoloLinkState(Parcel in) {
        this.wifiSsid = in.readString();
        this.wifiPassword = in.readString();
        this.controllerVersion = in.readString();
        this.controllerFirmwareVersion = in.readString();
        this.vehicleVersion = in.readString();
        this.autopilotVersion = in.readString();
        this.isEUTxPowerCompliant = in.readByte() != 0;
        this.buttonSettings = in.readSparseArray(SparseArray.class.getClassLoader());
    }

    public static final Creator<SoloLinkState> CREATOR = new Creator<SoloLinkState>() {
        public SoloLinkState createFromParcel(Parcel source) {
            return new SoloLinkState(source);
        }

        public SoloLinkState[] newArray(int size) {
            return new SoloLinkState[size];
        }
    };
}
