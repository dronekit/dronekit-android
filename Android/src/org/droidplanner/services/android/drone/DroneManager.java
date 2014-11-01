package org.droidplanner.services.android.drone;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

import com.MAVLink.Messages.MAVLinkMessage;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;
import org.droidplanner.services.android.communication.service.MAVLinkClient;
import org.droidplanner.services.android.location.FusedLocation;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;

/**
 * Created by fhuya on 11/1/14.
 */
public class DroneManager implements MAVLinkStreams.MavlinkInputStream {

    private final Handler handler = new Handler();
    private final Drone drone;
    private final Follow followMe;
    private final MavLinkMsgHandler mavLinkMsgHandler;

    public DroneManager(Context context, ConnectionParameter connParams){
        MAVLinkClient mavClient = new MAVLinkClient(context, this, connParams);

        DroneInterfaces.Clock clock = new DroneInterfaces.Clock() {
            @Override
            public long elapsedRealtime() {
                return SystemClock.elapsedRealtime();
            }
        };

        DroneInterfaces.Handler dpHandler = new DroneInterfaces.Handler() {
            @Override
            public void removeCallbacks(Runnable thread) {
                handler.removeCallbacks(thread);
            }

            @Override
            public void post(Runnable thread) {
                handler.post(thread);
            }

            @Override
            public void postDelayed(Runnable thread, long timeout) {
                handler.postDelayed(thread, timeout);
            }
        };

        DroidPlannerPrefs dpPrefs = new DroidPlannerPrefs(context);
        this.drone = new DroneImpl(mavClient, clock, dpHandler, dpPrefs);

        this.mavLinkMsgHandler = new MavLinkMsgHandler(this.drone);

        this.followMe = new Follow(this.drone, dpHandler, new FusedLocation(context));
    }

    @Override
    public void notifyConnected() {
        this.drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED);
    }

    @Override
    public void notifyDisconnected() {
        this.drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED);
    }

    @Override
    public void notifyReceivedData(MAVLinkMessage m) {
        this.mavLinkMsgHandler.receiveData(m);
    }

    public Drone getDrone(){
        return this.drone;
    }

    public Follow getFollowMe(){
        return followMe;
    }
}
