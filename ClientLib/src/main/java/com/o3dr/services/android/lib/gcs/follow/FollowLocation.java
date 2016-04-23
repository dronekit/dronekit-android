package com.o3dr.services.android.lib.gcs.follow;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kellys on 3/18/16.
 */
public class FollowLocation implements Parcelable {

    public static class Builder {
        private final FollowLocation loc;

        public Builder() {
            loc = new FollowLocation();
        }

        public Builder lat(double lat) { loc.setLat(lat); return this; }
        public Builder lng(double lng) { loc.setLng(lng); return this; }
        public Builder altitude(double alt) { loc.setAltitude(alt); return this; }
        public Builder speed(float f) { loc.setSpeed(f); return this; }
        public Builder heading(float f) { loc.setHeading(f); return this; }
        public Builder accuracy(float f) { loc.setAccuracy(f); return this; }
        public Builder time(long t) { loc.setTime(t); return this; }

        public FollowLocation build() { return loc; }
    }

    public static final Parcelable.Creator<FollowLocation> CREATOR =
            new Creator<FollowLocation>() {
                @Override
                public FollowLocation createFromParcel(Parcel source) {
                    return new FollowLocation(source);
                }

                @Override
                public FollowLocation[] newArray(int size) {
                    return new FollowLocation[size];
                }
            };

    private double lat = -1;
    private double lng = -1;
    private double altitude = -1;
    private float speed = -1f;
    private float heading = -1f;
    private float accuracy = -1f;
    private long time = -1l;

    public FollowLocation() {
        super();
    }

    public FollowLocation(Parcel in) {
        this();
        lat = in.readDouble();
        lng = in.readDouble();
        altitude = in.readDouble();
        speed = in.readFloat();
        heading = in.readFloat();
        accuracy = in.readFloat();
        time = in.readLong();
    }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public boolean hasAltitude() { return altitude >= 0; }
    public double getAltitude() { return altitude; }
    public void setAltitude(double altitude) { this.altitude = altitude; }

    public boolean hasSpeed() { return speed >= 0; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public boolean hasHeading() { return heading >= 0; }
    public float getHeading() { return heading; }
    public void setHeading(float heading) { this.heading = heading; }

    public boolean hasAccuracy() { return accuracy >= 0; }
    public float getAccuracy() { return accuracy; }
    public void setAccuracy(float accuracy) { this.accuracy = accuracy; }

    public boolean hasTime() { return time >= 0; }
    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeDouble(altitude);
        dest.writeFloat(speed);
        dest.writeFloat(heading);
        dest.writeFloat(accuracy);
        dest.writeLong(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "FollowLocation{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", altitude=" + altitude +
                ", speed=" + speed +
                ", heading=" + heading +
                ", accuracy=" + accuracy +
                ", time=" + time +
                '}';
    }
}
