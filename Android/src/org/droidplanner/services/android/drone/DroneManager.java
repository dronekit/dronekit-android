package org.droidplanner.services.android.drone;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.services.android.api.MavLinkServiceApi;
import org.droidplanner.services.android.communication.service.MAVLinkClient;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.location.FusedLocation;
import org.droidplanner.services.android.utils.file.help.CameraInfoLoader;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by fhuya on 11/1/14.
 */
public class DroneManager implements MAVLinkStreams.MavlinkInputStream, DroneEventsListener {

    private static final String TAG = DroneManager.class.getSimpleName();

    private final ConcurrentLinkedQueue<DroneEventsListener> droneEventsListeners = new
            ConcurrentLinkedQueue<DroneEventsListener>();

    private final Drone drone;
    private final Follow followMe;
    private final CameraInfoLoader cameraInfoLoader;
    private final DroneInterfaces.Handler dpHandler;
    private final ConnectionParameter connectionParams;
    private final MavLinkMsgHandler mavLinkMsgHandler;

    public DroneManager(Context context, final Handler handler,
                        MavLinkServiceApi mavlinkApi, ConnectionParameter connParams)
            throws ConnectionException {

        this.cameraInfoLoader = new CameraInfoLoader(context);

        this.connectionParams = connParams;
        MAVLinkClient mavClient = new MAVLinkClient(this, mavlinkApi, connParams);

        DroneInterfaces.Clock clock = new DroneInterfaces.Clock() {
            @Override
            public long elapsedRealtime() {
                return SystemClock.elapsedRealtime();
            }
        };

        dpHandler = new DroneInterfaces.Handler() {
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

        this.followMe = new Follow(this.drone, dpHandler, new FusedLocation(context, handler));

        //Connect to the drone.
        drone.addDroneListener(this);
        drone.getParameters().setParameterListener(this);
        drone.getMavClient().openConnection();
    }

    public void destroy() {
        Log.d(TAG, "Destroying drone manager.");
        drone.removeDroneListener(this);
        drone.getParameters().setParameterListener(null);
        drone.getMavClient().closeConnection();
        droneEventsListeners.clear();
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
        if(droneEventsListeners.remove(listener)){
            listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
        }
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

    public ConnectionParameter getConnectionParameter(){
        return this.connectionParams;
    }

    public Drone getDrone(){
        return this.drone;
    }

    public Follow getFollowMe(){
        return followMe;
    }

    public DroneInterfaces.Handler getHandler(){
        return dpHandler;
    }

    public int getListenersCount(){
        return droneEventsListeners.size();
    }

    public boolean isConnected(){
        return drone.getMavClient().isConnected();
    }

    public CameraInfoLoader getCameraInfoLoader(){
        return cameraInfoLoader;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        Log.d(TAG, "Received event: " + event);
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
