package org.droidplanner.services.android.interfaces;

import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;

/**
* Created by fhuya on 11/2/14.
*/
public interface DroneEventsListener extends DroneInterfaces.OnDroneListener,
        DroneInterfaces.OnParameterManagerListener, MagnetometerCalibration.OnMagCalibrationListener {

    DroneSharePrefs getDroneSharePrefs();

    void onConnectionFailed(String error);

    void onReceivedMavLinkMessage(MAVLinkMessage msg);
}
