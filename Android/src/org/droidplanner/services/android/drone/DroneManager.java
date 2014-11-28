package org.droidplanner.services.android.drone;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.connection.StreamRates;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.StreamRates.Rates;
import org.droidplanner.core.drone.variables.helpers.MagnetometerCalibration;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.services.android.api.MavLinkServiceApi;
import org.droidplanner.services.android.communication.connection.DroneshareClient;
import org.droidplanner.services.android.communication.service.MAVLinkClient;
import org.droidplanner.services.android.communication.service.UploaderService;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.location.FusedLocation;
import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.services.android.utils.file.help.CameraInfoLoader;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

/**
 * Created by fhuya on 11/1/14.
 */
public class DroneManager implements MAVLinkStreams.MavlinkInputStream, DroneEventsListener {

    private static final String TAG = DroneManager.class.getSimpleName();

    private final ConcurrentLinkedQueue<DroneEventsListener> droneEventsListeners = new
            ConcurrentLinkedQueue<DroneEventsListener>();

    private final Context context;

    private final Drone drone;
    private final Follow followMe;
    private final CameraInfoLoader cameraInfoLoader;
    private final DroneInterfaces.Handler dpHandler;
    private final MavLinkMsgHandler mavLinkMsgHandler;
    private MagnetometerCalibration magCalibration;

    private DroneshareClient uploader;
    private ConnectionParameter connectionParams;

    public DroneManager(Context context, final Handler handler, MavLinkServiceApi mavlinkApi) {

        this.context = context;
        this.cameraInfoLoader = new CameraInfoLoader(context);

        MAVLinkClient mavClient = new MAVLinkClient(this, mavlinkApi);

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

        this.magCalibration = new MagnetometerCalibration(this.drone, this, this.dpHandler);

        drone.addDroneListener(this);
        drone.getParameters().setParameterListener(this);
    }

    public void destroy() {
        Log.d(TAG, "Destroying drone manager.");

        drone.removeDroneListener(this);
        drone.getParameters().setParameterListener(null);

        try {
            disconnect();
        } catch (ConnectionException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        droneEventsListeners.clear();

        if (magCalibration.isRunning())
            magCalibration.stop();

        if (followMe.isEnabled())
            followMe.toggleFollowMeState();
    }

    public void addDroneEventsListener(DroneEventsListener listener) {
        droneEventsListeners.add(listener);

        if (isConnected()) {
            listener.onDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED, drone);
        } else {
            listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
        }
    }

    public void removeDroneEventsListener(DroneEventsListener listener) {
        if (droneEventsListeners.remove(listener)) {
            listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
        }
    }

    public void connect() throws ConnectionException {
        MAVLinkClient mavClient = (MAVLinkClient) drone.getMavClient();
        if (!mavClient.isConnected()) {
            mavClient.openConnection();
        } else {
            onDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED, drone);
        }
    }

    public void disconnect() throws ConnectionException {
        MAVLinkClient mavClient = (MAVLinkClient) drone.getMavClient();
        if (mavClient.isConnected())
            mavClient.closeConnection();
        else
            onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
    }

    @Override
    public void notifyConnected() {
        if(this.connectionParams != null){
            final DroneSharePrefs droneSharePrefs = connectionParams.getDroneSharePrefs();

            // Start a new ga analytics session. The new session will be tagged
            // with the mavlink connection mechanism, as well as whether the user
            // has an active droneshare account.
            GAUtils.startNewSession(droneSharePrefs);

            if(droneSharePrefs != null && droneSharePrefs.isLiveUploadEnabled() &&
                    droneSharePrefs.areLoginCredentialsSet()){
                Log.i(TAG, "Starting live upload");
                if(uploader == null)
                    uploader = new DroneshareClient();

                uploader.connect(droneSharePrefs.getUsername(), droneSharePrefs.getPassword());
            }
            else{
                Log.i(TAG, "Skipping live upload");
            }
        }

        this.drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED);
    }

    @Override
    public void notifyDisconnected() {
        if (this.connectionParams != null) {
            // See if we can at least do a delayed upload
            UploaderService.kickStart(context, this.connectionParams.getDroneSharePrefs());
        }

        if (uploader != null) {
            try {
                uploader.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing the drone share upload handler.", e);
            }
        }

        this.drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED);
    }

    public void startMagnetometerCalibration(List<ThreeSpacePoint> startPoints) {
        if (magCalibration.isRunning()) {
            magCalibration.stop();
        }

        magCalibration.start(startPoints);
    }

    public void stopMagnetometerCalibration() {
        if (magCalibration.isRunning())
            magCalibration.stop();
    }

    @Override
    public void notifyReceivedData(MAVLinkPacket packet) {
        this.mavLinkMsgHandler.receiveData(packet.unpack());

        if (uploader != null)
            try {
                uploader.filterMavlink(uploader.interfaceNum, packet.encodePacket());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
    }

    @Override
    public void onStreamError(String errorMsg) {
        for (DroneEventsListener listener : droneEventsListeners)
            listener.onConnectionFailed(errorMsg);
    }

    public ConnectionParameter getConnectionParameter() {
        return this.connectionParams;
    }

    public void setConnectionParameter(ConnectionParameter connParams){
        this.connectionParams = connParams;

        if(connParams != null){
            StreamRates connRates = connParams.getStreamRates();
            Rates droneRates = new Rates();
            droneRates.extendedStatus = connRates.getExtendedStatus();
            droneRates.extra1 = connRates.getExtra1();
            droneRates.extra2 = connRates.getExtra2();
            droneRates.extra3 = connRates.getExtra3();
            droneRates.position = connRates.getPosition();
            droneRates.rcChannels = connRates.getRcChannels();
            droneRates.rawSensors = connRates.getRawSensors();
            droneRates.rawController = connRates.getRawController();

            drone.getStreamRates().setRates(droneRates);
        }

        ((MAVLinkClient)drone.getMavClient()).setConnectionParameter(connParams);
    }

    public Drone getDrone() {
        return this.drone;
    }

    public Follow getFollowMe() {
        return followMe;
    }

    public DroneInterfaces.Handler getHandler() {
        return dpHandler;
    }

    public int getListenersCount() {
        return droneEventsListeners.size();
    }

    public MagnetometerCalibration getMagCalibration() {
        return magCalibration;
    }

    public boolean isConnected() {
        return drone.getMavClient().isConnected();
    }

    public CameraInfoLoader getCameraInfoLoader() {
        return cameraInfoLoader;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        for (DroneEventsListener listener : droneEventsListeners)
            listener.onDroneEvent(event, drone);
    }

    @Override
    public void onBeginReceivingParameters() {
        for (DroneEventsListener listener : droneEventsListeners)
            listener.onBeginReceivingParameters();
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        for (DroneEventsListener listener : droneEventsListeners)
            listener.onParameterReceived(parameter, index, count);
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameter) {
        for (DroneEventsListener listener : droneEventsListeners)
            listener.onEndReceivingParameters(parameter);
    }

    @Override
    public void onStarted(List<ThreeSpacePoint> points) {
        for (DroneEventsListener listener : droneEventsListeners)
            listener.onStarted(points);
    }

    @Override
    public void newEstimation(FitPoints fit, List<ThreeSpacePoint> points) {
        for (DroneEventsListener listener : droneEventsListeners)
            listener.newEstimation(fit, points);
    }

    @Override
    public void finished(FitPoints fit, double[] offsets) {
        try {
            offsets = magCalibration.sendOffsets();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        for (DroneEventsListener listener : droneEventsListeners)
            listener.finished(fit, offsets);
    }

    @Override
    public void onConnectionFailed(String error) {

    }
}
