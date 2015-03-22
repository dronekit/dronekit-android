package org.droidplanner.services.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.communication.service.MAVLinkClient;
import org.droidplanner.services.android.utils.AndroidApWarningParser;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.services.android.mock.MockMavLinkServiceAPI;

/**
 * Created by djmedina on 3/5/15.
 * This is a simple test case.
 */
public class BasicTest extends AndroidTestCase {

    private Drone drone;
    private MockMavLinkServiceAPI mavlinkApi;

    private final DroneInterfaces.Handler dpHandler = new DroneInterfaces.Handler() {

        private final Handler h = new Handler();

        public void removeCallbacks(Runnable thread) {
            h.removeCallbacks(thread);
        }

        @Override
        public void post(Runnable thread) {
            h.post(thread);
        }

        @Override
        public void postDelayed(Runnable thread, long timeout) {
            h.postDelayed(thread, timeout);
        }
    };

    private final MAVLinkStreams.MavlinkInputStream inputStreamListener = new MAVLinkStreams.MavlinkInputStream() {
        @Override
        public void notifyStartingConnection() {}

        @Override
        public void notifyConnected() {}

        @Override
        public void notifyDisconnected() {}

        @Override
        public void notifyReceivedData(MAVLinkPacket packet) {}

        @Override
        public void onStreamError(String errorMsg) {}
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ConnectionParameter connParams = new ConnectionParameter(0, new Bundle(), null);
        mavlinkApi = new MockMavLinkServiceAPI();
        DroneInterfaces.Clock clock = new DroneInterfaces.Clock() {
            @Override
            public long elapsedRealtime() {
                return SystemClock.elapsedRealtime();
            }
        };
        DroidPlannerPrefs dpPrefs = new DroidPlannerPrefs(this.getContext());
        MAVLinkClient mavClient = new MAVLinkClient(this.getContext(), inputStreamListener, connParams, mavlinkApi);

        drone = new DroneImpl(mavClient, clock, dpHandler, dpPrefs, new AndroidApWarningParser(this.getContext()));
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @SmallTest
    public void testPreconditions() {
    }

    /**
     * Basic MAVLink message test
     */
    @SmallTest
    public void testArm() {
        MavLinkArm.sendArmMessage(drone, true);
        MAVLinkPacket data = mavlinkApi.getData();
        assertTrue(data != null);

        //Unpack the message into the right MAVLink message type
        MAVLinkMessage msg = data.unpack();
        msg_command_long longMsg = (msg_command_long) msg;

        //Validate the message
        assertEquals(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, longMsg.command);
        assertEquals(1f, longMsg.param1);
    }
}
