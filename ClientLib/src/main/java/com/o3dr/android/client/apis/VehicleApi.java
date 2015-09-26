package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_CONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_DISCONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.EXTRA_CONNECT_PARAMETER;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.ACTION_REFRESH_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.ACTION_WRITE_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.EXTRA_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_ARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_ENABLE_RETURN_TO_ME;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_SET_VEHICLE_HOME;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_SET_VEHICLE_MODE;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_ARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_EMERGENCY_DISARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_IS_RETURN_TO_ME_ENABLED;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_VEHICLE_HOME_LOCATION;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_VEHICLE_MODE;

/**
 * Provides access to the vehicle specific functionality.
 */
public class VehicleApi extends Api {

    private static final ConcurrentHashMap<Drone, VehicleApi> vehicleApiCache = new ConcurrentHashMap<>();
    private static final Builder<VehicleApi> apiBuilder = new Builder<VehicleApi>() {
        @Override
        public VehicleApi build(Drone drone) {
            return new VehicleApi(drone);
        }
    };

    /**
     * Retrieves a vehicle api instance.
     *
     * @param drone target vehicle
     * @return a VehicleApi instance.
     */
    public static VehicleApi getApi(final Drone drone) {
        return getApi(drone, vehicleApiCache, apiBuilder);
    }

    private final Drone drone;
    private final ControlApi controlApi;

    private VehicleApi(Drone drone) {
        this.drone = drone;
        this.controlApi = ControlApi.getApi(drone);
    }

    /**
     * Establish connection with the vehicle.
     *
     * @param parameter parameter for the connection.
     */
    public void connect(ConnectionParameter parameter) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_CONNECT_PARAMETER, parameter);
        Action connectAction = new Action(ACTION_CONNECT, params);
        drone.performAsyncAction(connectAction);
    }

    /**
     * Break connection with the vehicle.
     */
    public void disconnect() {
        drone.performAsyncAction(new Action(ACTION_DISCONNECT));
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm true to arm, false to disarm.
     */
    public void arm(boolean arm) {
        arm(arm, null);
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm      true to arm, false to disarm.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void arm(boolean arm, AbstractCommandListener listener) {
        arm(arm, false, listener);
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm             true to arm, false to disarm.
     * @param emergencyDisarm true to skip landing check and disarm immediately,
     *                        false to disarm only if it is safe to do so.
     * @param listener        Register a callback to receive update of the command execution state.
     */
    public void arm(boolean arm, boolean emergencyDisarm, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_ARM, arm);
        params.putBoolean(EXTRA_EMERGENCY_DISARM, emergencyDisarm);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_ARM, params), listener);
    }

    /**
     * Change the vehicle mode for the connected drone.
     *
     * @param newMode new vehicle mode.
     */
    public void setVehicleMode(VehicleMode newMode) {
        setVehicleMode(newMode, null);
    }

    /**
     * Change the vehicle mode for the connected drone.
     *
     * @param newMode  new vehicle mode.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void setVehicleMode(VehicleMode newMode, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_MODE, newMode);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VEHICLE_MODE, params), listener);
    }

    /**
     * Generate action used to refresh the parameters for the connected drone.
     */
    public void refreshParameters() {
        drone.performAsyncAction(new Action(ACTION_REFRESH_PARAMETERS));
    }

    /**
     * Generate action used to write the given parameters to the connected drone.
     *
     * @param parameters parameters to write to the drone.
     * @return
     */
    public void writeParameters(Parameters parameters) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_PARAMETERS, parameters);
        drone.performAsyncAction(new Action(ACTION_WRITE_PARAMETERS, params));
    }

    /*
    Deprecated apis
     */

    /**
     * @param altitude altitude in meters
     * @deprecated Use {@link ControlApi#takeoff(double, AbstractCommandListener)} instead.
     * Perform a guided take off.
     */
    public void takeoff(double altitude) {
        controlApi.takeoff(altitude, null);
    }

    /**
     * @param altitude altitude in meters
     * @param listener Register a callback to receive update of the command execution state.
     * @deprecated Use {@link ControlApi#takeoff(double, AbstractCommandListener)} instead.
     * Perform a guided take off.
     */
    public void takeoff(double altitude, AbstractCommandListener listener) {
        controlApi.takeoff(altitude, listener);
    }

    /**
     * @param point guided point location
     * @param force true to enable guided mode is required.
     * @deprecated Use {@link ControlApi#goTo(LatLong, boolean, AbstractCommandListener)} instead.
     * Send a guided point to the connected drone.
     */
    public void sendGuidedPoint(LatLong point, boolean force) {
        controlApi.goTo(point, force, null);
    }

    /**
     * @param point    guided point location
     * @param force    true to enable guided mode is required.
     * @param listener Register a callback to receive update of the command execution state.
     * @deprecated Use {@link ControlApi#goTo(LatLong, boolean, AbstractCommandListener)} instead.
     * Send a guided point to the connected drone.
     */
    public void sendGuidedPoint(LatLong point, boolean force, AbstractCommandListener listener) {
        controlApi.goTo(point, force, listener);
    }

    /**
     * @param altitude altitude in meters
     * @deprecated Use {@link ControlApi#climbTo(double)} instead.
     * Set the altitude for the guided point.
     */
    public void setGuidedAltitude(double altitude) {
        controlApi.climbTo(altitude);
    }

    /**
     * @deprecated Use {@link ControlApi#pauseAtCurrentLocation(AbstractCommandListener)} instead.
     * Pause the vehicle at its current location.
     */
    public void pauseAtCurrentLocation() {
        controlApi.pauseAtCurrentLocation(null);
    }

    /**
     * @param listener Register a callback to receive update of the command execution state.
     * @deprecated Use {@link ControlApi#pauseAtCurrentLocation(AbstractCommandListener)} instead.
     * Pause the vehicle at its current location.
     */
    public void pauseAtCurrentLocation(final AbstractCommandListener listener) {
        controlApi.pauseAtCurrentLocation(listener);
    }

    /**
     * Changes the vehicle home location.
     *
     * @param homeLocation New home coordinate
     * @param listener     Register a callback to receive update of the command execution state.
     */
    public void setVehicleHome(final LatLongAlt homeLocation, final AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_HOME_LOCATION, homeLocation);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VEHICLE_HOME, params), listener);
    }

    /**
     * Enables 'return to me'
     * @param isEnabled
     * @param listener
     */
    public void enableReturnToMe(boolean isEnabled, final AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_IS_RETURN_TO_ME_ENABLED, isEnabled);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_ENABLE_RETURN_TO_ME, params), listener);
    }
}
