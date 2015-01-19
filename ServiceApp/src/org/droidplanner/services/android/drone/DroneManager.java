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
import org.droidplanner.services.android.utils.AndroidApWarningParser;
import org.droidplanner.services.android.utils.analytics.GAUtils;
import org.droidplanner.services.android.utils.file.DirectoryPath;
import org.droidplanner.services.android.utils.file.IO.CameraInfoLoader;
import org.droidplanner.services.android.utils.prefs.DroidPlannerPrefs;

import java.io.File;
import java.util.List;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

/**
 * Created by fhuya on 11/1/14.
 */
public class DroneManager implements MAVLinkStreams.MavlinkInputStream,
        MagnetometerCalibration.OnMagCalibrationListener, DroneInterfaces.OnDroneListener,
        DroneInterfaces.OnParameterManagerListener {

    private static final String TAG = DroneManager.class.getSimpleName();

    private DroneEventsListener droneEventsListener;

    private final Context context;
    private final String appId;
    private final Drone drone;
    private final Follow followMe;
    private final CameraInfoLoader cameraInfoLoader;
    private final MavLinkMsgHandler mavLinkMsgHandler;
    private MagnetometerCalibration magCalibration;

    private DroneshareClient uploader;
    private ConnectionParameter connectionParams;

    public DroneManager(Context context, String appId, final Handler handler, MavLinkServiceApi mavlinkApi) {
        this.appId = appId;
        this.context = context;
        this.cameraInfoLoader = new CameraInfoLoader(context);

        MAVLinkClient mavClient = new MAVLinkClient(context, this, mavlinkApi, getTLogDir());

        DroneInterfaces.Clock clock = new DroneInterfaces.Clock() {
            @Override
            public long elapsedRealtime() {
                return SystemClock.elapsedRealtime();
            }
        };

        final DroneInterfaces.Handler dpHandler = new DroneInterfaces.Handler() {
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
        this.drone = new DroneImpl(mavClient, clock, dpHandler, dpPrefs, new AndroidApWarningParser(context));

        this.mavLinkMsgHandler = new MavLinkMsgHandler(this.drone);

        this.followMe = new Follow(this.drone, dpHandler, new FusedLocation(context, handler));

        this.magCalibration = new MagnetometerCalibration(this.drone, this, dpHandler);

        drone.addDroneListener(this);
        drone.getParameters().setParameterListener(this);
    }

    private File getTLogDir() {
        return DirectoryPath.getTLogPath(this.context, this.appId);
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

        droneEventsListener = null;

        if (magCalibration.isRunning())
            magCalibration.stop();

        if (followMe.isEnabled())
            followMe.toggleFollowMeState();
    }

    public void setDroneEventsListener(DroneEventsListener listener) {
        if (droneEventsListener != null && listener == null) {
            droneEventsListener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
        }

        droneEventsListener = listener;

        if (listener != null) {
            if (isConnected()) {
                listener.onDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED, drone);
            } else {
                listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
            }
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
        if (this.connectionParams != null) {
            final DroneSharePrefs droneSharePrefs = connectionParams.getDroneSharePrefs();

            // Start a new ga analytics session. The new session will be tagged
            // with the mavlink connection mechanism, as well as whether the user has an active droneshare account.
            GAUtils.startNewSession(droneSharePrefs);

            //TODO: restore live upload functionality when issue
            // 'https://github.com/diydrones/droneapi-java/issues/2' is fixed.
            boolean isLiveUploadEnabled = false; //droneSharePrefs.isLiveUploadEnabled();
            if (droneSharePrefs != null && isLiveUploadEnabled && droneSharePrefs.areLoginCredentialsSet()) {
                Log.i(TAG, "Starting live upload");
                try {
                    if (uploader == null)
                        uploader = new DroneshareClient();

                    uploader.connect(droneSharePrefs.getUsername(), droneSharePrefs.getPassword());
                } catch (Exception e) {
                    Log.e(TAG, "DroneShare uploader error.", e);
                }
            } else {
                Log.i(TAG, "Skipping live upload");
            }
        }

        this.drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.CONNECTING);
    }

    public void kickStartDroneShareUpload(){
        if (this.connectionParams != null) {
            // See if we can at least do a delayed upload
            UploaderService.kickStart(context, this.appId, this.connectionParams.getDroneSharePrefs());
        }
    }

    @Override
    public void notifyDisconnected() {
        kickStartDroneShareUpload();

        if (uploader != null) {
            try {
                uploader.close();
            } catch (Exception e) {
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
        MAVLinkMessage receivedMsg = packet.unpack();
        this.mavLinkMsgHandler.receiveData(receivedMsg);

        if (droneEventsListener != null) {
            droneEventsListener.onReceivedMavLinkMessage(receivedMsg);
        }

        if (uploader != null) {
            try {
                uploader.filterMavlink(uploader.interfaceNum, packet.encodePacket());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void onStreamError(String errorMsg) {
        if (droneEventsListener != null) {
            droneEventsListener.onConnectionFailed(errorMsg);
        }
    }

    public ConnectionParameter getConnectionParameter() {
        return this.connectionParams;
    }

    public void setConnectionParameter(ConnectionParameter connParams) {
        this.connectionParams = connParams;

        if (connParams != null) {
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

        ((MAVLinkClient) drone.getMavClient()).setConnectionParameter(connParams);
    }

    public Drone getDrone() {
        return this.drone;
    }

    public Follow getFollowMe() {
        return followMe;
    }

    public boolean isConnected() {
        return drone.isConnected();
    }

    public CameraInfoLoader getCameraInfoLoader() {
        return cameraInfoLoader;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        if (droneEventsListener != null) {
            droneEventsListener.onDroneEvent(event, drone);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        if (droneEventsListener != null) {
            droneEventsListener.onBeginReceivingParameters();
        }
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        if (droneEventsListener != null) {
            droneEventsListener.onParameterReceived(parameter, index, count);
        }
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameter) {
        if (droneEventsListener != null) {
            droneEventsListener.onEndReceivingParameters(parameter);
        }
    }

    @Override
    public void onStarted(List<ThreeSpacePoint> points) {
        if (droneEventsListener != null) {
            droneEventsListener.onStarted(points);
        }
    }

    @Override
    public void newEstimation(FitPoints fit, List<ThreeSpacePoint> points) {
        if (droneEventsListener != null) {
            droneEventsListener.newEstimation(fit, points);
        }
    }

    @Override
    public void finished(FitPoints fit, double[] offsets) {
        try {
            offsets = magCalibration.sendOffsets();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (droneEventsListener != null) {
            droneEventsListener.finished(fit, offsets);
        }
    }
}
