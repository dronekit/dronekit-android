package org.droidplanner.services.android.interfaces;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;

/**
* Created by fhuya on 11/2/14.
*/
public interface DroneEventsListener extends DroneInterfaces.OnDroneListener,
        DroneInterfaces.OnParameterManagerListener, MagnetometerCalibration
                .OnMagCalibrationListener {}
