package org.droidplanner.services.android.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import junit.framework.TestCase;

import org.droidplanner.services.android.core.mission.Mission;

import java.util.List;

public class LandImplTest extends TestCase {

    public void testPackMissionItem() {
        Mission mission = new Mission(null);
        LandImpl item = new LandImpl(mission);

        List<msg_mission_item> listOfMsg = item.packMissionItem();
        assertEquals(1, listOfMsg.size());

        msg_mission_item msg = listOfMsg.get(0);

        assertEquals(MAV_CMD.MAV_CMD_NAV_LAND, msg.command);
        assertEquals(0.0f, msg.param1);
        assertEquals(0.0f, msg.param2);
        assertEquals(0.0f, msg.param3);
        assertEquals(0.0f, msg.param3);
    }

}
