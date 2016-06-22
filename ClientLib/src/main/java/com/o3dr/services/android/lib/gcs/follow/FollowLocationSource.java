package com.o3dr.services.android.lib.gcs.follow;

/**
 * Location source for Follow
 */
public enum FollowLocationSource {
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
}
