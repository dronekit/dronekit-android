package org.droidplanner.services.android.core.drone;

import android.os.Bundle;

import android.os.Bundle;

import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.model.IApiListener;

import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;

/**
 * Created by fhuya on 11/2/14.
 */
public interface DroneEventsListener extends DroneInterfaces.OnDroneListener,
        DroneInterfaces.OnParameterManagerListener, MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener {

    DroneSharePrefs getDroneSharePrefs();

    void onConnectionFailed(String error);

    void onReceivedMavLinkMessage(MAVLinkMessage msg);

    void onMessageLogged(int logLevel, String message);

    void onAttributeEvent(String attributeEvent, Bundle eventInfo);

    int getApiVersionCode();

    int getClientVersionCode();
}
