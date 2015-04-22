package com.o3dr.services.android.lib.drone.mission;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a set of mission items.
 */
public class Mission implements Parcelable {

    private int currentMissionItem;
    private final List<MissionItem> missionItemsList = new ArrayList<MissionItem>();

    public Mission() {}

    public void addMissionItem(MissionItem missionItem){
        missionItemsList.add(missionItem);
    }

    public void addMissionItem(int index, MissionItem missionItem){
        missionItemsList.add(index, missionItem);
    }

    public void removeMissionItem(MissionItem missionItem){
        missionItemsList.remove(missionItem);
    }

    public void removeMissionItem(int index){
        missionItemsList.remove(index);
    }

    public void clear(){
        missionItemsList.clear();
    }

    public MissionItem getMissionItem(int index){
        return missionItemsList.get(index);
    }

    public List<MissionItem> getMissionItems(){
        return missionItemsList;
    }

    public int getCurrentMissionItem() {
        return currentMissionItem;
    }

    public void setCurrentMissionItem(int currentMissionItem) {
        this.currentMissionItem = currentMissionItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.currentMissionItem);

        List<Bundle> missionItemsBundles = new ArrayList<Bundle>(missionItemsList.size());
        if(!missionItemsList.isEmpty()){
            for(MissionItem missionItem : missionItemsList){
                missionItemsBundles.add(missionItem.getType().storeMissionItem(missionItem));
            }
        }

        dest.writeTypedList(missionItemsBundles);
    }

    private Mission(Parcel in) {
        this.currentMissionItem = in.readInt();

        List<Bundle> missionItemsBundles = new ArrayList<>();
        in.readTypedList(missionItemsBundles, Bundle.CREATOR);
        if(!missionItemsBundles.isEmpty()){
            for(Bundle bundle : missionItemsBundles){
                missionItemsList.add(MissionItemType.restoreMissionItemFromBundle(bundle));
            }
        }
    }

    public static final Creator<Mission> CREATOR = new Creator<Mission>() {
        public Mission createFromParcel(Parcel source) {
            return new Mission(source);
        }

        public Mission[] newArray(int size) {
            return new Mission[size];
        }
    };
}
