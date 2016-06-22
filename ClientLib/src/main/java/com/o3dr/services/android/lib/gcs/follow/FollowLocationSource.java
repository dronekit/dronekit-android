package com.o3dr.services.android.lib.gcs.follow;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Location source for Follow
 */
public enum FollowLocationSource implements Parcelable {
    Internal("Device GPS"),
    External("Client Specified")
    ;

    final String mLabel;

    FollowLocationSource(String label) {
        mLabel = label;
    }

    public String getLabel() { return mLabel; }

    @Override
    public String toString() {
        return getLabel();
    }

    public static FollowLocationSource fromOrdinal(int ordinal) {
        for(FollowLocationSource source: values()) {
            if(source.ordinal() == ordinal) {
                return source;
            }
        }

        return Internal;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }

    public static final Parcelable.Creator<FollowLocationSource> CREATOR = new Parcelable.Creator<FollowLocationSource>() {
        public FollowLocationSource createFromParcel(Parcel source) {
            return FollowLocationSource.valueOf(source.readString());
        }

        public FollowLocationSource[] newArray(int size) {
            return new FollowLocationSource[size];
        }
    };
}
