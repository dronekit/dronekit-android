package org.droidplanner.core.model;

/**
 * Parse received autopilot warning messages.
 */
public interface AutopilotWarningParser {

    String getDefaultWarning();

    String parseWarning(Drone drone, String warning);
}
