package com.o3dr.android.client;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.o3dr.android.client.apis.CalibrationApi;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.ExperimentalApi;
import com.o3dr.android.client.apis.FollowApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationStatus;
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
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
import com.o3dr.services.android.lib.gcs.returnToMe.ReturnToMeState;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.IDroneApi;
import com.o3dr.services.android.lib.model.IObserver;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

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

    private final AtomicReference<IDroneApi> droneApiRef = new AtomicReference<>(null);
    private ConnectionParameter connectionParameter;
    private ExecutorService asyncScheduler;

    // flightTimer
    // ----------------
    private long startTime = 0;
    private long elapsedFlightTime = 0;

    private final Context context;
    private final ClassLoader contextClassLoader;

    /**
     * Creates a Drone instance.
     *
     * @param context Application context
     */
    public Drone(Context context) {
        this.context = context;
        this.contextClassLoader = context.getClassLoader();
    }

    void init(ControlTower controlTower, Handler handler) {
        this.handler = handler;
        this.serviceMgr = controlTower;
        this.apiListener = new DroneApiListener(this);
        this.droneObserver = new DroneObserver(this);
    }

    Context getContext(){
        return this.context;
    }

    synchronized void start() {
        if (!serviceMgr.isTowerConnected())
            throw new IllegalStateException("Service manager must be connected.");

        IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi))
            return;

        try {
            droneApi = serviceMgr.get3drServices().registerDroneApi(this.apiListener, serviceMgr.getApplicationId());
            droneApi.asBinder().linkToDeath(binderDeathRecipient, 0);
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to retrieve a valid drone handle.");
        }

        if (asyncScheduler == null || asyncScheduler.isShutdown())
            asyncScheduler = Executors.newFixedThreadPool(1);

        addAttributesObserver(droneApi, this.droneObserver);
        resetFlightTimer();

        droneApiRef.set(droneApi);
    }

    synchronized void destroy() {
        IDroneApi droneApi = droneApiRef.get();

        removeAttributesObserver(droneApi, this.droneObserver);

        try {
            if (isStarted(droneApi)) {
                droneApi.asBinder().unlinkToDeath(binderDeathRecipient, 0);
                serviceMgr.get3drServices().releaseDroneApi(droneApi);
            }
        } catch (RemoteException | NoSuchElementException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (asyncScheduler != null) {
            asyncScheduler.shutdownNow();
            asyncScheduler = null;
        }

        droneApiRef.set(null);
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
        final IDroneApi droneApi = droneApiRef.get();
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

    /**
     * Causes the Runnable to be added to the message queue.
     *
     * @param action Runnabl that will be executed.
     */
    public void post(Runnable action) {
        if (handler == null || action == null)
            return;

        handler.post(action);
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
        final IDroneApi droneApi = droneApiRef.get();
        if (!isStarted(droneApi) || type == null)
            return this.getAttributeDefaultValue(type);

        T attribute = null;
        Bundle carrier = null;
        try {
            carrier = droneApi.getAttribute(type);
        } catch (RemoteException e) {
            handleRemoteException(e);
        }

        if (carrier != null) {
            try {
                carrier.setClassLoader(contextClassLoader);
                attribute = carrier.getParcelable(type);
            }catch(Exception e){
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return attribute == null ? this.<T>getAttributeDefaultValue(type) : attribute;
    }

    public <T extends Parcelable> void getAttributeAsync(final String attributeType,
                                                         final OnAttributeRetrievedCallback<T> callback) {
        if (callback == null)
            throw new IllegalArgumentException("Callback must be non-null.");

        final IDroneApi droneApi = droneApiRef.get();
        if (!isStarted(droneApi)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onRetrievalFailed();
                }
            });
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
        if (attributeType == null)
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

            case AttributeType.MAGNETOMETER_CALIBRATION_STATUS:
                return (T) new MagnetometerCalibrationStatus();

            case AttributeType.RETURN_TO_ME_STATE:
                return (T) new ReturnToMeState();

            case AttributeType.CAMERA:
            case SoloAttributes.SOLO_STATE:
            case SoloAttributes.SOLO_GOPRO_STATE:
            default:
                return null;
        }
    }

    public void connect(final ConnectionParameter connParams) {
        VehicleApi.getApi(this).connect(connParams);
        this.connectionParameter = connParams;
    }

    public void disconnect() {
        VehicleApi.getApi(this).disconnect();
        this.connectionParameter = null;
    }

    private static AbstractCommandListener wrapListener(final Handler handler, final AbstractCommandListener listener) {
        AbstractCommandListener wrapperListener = listener;
        if (handler != null && listener != null) {
            wrapperListener = new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess();
                        }
                    });
                }

                @Override
                public void onError(final int executionError) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(executionError);
                        }
                    });
                }

                @Override
                public void onTimeout() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onTimeout();
                        }
                    });
                }
            };
        }

        return wrapperListener;
    }

    public boolean performAction(Action action) {
        return performActionOnDroneThread(action, null);
    }

    public boolean performActionOnDroneThread(Action action, AbstractCommandListener listener) {
        return performActionOnHandler(action, this.handler, listener);
    }

    public boolean performActionOnHandler(Action action, final Handler handler, final AbstractCommandListener listener) {
        final IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
            try {
                droneApi.executeAction(action, wrapListener(handler, listener));
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return false;
    }

    public boolean performAsyncAction(Action action) {
        return performAsyncActionOnDroneThread(action, null);
    }

    public boolean performAsyncActionOnDroneThread(Action action, AbstractCommandListener listener) {
        return performAsyncActionOnHandler(action, this.handler, listener);
    }

    public boolean performAsyncActionOnHandler(Action action, Handler handler, AbstractCommandListener listener) {
        final IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
            try {
                droneApi.executeAsyncAction(action, wrapListener(handler, listener));
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return false;
    }

    private boolean isStarted(IDroneApi droneApi) {
        return droneApi != null && droneApi.asBinder().pingBinder();
    }

    public boolean isStarted(){
        return isStarted(droneApiRef.get());
    }

    public boolean isConnected() {
        final IDroneApi droneApi = droneApiRef.get();
        State droneState = getAttribute(AttributeType.STATE);
        return isStarted(droneApi) && droneState.isConnected();
    }

    public ConnectionParameter getConnectionParameter() {
        return this.connectionParameter;
    }

    public <T extends MissionItem> void buildMissionItemsAsync(final MissionItem.ComplexItem<T>[] missionItems,
                                                               final OnMissionItemsBuiltCallback<T> callback) {
        if (callback == null)
            throw new IllegalArgumentException("Callback must be non-null.");

        if (missionItems == null || missionItems.length == 0)
            return;

        asyncScheduler.execute(new Runnable() {
            @Override
            public void run() {
                for (MissionItem.ComplexItem<T> missionItem : missionItems)
                    MissionApi.getApi(Drone.this).buildMissionItem(missionItem);

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

    private void addAttributesObserver(IDroneApi droneApi, IObserver observer) {
        if (isStarted(droneApi)) {
            try {
                droneApi.addAttributesObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void addMavlinkObserver(MavlinkObserver observer) {
        final IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
            try {
                droneApi.addMavlinkObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void removeMavlinkObserver(MavlinkObserver observer) {
        final IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
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

    private void removeAttributesObserver(IDroneApi droneApi, IObserver observer) {
        if (isStarted(droneApi)) {
            try {
                droneApi.removeAttributesObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    /**
     * @deprecated Use {@link VehicleApi#setVehicleMode(VehicleMode)} instead.
     */
    public void changeVehicleMode(VehicleMode newMode) {
        VehicleApi.getApi(this).setVehicleMode(newMode);
    }

    /**
     * @deprecated Use {@link VehicleApi#refreshParameters()} instead.
     */
    public void refreshParameters() {
        VehicleApi.getApi(this).refreshParameters();
    }

    /**
     * @deprecated Use {@link VehicleApi#writeParameters(Parameters)} instead.
     */
    public void writeParameters(Parameters parameters) {
        VehicleApi.getApi(this).writeParameters(parameters);
    }

    /**
     * @deprecated Use {@link MissionApi#setMission(Mission, boolean)} instead.
     */
    public void setMission(Mission mission, boolean pushToDrone) {
        MissionApi.getApi(this).setMission(mission, pushToDrone);
    }

    /**
     * @deprecated Use {@link MissionApi#generateDronie()} instead.
     */
    public void generateDronie() {
        MissionApi.getApi(this).generateDronie();
    }

    /**
     * @deprecated Use {@link VehicleApi#arm(boolean)} instead.
     */
    public void arm(boolean arm) {
        VehicleApi.getApi(this).arm(arm);
    }

    /**
     * @deprecated Use {@link CalibrationApi#startIMUCalibration()} instead.
     */
    public void startIMUCalibration() {
        CalibrationApi.getApi(this).startIMUCalibration();
    }

    /**
     * @deprecated Use {@link CalibrationApi#sendIMUAck(int)} instead.
     */
    public void sendIMUCalibrationAck(int step) {
        CalibrationApi.getApi(this).sendIMUAck(step);
    }

    /**
     * @deprecated Use {@link ControlApi#takeoff(double, AbstractCommandListener)} instead.
     */
    public void doGuidedTakeoff(double altitude) {
        ControlApi.getApi(this).takeoff(altitude, null);
    }

    /**
     * @deprecated Use {@link ControlApi#pauseAtCurrentLocation(AbstractCommandListener)} instead.
     */
    public void pauseAtCurrentLocation() {
        ControlApi.getApi(this).pauseAtCurrentLocation(null);
    }

    /**
     * @deprecated Use {@link ControlApi#goTo(LatLong, boolean, AbstractCommandListener)} instead.
     */
    public void sendGuidedPoint(LatLong point, boolean force) {
        ControlApi.getApi(this).goTo(point, force, null);
    }

    /**
     * @deprecated Use {@link ExperimentalApi#sendMavlinkMessage(MavlinkMessageWrapper)} instead.
     */
    public void sendMavlinkMessage(MavlinkMessageWrapper messageWrapper) {
        ExperimentalApi.getApi(this).sendMavlinkMessage(messageWrapper);
    }

    /**
     * @deprecated Use {@link ControlApi#climbTo(double)} instead.
     */
    public void setGuidedAltitude(double altitude) {
        ControlApi.getApi(this).climbTo(altitude);
    }

    /**
     * @deprecated Use {@link FollowApi#enableFollowMe(FollowType)} instead.
     */
    public void enableFollowMe(FollowType followType) {
        FollowApi.getApi(this).enableFollowMe(followType);
    }

    /**
     * @deprecated Use {@link FollowApi#disableFollowMe()} instead.
     */
    public void disableFollowMe() {
        FollowApi.getApi(this).disableFollowMe();
    }

    /**
     * @deprecated Use {@link ExperimentalApi#triggerCamera()} instead.
     */
    public void triggerCamera() {
        ExperimentalApi.getApi(this).triggerCamera();
    }

    /**
     * @deprecated Use {@link MissionApi#loadWaypoints()} instead.
     */
    public void loadWaypoints() {
        MissionApi.getApi(this).loadWaypoints();
    }

    public Handler getHandler(){
        return handler;
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
        if (extras != null)
            extras.setClassLoader(contextClassLoader);

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
                for (DroneListener listener : droneListeners) {
                    try {
                        listener.onDroneEvent(attributeEvent, extras);
                    }catch(Exception e){
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
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
