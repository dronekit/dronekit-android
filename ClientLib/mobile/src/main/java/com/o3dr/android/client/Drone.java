package com.o3dr.android.client;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fhuya on 11/4/14.
 */
public class Drone {

    private static final String CLAZZ_NAME = Drone.class.getName();
    private static final String TAG = Drone.class.getSimpleName();

    public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
    public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
    public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;

    public static final String ACTION_GROUND_COLLISION_IMMINENT = CLAZZ_NAME +
            ".ACTION_GROUND_COLLISION_IMMINENT";
    public static final String EXTRA_IS_GROUND_COLLISION_IMMINENT =
            "extra_is_ground_collision_imminent";

    private final ConcurrentLinkedQueue<DroneListener> droneListeners = new
            ConcurrentLinkedQueue<DroneListener>();

    private final Handler handler;
    private final ServiceManager serviceMgr;
    private final DroneObserver droneObserver;
    private final DroneApiListener apiListener;
    private IDroneApi droneApi;

    private ConnectionParameter connectionParameter;

    // flightTimer
    // ----------------
    private long startTime = 0;
    private long elapsedFlightTime = 0;
    private AtomicBoolean isTimerRunning = new AtomicBoolean(false);

    public Drone(ServiceManager serviceManager, Handler handler) {
        this.handler = handler;
        this.serviceMgr = serviceManager;
        this.apiListener = new DroneApiListener(this);
        this.droneObserver = new DroneObserver(this);
    }

    public void start() {
        if (!serviceMgr.isServiceConnected())
            throw new IllegalStateException("Service manager must be connected.");

        if (isStarted())
            return;

        try {
            this.droneApi = serviceMgr.get3drServices().registerDroneApi(this.apiListener,
                    serviceMgr.getApplicationId());
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to retrieve a valid drone handle.");
        }

        addAttributesObserver(this.droneObserver);
        resetFlightTimer();
    }

    public void destroy() {
        removeAttributesObserver(this.droneObserver);

        try {
            if (isStarted() && serviceMgr.isServiceConnected())
                serviceMgr.get3drServices().releaseDroneApi(this.droneApi);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        this.droneApi = null;
        droneListeners.clear();
    }

    private void checkForGroundCollision() {
        Speed speed = getSpeed();
        Altitude altitude = getAltitude();
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
        final String errorMsg = e.getMessage();
        Log.e(TAG, errorMsg, e);
        notifyDroneServiceInterrupted(errorMsg);
    }

    public double getSpeedParameter() {
        Parameters params = getParameters();
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
        isTimerRunning.set(true);
    }

    public void startTimer() {
        if (isTimerRunning.compareAndSet(false, true))
            startTime = SystemClock.elapsedRealtime();
    }

    public void stopTimer() {
        if (isTimerRunning.compareAndSet(true, false)) {
            // lets calc the final elapsed timer
            elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
            startTime = SystemClock.elapsedRealtime();
        }
    }

    public long getFlightTime() {
        State droneState = getState();
        if (droneState != null && droneState.isFlying()) {
            // calc delta time since last checked
            elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
            startTime = SystemClock.elapsedRealtime();
        }
        return elapsedFlightTime / 1000;
    }

    public Gps getGps() {
        Gps gps = getAttribute(AttributeType.GPS, Gps.class.getClassLoader());
        return gps == null ? new Gps() : gps;
    }

    public State getState() {
        State state = getAttribute(AttributeType.STATE, State.class.getClassLoader());
        return state == null ? new State() : state;
    }

    private Bundle getAttribute(String attributeType) {
        if (attributeType != null) {
            if (isStarted()) {
                try {
                    return droneApi.getAttribute(attributeType);
                } catch (RemoteException e) {
                    handleRemoteException(e);
                }
            }
        }

        return new Bundle();
    }

    private <T extends Parcelable> T getAttribute(String type, ClassLoader classLoader) {
        T attribute = null;
        if (isStarted()) {
            Bundle carrier = getAttribute(type);
            if (carrier != null) {
                carrier.setClassLoader(classLoader);
                attribute = carrier.getParcelable(type);
            }
        }

        return attribute;
    }

    public Parameters getParameters() {
        Parameters params = getAttribute(AttributeType.PARAMETERS, Parameters.class.getClassLoader
                ());
        return params == null ? new Parameters() : params;
    }

    public Speed getSpeed() {
        Speed speed = getAttribute(AttributeType.SPEED, Speed.class.getClassLoader());
        return speed == null ? new Speed() : speed;
    }

    public Attitude getAttitude() {
        Attitude attitude = getAttribute(AttributeType.ATTITUDE, Attitude.class.getClassLoader());
        return attitude == null ? new Attitude() : attitude;
    }

    public Home getHome() {
        Home home = getAttribute(AttributeType.HOME, Home.class.getClassLoader());
        return home == null ? new Home() : home;
    }

    public Battery getBattery() {
        Battery battery = getAttribute(AttributeType.BATTERY, Battery.class.getClassLoader());
        return battery == null ? new Battery() : battery;
    }

    public Altitude getAltitude() {
        Altitude altitude = getAttribute(AttributeType.ALTITUDE, Altitude.class.getClassLoader());
        return altitude == null ? new Altitude() : altitude;
    }

    public Mission getMission() {
        Mission mission = getAttribute(AttributeType.MISSION, Mission.class.getClassLoader());
        return mission == null ? new Mission() : mission;
    }

    public Signal getSignal() {
        Signal signal = getAttribute(AttributeType.SIGNAL, Signal.class.getClassLoader());
        return signal == null ? new Signal() : signal;
    }

    public Type getType() {
        Type type = getAttribute(AttributeType.TYPE, Type.class.getClassLoader());
        return type == null ? new Type() : type;
    }

    public void connect(final ConnectionParameter connParams) {
        if (isStarted()) {
            try {
                droneApi.connect(connParams);
                this.connectionParameter = connParams;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void disconnect() {
        if (isStarted()) {
            try {
                droneApi.disconnect();
                this.connectionParameter = null;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public boolean isStarted() {
        return droneApi != null;
    }

    public boolean isConnected() {
        return isStarted() && getState().isConnected();
    }

    public GuidedState getGuidedState() {
        GuidedState guidedState = getAttribute(AttributeType.GUIDED_STATE,
                GuidedState.class.getClassLoader());
        return guidedState == null ? new GuidedState() : guidedState;
    }

    public FollowState getFollowState() {
        FollowState followState = getAttribute(AttributeType.FOLLOW_STATE,
                FollowState.class.getClassLoader());
        return followState == null ? new FollowState() : followState;
    }

    public CameraProxy getCamera() {
        return getAttribute(AttributeType.CAMERA, CameraProxy.class.getClassLoader());
    }

    public ConnectionParameter getConnectionParameter() {
        return this.connectionParameter;
    }

    public <T extends MissionItem> void buildComplexMissionItem(MissionItem.ComplexItem<T>
                                                                        complexItem) {
        if (isStarted()) {
            try {
                T missionItem = (T) complexItem;
                Bundle payload = missionItem.getType().storeMissionItem(missionItem);
                if (payload == null)
                    return;

                droneApi.buildComplexMissionItem(payload);
                T updatedItem = MissionItemType.restoreMissionItemFromBundle(payload);
                complexItem.copy(updatedItem);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void registerDroneListener(DroneListener listener) {
        if (listener == null)
            return;

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
        if (isStarted()) {
            try {
                droneApi.changeVehicleMode(newMode);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void refreshParameters() {
        if (isStarted()) {
            try {
                droneApi.refreshParameters();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void writeParameters(Parameters parameters) {
        if (isStarted()) {
            try {
                droneApi.writeParameters(parameters);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void setMission(Mission mission, boolean pushToDrone) {
        if (isStarted()) {
            try {
                droneApi.setMission(mission, pushToDrone);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void generateDronie() {
        if (isStarted()) {
            try {
                droneApi.generateDronie();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void arm(boolean arm) {
        if (isStarted()) {
            try {
                droneApi.arm(arm);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void startMagnetometerCalibration(double[] startPointsX, double[] startPointsY,
                                             double[] startPointsZ) {
        if (isStarted()) {
            try {
                droneApi.startMagnetometerCalibration(startPointsX, startPointsY, startPointsZ);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void stopMagnetometerCalibration() {
        if (isStarted()) {
            try {
                droneApi.stopMagnetometerCalibration();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void startIMUCalibration() {
        if (isStarted()) {
            try {
                droneApi.startIMUCalibration();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void sendIMUCalibrationAck(int step) {
        if (isStarted()) {
            try {
                droneApi.sendIMUCalibrationAck(step);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void doGuidedTakeoff(double altitude) {
        if (isStarted()) {
            try {
                droneApi.doGuidedTakeoff(altitude);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void pauseAtCurrentLocation() {
        sendGuidedPoint(getGps().getPosition(), true);
    }

    public void sendGuidedPoint(LatLong point, boolean force) {
        if (isStarted()) {
            try {
                droneApi.sendGuidedPoint(point, force);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void sendMavlinkMessage(MavlinkMessageWrapper messageWrapper) {
        if (messageWrapper != null && isStarted()) {
            try {
                droneApi.sendMavlinkMessage(messageWrapper);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void setGuidedAltitude(double altitude) {
        if (isStarted()) {
            try {
                droneApi.setGuidedAltitude(altitude);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void setGuidedVelocity(double xVel, double yVel, double zVel) {
        if (isStarted()) {
            try {
                droneApi.setGuidedVelocity(xVel, yVel, zVel);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void enableFollowMe(FollowType followType) {
        if (isStarted()) {
            try {
                droneApi.enableFollowMe(followType);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void setFollowMeRadius(double radius) {
        if (isStarted()) {
            try {
                droneApi.setFollowMeRadius(radius);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void disableFollowMe() {
        if (isStarted()) {
            try {
                droneApi.disableFollowMe();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }


    public void triggerCamera() {
        if (isStarted()) {
            try {
                droneApi.triggerCamera();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void epmCommand(boolean release) {
        if (isStarted()) {
            try {
                droneApi.epmCommand(release);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void loadWaypoints() {
        if (isStarted()) {
            try {
                droneApi.loadWaypoints();
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
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
        if (AttributeEvent.STATE_UPDATED.equals(attributeEvent)) {
            if (getState().isFlying())
                startTimer();
            else
                stopTimer();
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
