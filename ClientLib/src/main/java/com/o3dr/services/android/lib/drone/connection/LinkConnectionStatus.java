package com.o3dr.services.android.lib.drone.connection;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

/**
 * Conveys information if the connection attempt fails.
 */
public final class LinkConnectionStatus implements Parcelable {
    @StringDef({
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        FAILED
    })
    public @interface StatusCode{}
    public static final String CONNECTED = "CONNECTED";
    public static final String CONNECTING = "CONNECTING";
    public static final String DISCONNECTED = "DISCONNECTED";
    public static final String FAILED = "FAILED";

    public static final String EXTRA_ERROR_CODE_KEY = "extra_error_code";
    public static final String EXTRA_ERROR_MSG_KEY = "extra_error_message";
    public static final String EXTRA_CONNECTION_TIME = "extra_connection_time";

    @IntDef({
        SYSTEM_UNAVAILABLE,
        LINK_UNAVAILABLE,
        PERMISSION_DENIED,
        INVALID_CREDENTIALS,
        TIMEOUT,
        ADDRESS_IN_USE,
        UNKNOWN
    })
    public @interface ErrorCode{}
    public static final int SYSTEM_UNAVAILABLE = -1;
    public static final int LINK_UNAVAILABLE = -2;
    public static final int PERMISSION_DENIED = -3;
    public static final int INVALID_CREDENTIALS = -4;
    public static final int TIMEOUT = -5;
    public static final int ADDRESS_IN_USE = -6;
    public static final int UNKNOWN = -7;

    private final @StatusCode String mStatusCode;
    private final Bundle mExtras;

    public LinkConnectionStatus(@StatusCode String errorCode, Bundle extras) {
        this.mStatusCode = errorCode;
        this.mExtras = extras;
    }

    public @StatusCode String getStatusCode() {
        return mStatusCode;
    }

    public Bundle getExtras() {
        return mExtras;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mStatusCode);
        dest.writeBundle(this.mExtras);
    }

    private LinkConnectionStatus(Parcel in) {
        @StatusCode String statusCode = in.readString();

        this.mStatusCode = statusCode;
        this.mExtras = in.readBundle();
    }

    public static final Parcelable.Creator<LinkConnectionStatus> CREATOR = new Parcelable.Creator<LinkConnectionStatus>() {
        public LinkConnectionStatus createFromParcel(Parcel source) {
            return new LinkConnectionStatus(source);
        }

        public LinkConnectionStatus[] newArray(int size) {
            return new LinkConnectionStatus[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkConnectionStatus that = (LinkConnectionStatus) o;

        if (mStatusCode != null ? !mStatusCode.equals(that.mStatusCode) : that.mStatusCode != null) {
            return false;
        }
        return !(mExtras != null ? !mExtras.equals(that.mExtras) : that.mExtras != null);

    }

    @Override
    public int hashCode() {
        int result = mStatusCode != null ? mStatusCode.hashCode() : 0;
        result = 31 * result + (mExtras != null ? mExtras.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConnectionResult{" +
            "mStatusCode='" + mStatusCode + '\'' +
            ", mExtras=" + mExtras +
            '}';
    }
}
