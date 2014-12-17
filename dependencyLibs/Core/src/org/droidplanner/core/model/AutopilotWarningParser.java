package org.droidplanner.core.model;

/**
 * Parse received autopilot warning messages.
 */
public interface AutopilotWarningParser {

    String parseWarning(String warning);
}
