package com.o3dr.services.android.lib.gcs.follow;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 11/5/14.
 */
public enum FollowType implements Parcelable {

    LEASH("Leash"),
    LEAD("Lead"),
    RIGHT("Right"),
    LEFT("Left"),
    CIRCLE("Circle"),
    ABOVE("Above", false),
    SPLINE_LEASH("Vector Leash"),
    SPLINE_ABOVE("Vector Above", false),
    GUIDED_SCAN("Guided Scan", false);

    private final String typeLabel;
    private final boolean hasRadius;

    private FollowType(String typeLabel){
        this.typeLabel = typeLabel;
        this.hasRadius = true;
    }

    private FollowType(String typeLabel, boolean hasRadius) {
        this.typeLabel = typeLabel;
        this.hasRadius = hasRadius;
    }

    public boolean hasRadius() {
        return hasRadius;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    @Override
    public String toString(){
        return getTypeLabel();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }

    public static List<FollowType> getFollowTypes(boolean includeAdvanced){
        List<FollowType> followTypes = new ArrayList<>();
        followTypes.add(LEASH);
        followTypes.add(LEAD);
        followTypes.add(RIGHT);
        followTypes.add(LEFT);
        followTypes.add(CIRCLE);
        followTypes.add(ABOVE);

        if(includeAdvanced){
            followTypes.add(SPLINE_LEASH);
            followTypes.add(SPLINE_ABOVE);
            followTypes.add(GUIDED_SCAN);
        }

        return followTypes;
    }

    public static final Parcelable.Creator<FollowType> CREATOR = new Parcelable.Creator<FollowType>() {
        public FollowType createFromParcel(Parcel source) {
            return FollowType.valueOf(source.readString());
        }

        public FollowType[] newArray(int size) {
            return new FollowType[size];
        }
    };
}
