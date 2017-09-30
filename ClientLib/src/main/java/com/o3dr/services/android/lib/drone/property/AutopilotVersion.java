package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

/**
 * Stores information about the drone's various versions.
 */
public class AutopilotVersion implements DroneAttribute {

    /**
     * bitmask of capabilities (see MAV_PROTOCOL_CAPABILITY enum)
     */
    public long capabilities;

    /**
     * UID if provided by hardware
     */
    public long uid;

    /**
     * Firmware version number
     */
    public long flightSwVersion;

    /**
     * Middleware version number
     */
    public long middlewareSwVersion;

    /**
     * Operating system version number
     */
    public long osSwVersion;

    /**
     * HW / board version (last 8 bytes should be silicon ID, if any)
     */
    public long boardVersion;

    /**
     * ID of the board vendor
     */
    public int vendorId;

    /**
     * ID of the product
     */
    public int productId;

    public AutopilotVersion() {
        super();
    }

    public AutopilotVersion(
            long capabilities, long uid, long flightSwVersion, long middlewareSwVersion,
            long osSwVersion, long boardVersion, int vendorId, int productId) {
        this();
        this.capabilities = capabilities;
        this.uid = uid;
        this.flightSwVersion = flightSwVersion;
        this.middlewareSwVersion = middlewareSwVersion;
        this.osSwVersion = osSwVersion;
        this.boardVersion = boardVersion;
        this.vendorId = vendorId;
        this.productId = productId;
    }

    public long getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(long capabilities) {
        this.capabilities = capabilities;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getFlightSwVersion() {
        return flightSwVersion;
    }

    public void setFlightSwVersion(long flightSwVersion) {
        this.flightSwVersion = flightSwVersion;
    }

    public long getMiddlewareSwVersion() {
        return middlewareSwVersion;
    }

    public void setMiddlewareSwVersion(long middlewareSwVersion) {
        this.middlewareSwVersion = middlewareSwVersion;
    }

    public long getOsSwVersion() {
        return osSwVersion;
    }

    public void setOsSwVersion(long osSwVersion) {
        this.osSwVersion = osSwVersion;
    }

    public long getBoardVersion() {
        return boardVersion;
    }

    public void setBoardVersion(long boardVersion) {
        this.boardVersion = boardVersion;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "AutopilotVersion{" +
                "capabilities=" + capabilities +
                ", uid=" + uid +
                ", flightSwVersion=" + flightSwVersion +
                ", middlewareSwVersion=" + middlewareSwVersion +
                ", osSwVersion=" + osSwVersion +
                ", boardVersion=" + boardVersion +
                ", vendorId=" + vendorId +
                ", productId=" + productId +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(capabilities);
        dest.writeLong(uid);
        dest.writeLong(flightSwVersion);
        dest.writeLong(middlewareSwVersion);
        dest.writeLong(osSwVersion);
        dest.writeLong(boardVersion);
        dest.writeInt(vendorId);
        dest.writeInt(productId);
    }

    private AutopilotVersion(Parcel in) {
        this.capabilities = in.readLong();
        this.uid = in.readLong();
        this.flightSwVersion = in.readLong();
        this.middlewareSwVersion = in.readLong();
        this.osSwVersion = in.readLong();
        this.boardVersion = in.readLong();
        this.vendorId = in.readInt();
        this.productId = in.readInt();
    }

    public static final Creator<AutopilotVersion> CREATOR = new Creator<AutopilotVersion>() {
        public AutopilotVersion createFromParcel(Parcel source) {
            return new AutopilotVersion(source);
        }

        public AutopilotVersion[] newArray(int size) {
            return new AutopilotVersion[size];
        }
    };
}
