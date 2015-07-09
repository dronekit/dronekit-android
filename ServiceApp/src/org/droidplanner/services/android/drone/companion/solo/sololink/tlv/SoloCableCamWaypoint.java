package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.nio.ByteBuffer;

/**
 *  Send the app our cable cam waypoint when it’s recorded.
 */
public class SoloCableCamWaypoint extends TLVPacket {

    private LatLongAlt coordinate;

    /**
     * Yaw in degrees
     */
    private float degreesYaw;

    /**
     * Camera pitch in degrees
     */
    private float pitch;

    public SoloCableCamWaypoint(double latitude, double longitude, float altitude, float degreesYaw, float pitch) {
        super(TLVMessageTypes.TYPE_SOLO_CABLE_CAM_WAYPOINT, 28);
        this.coordinate = new LatLongAlt(latitude, longitude, altitude);
        this.degreesYaw = degreesYaw;
        this.pitch = pitch;
    }

    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    public float getDegreesYaw() {
        return degreesYaw;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putDouble(coordinate.getLatitude());
        valueCarrier.putDouble(coordinate.getLongitude());
        valueCarrier.putFloat((float) coordinate.getAltitude());
        valueCarrier.putFloat(degreesYaw);
        valueCarrier.putFloat(pitch);
    }
}
