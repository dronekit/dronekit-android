package com.o3dr.services.android.lib.coordinate;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Stores latitude and longitude in degrees.
 */
public class LatLong implements Parcelable, Serializable {

    private static final long serialVersionUID =-5809863197722412339L;

    /**
     * Stores latitude, and longitude in degrees
     */
    private double latitude;
    private double longitude;

    public LatLong(){
        this.latitude = -100.0; // TODO: Should we set this to invalid ie -100.0 ?
        this.longitude = -190.0;// TODO: Should we set this to invalid ie -190.0 ?
    }

    public LatLong(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLong(LatLong copy){
        this(copy.getLatitude(), copy.getLongitude());
    }

    public void set(LatLong update){
        this.latitude = update.latitude;
        this.longitude = update.longitude;
    }

    /**
     * @return if this is a valid LatLong global point
     */
    public boolean isValid() {

        if ( this.longitude > 180.0 || this.latitude > 90.0
            || this.longitude < -180.0 || this.latitude < -90.0 ) {
            return false; // Not a valid location

        } if (Double.compare(this.longitude, 0.0) == 0 && Double.compare(this.latitude, 0.0) == 0) {
            return false; // Rarely in 0.0,0.0 a valid location, so reject.

        } else {
            return true;
        }
    }

    /**
     * @return the latitude in degrees
     */
    public double getLatitude(){
        return latitude;
    }

    /**
     * @return the longitude in degrees
     */
    public double getLongitude(){
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLong dot(double scalar) {
        return new LatLong(latitude * scalar, longitude * scalar);
    }

    public LatLong negate() {
        return new LatLong(latitude * -1, longitude * -1);
    }

    public LatLong subtract(LatLong coord) {
        return new LatLong(latitude - coord.latitude, longitude - coord.longitude);
    }

    public LatLong sum(LatLong coord) {
        return new LatLong(latitude + coord.latitude, longitude + coord.longitude);
    }

    public static LatLong sum(LatLong... toBeAdded) {
        double latitude = 0;
        double longitude = 0;
        for (LatLong coord : toBeAdded) {
            latitude += coord.latitude;
            longitude += coord.longitude;
        }
        return new LatLong(latitude, longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LatLong)) return false;

        LatLong latLong = (LatLong) o;

        if (Double.compare(latLong.latitude, latitude) != 0) return false;
        if (Double.compare(latLong.longitude, longitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LatLong{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Parcelable.Creator<LatLong> CREATOR = new Parcelable.Creator<LatLong>() {
        public LatLong createFromParcel(Parcel source) {
            return (LatLong) source.readSerializable();
        }

        public LatLong[] newArray(int size) {
            return new LatLong[size];
        }
    };
}
