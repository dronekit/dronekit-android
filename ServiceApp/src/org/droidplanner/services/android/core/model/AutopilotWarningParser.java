package org.droidplanner.services.android.core.model;

import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

/**
 * Parse received autopilot warning messages.
 */
public interface AutopilotWarningParser {

    String getDefaultWarning();

    String parseWarning(MavLinkDrone drone, String warning);
}
