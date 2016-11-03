package com.o3dr.services.android.lib.coordinate;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores latitude, longitude, and altitude information for a coordinate.
 */

public class LatLongAlt extends LatLong {

    private static final long serialVersionUID =-4771550293045623743L;

    /**
     * Stores the altitude in meters.
     */
    private double mAltitude;
    private Frame mFrame;

    public LatLongAlt() {
        super();
        mAltitude = 0.0;
        mFrame = Frame.GLOBAL_RELATIVE;
    }

    public LatLongAlt(double latitude, double longitude, double altitude, Frame frame) {
        super(latitude, longitude);
        mAltitude = altitude;
        mFrame = frame;
    }

    public LatLongAlt(LatLong location, double altitude, Frame frame){
        super(location);
        mAltitude = altitude;
        mFrame = frame;
    }

    public LatLongAlt(LatLongAlt copy) {
        this(copy.getLatitude(), copy.getLongitude(), copy.getAltitude(), copy.getFrame());
    }

    public void set(LatLongAlt source){ // TODO: this looks wrong
        super.set(source);
        this.mAltitude = source.mAltitude;
        this.mFrame = source.mFrame;
    }

    /**
     * @return the altitude in meters
     */
    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double altitude) {
        this.mAltitude = altitude;
    }

    public Frame getFrame() {
        return mFrame;
    }

    public void setFrame(Frame frame) {
        mFrame = frame;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LatLongAlt)) return false;
        if (!super.equals(o)) return false;

        LatLongAlt that = (LatLongAlt) o;

        if ((Double.compare(that.mAltitude, mAltitude) != 0)
            && (that.mFrame.asInt() != mFrame.asInt()) )
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(mAltitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32)); // TODO Check this hash is OK with frame
        return result;
    }

    @Override
    public String toString() {
        final String superToString = super.toString();
        return "LatLongAlt{" +
                superToString +
                ", mAltitude=" + mAltitude +
                ", mFrame=" + mFrame.getAbbreviation() +
                '}';
    }

    public static final Parcelable.Creator<LatLongAlt> CREATOR = new Parcelable.Creator<LatLongAlt>
            () {
        public LatLongAlt createFromParcel(Parcel source) {
            return (LatLongAlt) source.readSerializable();
        }

        public LatLongAlt[] newArray(int size) {
            return new LatLongAlt[size];
        }
    };
}
