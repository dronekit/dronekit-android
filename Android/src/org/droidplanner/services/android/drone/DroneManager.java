package org.droidplanner.services.android.drone;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.MAVLink.Messages.MAVLinkMessage;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.services.android.communication.service.MAVLinkClient;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.location.FusedLocation;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by fhuya on 11/1/14.
 */
public class DroneManager implements MAVLinkStreams.MavlinkInputStream, DroneEventsListener {

    private static final String CLAZZ_NAME = DroneManager.class.getName();
    public static final String ACTION_DRONE_CREATED = CLAZZ_NAME + ".ACTION_DRONE_CREATED";
    public static final String ACTION_DRONE_DESTROYED = CLAZZ_NAME + ".ACTION_DRONE_DESTROYED";

    private final ConcurrentLinkedQueue<DroneEventsListener> droneEventsListeners = new
            ConcurrentLinkedQueue<DroneEventsListener>();

    private final Handler handler = new Handler();
    private final LocalBroadcastManager lbm;

    private final Drone drone;
    private final Follow followMe;
    private final MavLinkMsgHandler mavLinkMsgHandler;

    public DroneManager(Context context, ConnectionParameter connParams) throws ConnectionException {
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

        lbm = LocalBroadcastManager.getInstance(context);
        lbm.sendBroadcast(new Intent(ACTION_DRONE_CREATED));

        //Connect to the drone.
        drone.addDroneListener(this);
        drone.getParameters().setParameterListener(this);
        drone.getMavClient().toggleConnectionState();
    }

    public void destroy() {
        drone.removeDroneListener(this);
        drone.getParameters().setParameterListener(null);
        drone.getMavClient().toggleConnectionState();
        droneEventsListeners.clear();

        this.lbm.sendBroadcast(new Intent(ACTION_DRONE_DESTROYED));
    }

    public void addDroneEventsListener(DroneEventsListener listener){
        droneEventsListeners.add(listener);

        if(isConnected()){
            listener.onDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED, drone);
        }
        else{
            listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
        }
    }

    public void removeDroneEventsListener(DroneEventsListener listener){
        droneEventsListeners.remove(listener);
        listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
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

    public boolean isConnected(){
        return drone.getMavClient().isConnected();
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        for(DroneEventsListener listener: droneEventsListeners)
            listener.onDroneEvent(event, drone);
    }

    @Override
    public void onBeginReceivingParameters() {
        for(DroneEventsListener listener:droneEventsListeners)
            listener.onBeginReceivingParameters();
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        for(DroneEventsListener listener:droneEventsListeners)
            listener.onParameterReceived(parameter, index, count);
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameter) {
        for(DroneEventsListener listener:droneEventsListeners)
            listener.onEndReceivingParameters(parameter);
    }

}
