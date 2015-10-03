package org.droidplanner.services.android.core.drone.variables;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.services.android.core.MAVLink.MavLinkWaypoint;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.core.drone.DroneVariable;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

public class Home extends DroneVariable implements DroneInterfaces.OnDroneListener {
    public static final int HOME_WAYPOINT_INDEX = 0;

    private LatLongAlt coordinate;

    public Home(MavLinkDrone drone) {
        super(drone);
        drone.addDroneListener(this);
    }

    public boolean isValid() {
        return (coordinate != null);
    }

    public Home getHome() {
        return this;
    }

    public LatLongAlt getCoord() {
        return coordinate;
    }

    public double getAltitude() {
        return coordinate == null ? 0 : coordinate.getAltitude();
    }

    public void setHome(msg_mission_item msg) {

        if (this.coordinate == null) {
            this.coordinate = new LatLongAlt(msg.x, msg.y, msg.z);
        } else if (this.coordinate.getLatitude() != msg.x
                || this.coordinate.getLongitude() != msg.y
                || this.coordinate.getAltitude() != msg.z) {
            this.coordinate.setLatitude(msg.x);
            this.coordinate.setLongitude(msg.y);
            this.coordinate.setAltitude(msg.z);
        }

        myDrone.notifyDroneEvent(DroneEventsType.HOME);
    }

    public msg_mission_item packMavlink() {
        msg_mission_item mavMsg = new msg_mission_item();
        mavMsg.autocontinue = 1;
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
        mavMsg.current = 0;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
        mavMsg.target_system = myDrone.getSysid();
        mavMsg.target_component = myDrone.getCompid();
        if (isValid()) {
            mavMsg.x = (float) coordinate.getLatitude();
            mavMsg.y = (float) coordinate.getLongitude();
            mavMsg.z = (float) coordinate.getAltitude();
        }

        return mavMsg;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case EKF_POSITION_STATE_UPDATE:
                if (drone.getState().isEkfPositionOk())
                    requestHomeUpdate(myDrone);
                break;
        }
    }

    public void requestHomeUpdate() {
        requestHomeUpdate(myDrone);
    }

    private static void requestHomeUpdate(MavLinkDrone drone) {
        MavLinkWaypoint.requestWayPoint(drone, HOME_WAYPOINT_INDEX);
    }
}
