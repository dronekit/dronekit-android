package org.droidplanner.core.drone.variables;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.core.MAVLink.MavLinkWaypoint;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;

public class Home extends DroneVariable implements DroneInterfaces.OnDroneListener {
    public static final int HOME_WAYPOINT_INDEX = 0;

    private Coord2D coordinate;
    private double altitude = 0;

    public Home(Drone drone) {
        super(drone);
        drone.addDroneListener(this);
    }

    public boolean isValid() {
        return (coordinate != null);
    }

    public Home getHome() {
        return this;
    }

    public Coord2D getCoord() {
        return coordinate;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setHome(msg_mission_item msg) {
        boolean homeLocationUpdated = false;

        if (this.coordinate == null) {
            this.coordinate = new Coord2D(msg.x, msg.y);
            homeLocationUpdated = true;
        } else if (this.coordinate.getLat() != msg.x || this.coordinate.getLng() != msg.y) {
            this.coordinate.set(msg.x, msg.y);
            homeLocationUpdated = true;
        }

        if (this.altitude != msg.z) {
            this.altitude = msg.z;
            homeLocationUpdated = true;
        }

        if (homeLocationUpdated)
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
            mavMsg.x = (float) coordinate.getLat();
            mavMsg.y = (float) coordinate.getLng();
            mavMsg.z = (float) altitude;
        }

        return mavMsg;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch(event){
            case EKF_POSITION_STATE_UPDATE:
                if(drone.getState().isEkfPositionOk())
                    requestHomeUpdate(myDrone);
                break;
        }
    }

    private static void requestHomeUpdate(Drone drone){
        MavLinkWaypoint.requestWayPoint(drone, HOME_WAYPOINT_INDEX);
    }
}
