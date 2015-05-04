package com.o3dr.android.client;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.o3dr.android.client.apis.drone.ConnectApi;
import com.o3dr.android.client.apis.drone.DroneStateApi;
import com.o3dr.android.client.apis.drone.ExperimentalApi;
import com.o3dr.android.client.apis.drone.GuidedApi;
import com.o3dr.android.client.apis.drone.ParameterApi;
import com.o3dr.android.client.apis.gcs.CalibrationApi;
import com.o3dr.android.client.apis.gcs.FollowApi;
import com.o3dr.android.client.apis.mission.MissionApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationStatus;
import com.o3dr.services.android.lib.drone.camera.GoPro;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.CameraProxy;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.IDroneApi;
import com.o3dr.services.android.lib.model.IObserver;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fhuya on 11/4/14.
 */
public class Drone {

    private static final String CLAZZ_NAME = Drone.class.getName();
    private static final String TAG = Drone.class.getSimpleName();

    public interface OnAttributeRetrievedCallback<T extends Parcelable> {
        void onRetrievalSucceed(T attribute);

        void onRetrievalFailed();
    }

    public static class AttributeRetrievedListener<T extends Parcelable> implements OnAttributeRetrievedCallback<T> {

        @Override
        public void onRetrievalSucceed(T attribute) {
        }

        @Override
        public void onRetrievalFailed() {
        }
    }

    public interface OnMissionItemsBuiltCallback<T extends MissionItem> {
        void onMissionItemsBuilt(MissionItem.ComplexItem<T>[] complexItems);
    }

    public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
    public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
    public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;

    public static final String ACTION_GROUND_COLLISION_IMMINENT = CLAZZ_NAME + ".ACTION_GROUND_COLLISION_IMMINENT";
    public static final String EXTRA_IS_GROUND_COLLISION_IMMINENT = "extra_is_ground_collision_imminent";

    private final IBinder.DeathRecipient binderDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            notifyDroneServiceInterrupted("Lost access to the drone api.");
        }
    };

    private final ConcurrentLinkedQueue<DroneListener> droneListeners = new ConcurrentLinkedQueue<>();

    private Handler handler;
    private ControlTower serviceMgr;
    private DroneObserver droneObserver;
    private DroneApiListener apiListener;

    private IDroneApi droneApi;
    private ConnectionParameter connectionParameter;
    private ExecutorService asyncScheduler;

    // flightTimer
    // ----------------
    private long startTime = 0;
    private long elapsedFlightTime = 0;

    private final Context context;

    public Drone(Context context){
        this.context = context;
    }

    void init(ControlTower controlTower, Handler handler){
        this.handler = handler;
        this.serviceMgr = controlTower;
        this.apiListener = new DroneApiListener(this);
        this.droneObserver = new DroneObserver(this);
    }

    void start() {
        if (!serviceMgr.isTowerConnected())
            throw new IllegalStateException("Service manager must be connected.");

        if (isStarted())
            return;

        try {
            this.droneApi = serviceMgr.get3drServices().registerDroneApi(this.apiListener, serviceMgr.getApplicationId());
            this.droneApi.asBinder().linkToDeath(binderDeathRecipient, 0);
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to retrieve a valid drone handle.");
        }

        if (asyncScheduler == null || asyncScheduler.isShutdown())
            asyncScheduler = Executors.newFixedThreadPool(1);

        addAttributesObserver(this.droneObserver);
        resetFlightTimer();
    }

    void destroy() {
        removeAttributesObserver(this.droneObserver);

        try {
            if (isStarted()) {
                this.droneApi.asBinder().unlinkToDeath(binderDeathRecipient, 0);
                serviceMgr.get3drServices().releaseDroneApi(this.droneApi);
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (asyncScheduler != null) {
            asyncScheduler.shutdownNow();
            asyncScheduler = null;
        }

        this.droneApi = null;
        droneListeners.clear();
    }

    private void checkForGroundCollision() {
        Speed speed = getAttribute(AttributeType.SPEED);
        Altitude altitude = getAttribute(AttributeType.ALTITUDE);
        if (speed == null || altitude == null)
            return;

        double verticalSpeed = speed.getVerticalSpeed();
        double altitudeValue = altitude.getAltitude();

        boolean isCollisionImminent = altitudeValue
                + (verticalSpeed * COLLISION_SECONDS_BEFORE_COLLISION) < 0
                && verticalSpeed < COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND
                && altitudeValue > COLLISION_SAFE_ALTITUDE_METERS;

        Bundle extrasBundle = new Bundle(1);
        extrasBundle.putBoolean(EXTRA_IS_GROUND_COLLISION_IMMINENT, isCollisionImminent);
        notifyAttributeUpdated(ACTION_GROUND_COLLISION_IMMINENT, extrasBundle);
    }

    private void handleRemoteException(RemoteException e) {
        if (droneApi != null && !droneApi.asBinder().pingBinder()) {
            final String errorMsg = e.getMessage();
            Log.e(TAG, errorMsg, e);
            notifyDroneServiceInterrupted(errorMsg);
        }
    }

    public double getSpeedParameter() {
        Parameters params = getAttribute(AttributeType.PARAMETERS);
        if (params != null) {
            Parameter speedParam = params.getParameter("WPNAV_SPEED");
            if (speedParam != null)
                return speedParam.getValue();
        }

        return 0;
    }

    public void resetFlightTimer() {
        elapsedFlightTime = 0;
        startTime = SystemClock.elapsedRealtime();
    }

    public void stopTimer() {
        // lets calc the final elapsed timer
        elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
        startTime = SystemClock.elapsedRealtime();
    }

    public long getFlightTime() {
        State droneState = getAttribute(AttributeType.STATE);
        if (droneState != null && droneState.isFlying()) {
            // calc delta time since last checked
            elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
            startTime = SystemClock.elapsedRealtime();
        }
        return elapsedFlightTime / 1000;
    }

    public <T extends Parcelable> T getAttribute(String type) {
        if (!isStarted() || type == null)
            return this.getAttributeDefaultValue(type);

        T attribute = null;
        Bundle carrier = null;
        try {
            carrier = droneApi.getAttribute(type);
        } catch (RemoteException e) {
            handleRemoteException(e);
        }

        if (carrier != null) {
            ClassLoader classLoader = this.context.getClassLoader();
            if (classLoader != null) {
                carrier.setClassLoader(classLoader);
                attribute = carrier.getParcelable(type);
            }
        }

        return attribute == null ? this.<T>getAttributeDefaultValue(type) : attribute;
    }

    public <T extends Parcelable> void getAttributeAsync(final String attributeType,
                                                         final OnAttributeRetrievedCallback<T> callback) {
        if (callback == null)
            throw new IllegalArgumentException("Callback must be non-null.");

        if (!isStarted()) {
            callback.onRetrievalFailed();
            return;
        }

        asyncScheduler.execute(new Runnable() {
            @Override
            public void run() {
                final T attribute = getAttribute(attributeType);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (attribute == null)
                            callback.onRetrievalFailed();
                        else
                            callback.onRetrievalSucceed(attribute);
                    }
                });
            }
        });
    }

    private <T extends Parcelable> T getAttributeDefaultValue(String attributeType) {
        if(attributeType == null)
            return null;

        switch (attributeType) {
            case AttributeType.ALTITUDE:
                return (T) new Altitude();

            case AttributeType.GPS:
                return (T) new Gps();

            case AttributeType.STATE:
                return (T) new State();

            case AttributeType.PARAMETERS:
                return (T) new Parameters();

            case AttributeType.SPEED:
                return (T) new Speed();

            case AttributeType.ATTITUDE:
                return (T) new Attitude();

            case AttributeType.HOME:
                return (T) new Home();

            case AttributeType.BATTERY:
                return (T) new Battery();

            case AttributeType.MISSION:
                return (T) new Mission();

            case AttributeType.SIGNAL:
                return (T) new Signal();

            case AttributeType.GUIDED_STATE:
                return (T) new GuidedState();

            case AttributeType.TYPE:
                return (T) new Type();

            case AttributeType.FOLLOW_STATE:
                return (T) new FollowState();

            case AttributeType.GOPRO:
                return (T) new GoPro();

            case AttributeType.MAGNETOMETER_CALIBRATION_STATUS:
                return (T) new MagnetometerCalibrationStatus();

            case AttributeType.CAMERA:
            default:
                return null;
        }
    }

    public void connect(final ConnectionParameter connParams) {
        if (ConnectApi.connect(this, connParams))
            this.connectionParameter = connParams;
    }

    public void disconnect() {
        if (ConnectApi.disconnect(this))
            this.connectionParameter = null;
    }

    public boolean performAction(Action action) {
        if (isStarted()) {
            try {
                droneApi.performAction(action);
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return false;
    }

    public boolean performAsyncAction(Action action) {
        if (isStarted()) {
            try {
                droneApi.performAsyncAction(action);
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return false;
    }

    public boolean isStarted() {
        return droneApi != null && droneApi.asBinder().pingBinder();
    }

    public boolean isConnected() {
        State droneState = getAttribute(AttributeType.STATE);
        return isStarted() && droneState.isConnected();
    }

    public ConnectionParameter getConnectionParameter() {
        return this.connectionParameter;
    }

    public <T extends MissionItem> void buildMissionItemsAsync(final OnMissionItemsBuiltCallback<T> callback,
                                                               final MissionItem.ComplexItem<T>... missionItems) {
        if (callback == null)
            throw new IllegalArgumentException("Callback must be non-null.");

        if (missionItems == null || missionItems.length == 0)
            return;

        asyncScheduler.execute(new Runnable() {
            @Override
            public void run() {
                for (MissionItem.ComplexItem<T> missionItem : missionItems)
                    MissionApi.buildMissionItem(Drone.this, missionItem);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onMissionItemsBuilt(missionItems);
                    }
                });
            }
        });
    }

    public void registerDroneListener(DroneListener listener) {
        if (listener == null)
            return;

        if (!droneListeners.contains(listener))
            droneListeners.add(listener);
    }

    private void addAttributesObserver(IObserver observer) {
        if (isStarted()) {
            try {
                this.droneApi.addAttributesObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void addMavlinkObserver(MavlinkObserver observer) {
        if (isStarted()) {
            try {
                droneApi.addMavlinkObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void removeMavlinkObserver(MavlinkObserver observer) {
        if (isStarted()) {
            try {
                droneApi.removeMavlinkObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void unregisterDroneListener(DroneListener listener) {
        if (listener == null)
            return;

        droneListeners.remove(listener);
    }

    private void removeAttributesObserver(IObserver observer) {
        if (isStarted()) {
            try {
                this.droneApi.removeAttributesObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void changeVehicleMode(VehicleMode newMode) {
        DroneStateApi.setVehicleMode(this, newMode);
    }

    public void refreshParameters() {
        ParameterApi.refreshParameters(this);
    }

    public void writeParameters(Parameters parameters) {
        ParameterApi.writeParameters(this, parameters);
    }

    public void setMission(Mission mission, boolean pushToDrone) {
        MissionApi.setMission(this, mission, pushToDrone);
    }

    public void generateDronie() {
        MissionApi.generateDronie(this);
    }

    public void arm(boolean arm) {
        DroneStateApi.arm(this, arm);
    }

    /**
     * @deprecated Use {@link CalibrationApi#startIMUCalibration(Drone)} instead.
     */
    public void startIMUCalibration() {
        CalibrationApi.startIMUCalibration(this);
    }

    /**
     * @deprecated Use {@link CalibrationApi#sendIMUAck(Drone, int)} instead.
     */
    public void sendIMUCalibrationAck(int step) {
        CalibrationApi.sendIMUAck(this, step);
    }

    public void doGuidedTakeoff(double altitude) {
        GuidedApi.takeoff(this, altitude);
    }

    public void pauseAtCurrentLocation() {
        GuidedApi.pauseAtCurrentLocation(this);
    }

    public void sendGuidedPoint(LatLong point, boolean force) {
        GuidedApi.sendGuidedPoint(this, point, force);
    }

    public void sendMavlinkMessage(MavlinkMessageWrapper messageWrapper) {
        ExperimentalApi.sendMavlinkMessage(this, messageWrapper);
    }

    public void setGuidedAltitude(double altitude) {
        GuidedApi.setGuidedAltitude(this, altitude);
    }

    public void enableFollowMe(FollowType followType) {
        FollowApi.enableFollowMe(this, followType);
    }

    public void disableFollowMe() {
        FollowApi.disableFollowMe(this);
    }

    public void triggerCamera() {
        ExperimentalApi.triggerCamera(this);
    }

    public void epmCommand(boolean release) {
        ExperimentalApi.epmCommand(this, release);
    }

    public void loadWaypoints() {
        MissionApi.loadWaypoints(this);
    }

    void notifyDroneConnectionFailed(final ConnectionResult result) {
        if (droneListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DroneListener listener : droneListeners)
                    listener.onDroneConnectionFailed(result);
            }
        });
    }

    void notifyAttributeUpdated(final String attributeEvent, final Bundle extras) {
        //Update the bundle classloader
        if(extras != null)
            extras.setClassLoader(context.getClassLoader());

        if (AttributeEvent.STATE_UPDATED.equals(attributeEvent)) {
            getAttributeAsync(AttributeType.STATE, new OnAttributeRetrievedCallback<State>() {
                @Override
                public void onRetrievalSucceed(State state) {
                    if (state.isFlying())
                        resetFlightTimer();
                    else
                        stopTimer();
                }

                @Override
                public void onRetrievalFailed() {
                    stopTimer();
                }
            });
        } else if (AttributeEvent.SPEED_UPDATED.equals(attributeEvent)) {
            checkForGroundCollision();
        }

        if (droneListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DroneListener listener : droneListeners)
                    listener.onDroneEvent(attributeEvent, extras);
            }
        });
    }

    void notifyDroneServiceInterrupted(final String errorMsg) {
        if (droneListeners.isEmpty())
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DroneListener listener : droneListeners)
                    listener.onDroneServiceInterrupted(errorMsg);
            }
        });
    }
}
