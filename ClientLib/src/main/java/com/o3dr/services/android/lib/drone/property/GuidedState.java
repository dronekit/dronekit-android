package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.services.android.impl.core.drone.variables.GuidedPoint;

/**
 * Created by fhuya on 11/5/14.
 */
public class GuidedState implements DroneAttribute {

    public static final int STATE_UNINITIALIZED = 0;
    public static final int STATE_IDLE = 1;
    public static final int STATE_ACTIVE = 2;

    private int state;
    private LatLongAlt coordinate;

    public GuidedState(){}

    public GuidedState(int state, LatLongAlt coordinate) {
        this.state = state;
        this.coordinate = coordinate;
    }

    public boolean isActive(){
        return state == STATE_ACTIVE;
    }

    public boolean isIdle(){
        return state == STATE_IDLE;
    }

    public boolean isInitialized(){
        return state != STATE_UNINITIALIZED;
    }

    public LatLongAlt getCoordinate(){
        return coordinate;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setCoordinate(LatLongAlt coordinate) {
        this.coordinate = coordinate;
    }

    public static GuidedState getGuidedStateFromPoint(GuidedPoint guidedPoint) {
        if (guidedPoint == null)
            return new GuidedState();

        int guidedState;
        switch (guidedPoint.getState()) {
            default:
            case UNINITIALIZED:
                guidedState = GuidedState.STATE_UNINITIALIZED;
                break;

            case ACTIVE:
                guidedState = GuidedState.STATE_ACTIVE;
                break;

            case IDLE:
                guidedState = GuidedState.STATE_IDLE;
                break;
        }
        return new GuidedState(guidedState, guidedPoint.getCoord());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state);
        dest.writeParcelable(this.coordinate, flags);
    }

    private GuidedState(Parcel in) {
        this.state = in.readInt();
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
    }

    public static final Parcelable.Creator<GuidedState> CREATOR = new Parcelable.Creator<GuidedState>() {
        public GuidedState createFromParcel(Parcel source) {
            return new GuidedState(source);
        }

        public GuidedState[] newArray(int size) {
            return new GuidedState[size];
        }
    };
}
