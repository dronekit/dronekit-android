package org.droidplanner.services.android.core.drone.autopilot.apm;

import com.github.zafarkhaja.semver.Version;

import junit.framework.TestCase;

/**
 * Unit tests for ArduPilot.
 */
public class ArduPilotTest extends TestCase {
    public void testExtractVersionNumber() throws Exception {
        Version version = ArduPilot.extractVersionNumber("APM:Copter V3.2");
        assertEquals(3, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());

        version = ArduPilot.extractVersionNumber("APM:Copter V3.3");
        assertEquals(3, version.getMajorVersion());
        assertEquals(3, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());
    }
}