package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.nio.ByteBuffer;

/**
 * Sent from app to solo to communicate a location.
 */
public class SoloMessageLocation extends TLVPacket {

    private LatLongAlt coordinate;

    public SoloMessageLocation(double latitude, double longitude, float altitudeInMeters) {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_LOCATION, 20);
        this.coordinate = new LatLongAlt(latitude, longitude, altitudeInMeters);
    }

    public SoloMessageLocation(LatLongAlt coordinate){
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_LOCATION, 20);
        this.coordinate = coordinate;
    }

    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLongAlt coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putDouble(coordinate.getLatitude());
        valueCarrier.putDouble(coordinate.getLongitude());
        valueCarrier.putFloat((float) coordinate.getAltitude());
    }
}
