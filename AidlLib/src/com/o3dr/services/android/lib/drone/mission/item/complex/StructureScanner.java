package com.o3dr.services.android.lib.drone.mission.item.complex;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class StructureScanner extends BaseSpatialItem implements MissionItem.ComplexItem<StructureScanner>,
        android.os.Parcelable {

    private double radius = 10;
    private double heightStep = 5;
    private int stepsCount = 2;
    private boolean crossHatch = false;
    private SurveyDetail surveyDetail = new SurveyDetail();
    private List<LatLong> path = new ArrayList<LatLong>();

    public StructureScanner(){
        super(MissionItemType.STRUCTURE_SCANNER);
    }

    public void copy(StructureScanner source){
        this.radius = source.radius;
        this.heightStep = source.heightStep;
        this.stepsCount = source.stepsCount;
        this.crossHatch = source.crossHatch;
        this.surveyDetail = source.surveyDetail;
        this.path = source.path;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getHeightStep() {
        return heightStep;
    }

    public void setHeightStep(double heightStep) {
        this.heightStep = heightStep;
    }

    public int getStepsCount() {
        return stepsCount;
    }

    public void setStepsCount(int stepsCount) {
        this.stepsCount = stepsCount;
    }

    public boolean isCrossHatch() {
        return crossHatch;
    }

    public void setCrossHatch(boolean crossHatch) {
        this.crossHatch = crossHatch;
    }

    public SurveyDetail getSurveyDetail() {
        return surveyDetail;
    }

    public void setSurveyDetail(SurveyDetail surveyDetail) {
        this.surveyDetail = surveyDetail;
    }

    public List<LatLong> getPath() {
        return path;
    }

    public void setPath(List<LatLong> points){
        this.path = points;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.radius);
        dest.writeDouble(this.heightStep);
        dest.writeInt(this.stepsCount);
        dest.writeByte(crossHatch ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.surveyDetail, 0);
        dest.writeTypedList(path);
    }

    private StructureScanner(Parcel in) {
        super(in);
        this.radius = in.readDouble();
        this.heightStep = in.readDouble();
        this.stepsCount = in.readInt();
        this.crossHatch = in.readByte() != 0;
        this.surveyDetail = in.readParcelable(SurveyDetail.class.getClassLoader());
        in.readTypedList(path, LatLong.CREATOR);
    }

    public static final Creator<StructureScanner> CREATOR = new Creator<StructureScanner>() {
        public StructureScanner createFromParcel(Parcel source) {
            return new StructureScanner(source);
        }

        public StructureScanner[] newArray(int size) {
            return new StructureScanner[size];
        }
    };
}
