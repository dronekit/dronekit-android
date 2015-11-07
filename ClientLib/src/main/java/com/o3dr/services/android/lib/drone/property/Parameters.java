package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by fhuya on 10/28/14.
 */
public class Parameters implements DroneAttribute {

    private final List<Parameter> parametersList = new ArrayList<>();

    public Parameters(){
    }

    public Parameters(Collection<Parameter> parameterList) {
        setParametersList(parameterList);
    }

    public List<Parameter> getParameters(){
        return parametersList;
    }

    public Parameter getParameter(String name){
        if(TextUtils.isEmpty(name))
            return null;

        for(Parameter param: parametersList){
            if(param.getName().equalsIgnoreCase(name))
                return param;
        }

        return null;
    }

    public void setParametersList(Collection<Parameter> parametersList) {
        this.parametersList.clear();
        if(parametersList != null && !parametersList.isEmpty()) {
            this.parametersList.addAll(parametersList);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(parametersList);
    }

    private Parameters(Parcel in) {
        in.readTypedList(parametersList, Parameter.CREATOR);
    }

    public static final Creator<Parameters> CREATOR = new Creator<Parameters>() {
        public Parameters createFromParcel(Parcel source) {
            return new Parameters(source);
        }

        public Parameters[] newArray(int size) {
            return new Parameters[size];
        }
    };
}
