package com.o3dr.services.android.lib.gcs.returnToMe;

import android.os.Parcel;
import android.support.annotation.IntDef;

import com.o3dr.services.android.lib.drone.property.DroneAttribute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Fredia Huya-Kouadio on 9/22/15.
 */
public class ReturnToMeState implements DroneAttribute {

    @IntDef({STATE_IDLE, STATE_USER_LOCATION_UNAVAILABLE, STATE_USER_LOCATION_INACCURATE,
            STATE_WAITING_FOR_VEHICLE_GPS, STATE_UPDATING_HOME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReturnToMeStates{}

    public static final int STATE_IDLE = 0;
    public static final int STATE_USER_LOCATION_UNAVAILABLE = 1;
    public static final int STATE_USER_LOCATION_INACCURATE = 2;
    public static final int STATE_WAITING_FOR_VEHICLE_GPS = 3;
    public static final int STATE_UPDATING_HOME = 4;

    @ReturnToMeStates
    private int state = STATE_IDLE;

    public ReturnToMeState(){}

    public ReturnToMeState(@ReturnToMeStates int state){
        this.state = state;
    }

    @ReturnToMeStates
    public int getState() {
        return state;
    }

    public void setState(@ReturnToMeStates int state) {
        this.state = state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state);
    }

    protected ReturnToMeState(Parcel in) {
        @ReturnToMeStates final int temp = in.readInt();
        this.state = temp;
    }

    public static final Creator<ReturnToMeState> CREATOR = new Creator<ReturnToMeState>() {
        public ReturnToMeState createFromParcel(Parcel source) {
            return new ReturnToMeState(source);
        }

        public ReturnToMeState[] newArray(int size) {
            return new ReturnToMeState[size];
        }
    };
}
