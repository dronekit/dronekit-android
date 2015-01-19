package com.o3dr.services.android.lib.model.action;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wrapper for action exposed by the api.
 */
public class Action implements Parcelable {

    public final String type;
    public final Bundle data;

    public Action(String actionType){
        this.type = actionType;
        this.data = null;
    }

    public Action(String actionType, Bundle actionData){
        this.type = actionType;
        this.data = actionData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeBundle(data);
    }

    private Action(Parcel in) {
        this.type = in.readString();
        data = in.readBundle();
    }

    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
        public Action createFromParcel(Parcel source) {
            return new Action(source);
        }

        public Action[] newArray(int size) {
            return new Action[size];
        }
    };
}
