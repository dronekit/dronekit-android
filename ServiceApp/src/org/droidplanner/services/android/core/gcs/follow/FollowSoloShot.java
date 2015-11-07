package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloMessageLocation;

import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.core.drone.autopilot.apm.solo.ArduSolo;
import org.droidplanner.services.android.core.drone.autopilot.apm.solo.SoloComp;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.gcs.roi.ROIEstimator;

/**
 * Created by Fredia Huya-Kouadio on 8/3/15.
 */
public class FollowSoloShot extends FollowAlgorithm {

    private final SoloComp soloComp;

    private final LatLongAlt locationCoord = new LatLongAlt(0, 0, 0);
    private final SoloMessageLocation locationSetter = new SoloMessageLocation(locationCoord);

    public FollowSoloShot(DroneManager droneMgr, Handler handler) {
        super(droneMgr, handler);
        final ArduSolo drone = (ArduSolo) droneMgr.getDrone();
        this.soloComp = drone.getSoloComp();
    }

    @Override
    public void enableFollow() {
        super.enableFollow();
        soloComp.enableFollowDataConnection();
    }

    @Override
    public void disableFollow() {
        super.disableFollow();
        soloComp.disableFollowDataConnection();
    }

    @Override
    protected void processNewLocation(Location location) {
        if (location != null) {
            final LatLongAlt receivedCoord = location.getCoord();

            locationCoord.setAltitude(receivedCoord.getAltitude());
            locationCoord.setLatitude(receivedCoord.getLatitude());
            locationCoord.setLongitude(receivedCoord.getLongitude());

            locationSetter.setCoordinate(locationCoord);

            soloComp.updateFollowCenter(locationSetter);
        }
    }

    @Override
    public FollowModes getType() {
        return FollowModes.SOLO_SHOT;
    }

    @Override
    protected ROIEstimator initROIEstimator(MavLinkDrone drone, Handler handler) {
        return null;
    }
}
