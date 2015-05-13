package org.droidplanner.services.android.interfaces;

import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.calibration.MagnetometerCalibrationImpl;

/**
 * Created by fhuya on 11/2/14.
 */
public interface DroneEventsListener extends DroneInterfaces.OnDroneListener,
        DroneInterfaces.OnParameterManagerListener, MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener {

    DroneSharePrefs getDroneSharePrefs();

    void onConnectionFailed(String error);

    void onReceivedMavLinkMessage(MAVLinkMessage msg);

    void onMessageLogged(int logLevel, String message);
}
