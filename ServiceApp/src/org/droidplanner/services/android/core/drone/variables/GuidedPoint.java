package org.droidplanner.services.android.core.drone.variables;

import android.os.Handler;
import android.os.RemoteException;

import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.droidplanner.services.android.core.MAVLink.MavLinkModes;
import org.droidplanner.services.android.core.MAVLink.MavLinkTakeoff;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.core.drone.DroneVariable;
import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

import timber.log.Timber;

public class GuidedPoint extends DroneVariable implements OnDroneListener {

    private GuidedStates state = GuidedStates.UNINITIALIZED;
    private Coord2D coord = new Coord2D(0, 0);
    private double altitude = 0.0; //altitude in meters

    private Runnable mPostInitializationTask;

    private final Handler handler;

    public enum GuidedStates {
        UNINITIALIZED, IDLE, ACTIVE
    }

    public GuidedPoint(MavLinkDrone myDrone, Handler handler) {
        super(myDrone);
        this.handler = handler;
        myDrone.addDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case HEARTBEAT_FIRST:
            case HEARTBEAT_RESTORED:
            case MODE:
                if (isGuidedMode(myDrone)) {
                    initialize();
                } else {
                    disable();
                }
                break;

            case DISCONNECTED:
            case HEARTBEAT_TIMEOUT:
                disable();

            default:
                break;
        }
    }

    public static boolean isGuidedMode(MavLinkDrone drone) {
        if(drone == null)
            return false;

        final int droneType = drone.getType();
        final ApmModes droneMode = drone.getState().getMode();

        if (Type.isCopter(droneType)) {
            return droneMode == ApmModes.ROTOR_GUIDED;
        }

        if (Type.isPlane(droneType)) {
            return droneMode == ApmModes.FIXED_WING_GUIDED;
        }

        if (Type.isRover(droneType)) {
            return droneMode == ApmModes.ROVER_GUIDED || droneMode == ApmModes.ROVER_HOLD;
        }

        return false;
    }

    public void pauseAtCurrentLocation(ICommandListener listener) {
        if (state == GuidedStates.UNINITIALIZED) {
            changeToGuidedMode(myDrone, listener);
        } else {
            newGuidedCoord(myDrone.getGps().getPosition());
            state = GuidedStates.IDLE;
        }
    }

    public static void changeToGuidedMode(MavLinkDrone drone, ICommandListener listener) {
        final State droneState = drone.getState();
        final int droneType = drone.getType();

        if (Type.isCopter(droneType)) {
            droneState.changeFlightMode(ApmModes.ROTOR_GUIDED, listener);
        } else if (Type.isPlane(droneType)) {
            //You have to send a guided point to the plane in order to trigger guided mode.
            forceSendGuidedPoint(drone, drone.getGps().getPosition(), getDroneAltConstrained(drone));
        } else if (Type.isRover(droneType)) {
            droneState.changeFlightMode(ApmModes.ROVER_GUIDED, listener);
        }
    }

    public void doGuidedTakeoff(final double alt, final ICommandListener listener) {
        if (Type.isCopter(myDrone.getType())) {
            coord = myDrone.getGps().getPosition();
            altitude = alt;
            state = GuidedStates.IDLE;

            changeToGuidedMode(myDrone, new SimpleCommandListener() {
                @Override
                public void onSuccess() {
                    MavLinkTakeoff.sendTakeoff(myDrone, alt, listener);
                    myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
                }

                @Override
                public void onError(int executionError){
                    if(listener != null){
                        try {
                            listener.onError(executionError);
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                }

                @Override
                public void onTimeout(){
                    if(listener != null){
                        try {
                            listener.onTimeout();
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                }
            });
        }
        else{
            if(listener !=  null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listener.onError(CommandExecutionError.COMMAND_UNSUPPORTED);
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                });
            }
        }
    }

    public void newGuidedCoord(Coord2D coord) {
        changeCoord(coord);
    }

    public void newGuidedPosition(double latitude, double longitude, double altitude) {
        MavLinkModes.sendGuidedPosition(myDrone, latitude, longitude, altitude);
    }

    public void newGuidedVelocity(double xVel, double yVel, double zVel) {
        MavLinkModes.sendGuidedVelocity(myDrone, xVel, yVel, zVel);
    }

    public void newGuidedCoordAndVelocity(Coord2D coord, double xVel, double yVel, double zVel) {
        changeCoordAndVelocity(coord, xVel, yVel, zVel);
    }

    public void changeGuidedAltitude(double alt) {
        changeAlt(alt);
    }

    public void forcedGuidedCoordinate(final Coord2D coord, final ICommandListener listener) {
        if (!myDrone.getGps().hasGpsLock()) {
            postErrorEvent(handler, listener, CommandExecutionError.COMMAND_FAILED);
            return;
        }

        if (isInitialized()) {
            changeCoord(coord);
            postSuccessEvent(handler, listener);
        } else {
            mPostInitializationTask = new Runnable() {
                @Override
                public void run() {
                    changeCoord(coord);
                }
            };

            changeToGuidedMode(myDrone, listener);
        }
    }

    public void forcedGuidedCoordinate(final Coord2D coord, final double alt, final ICommandListener listener) {
        if (!myDrone.getGps().hasGpsLock()) {
            postErrorEvent(handler, listener, CommandExecutionError.COMMAND_FAILED);
            return;
        }

        if (isInitialized()) {
            changeCoord(coord);
            changeAlt(alt);
            postSuccessEvent(handler, listener);
        } else {
            mPostInitializationTask = new Runnable() {
                @Override
                public void run() {
                    changeCoord(coord);
                    changeAlt(alt);
                }
            };

            changeToGuidedMode(myDrone, listener);
        }
    }

    private void initialize() {
        if (state == GuidedStates.UNINITIALIZED) {
            coord = myDrone.getGps().getPosition();
            altitude = getDroneAltConstrained(myDrone);
            state = GuidedStates.IDLE;
            myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        }

        if (mPostInitializationTask != null) {
            mPostInitializationTask.run();
            mPostInitializationTask = null;
        }
    }

    private void disable() {
        if(state == GuidedStates.UNINITIALIZED)
            return;

        state = GuidedStates.UNINITIALIZED;
        myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
    }

    private void changeAlt(double alt) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/

            case ACTIVE:
                altitude = alt;
                sendGuidedPoint();
                break;
        }
    }

    private void changeCoord(Coord2D coord) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/
            case ACTIVE:
                this.coord = coord;
                sendGuidedPoint();
                break;
        }
    }

    private void changeCoordAndVelocity(Coord2D coord, double xVel, double yVel, double zVel) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/
            case ACTIVE:
                this.coord = coord;
                sendGuidedPointAndVelocity(xVel, yVel, zVel);
                break;
        }
    }

    private void sendGuidedPointAndVelocity(double xVel, double yVel, double zVel) {
        if (state == GuidedStates.ACTIVE) {
            forceSendGuidedPointAndVelocity(myDrone, coord, altitude, xVel, yVel, zVel);
        }
    }

    private void sendGuidedPoint() {
        if (state == GuidedStates.ACTIVE) {
            forceSendGuidedPoint(myDrone, coord, altitude);
        }
    }

    public static void forceSendGuidedPoint(MavLinkDrone drone, Coord2D coord, double altitudeInMeters) {
        drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        if (coord != null) {
            MavLinkModes.setGuidedMode(drone, coord.getLat(), coord.getLng(), altitudeInMeters);
        }
    }

    public static void forceSendGuidedPointAndVelocity(MavLinkDrone drone, Coord2D coord, double altitudeInMeters,
                                                       double xVel, double yVel, double zVel) {
        drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        if (coord != null) {
            MavLinkModes.sendGuidedPositionAndVelocity(drone, coord.getLat(), coord.getLng(), altitudeInMeters, xVel,
                    yVel, zVel);
        }
    }

    private static double getDroneAltConstrained(MavLinkDrone drone) {
        final Altitude droneAltitude = (Altitude) drone.getAttribute(AttributeType.ALTITUDE);
        double alt = Math.floor(droneAltitude.getAltitude());
        return Math.max(alt, getDefaultMinAltitude(drone));
    }

    public Coord2D getCoord() {
        return coord;
    }

    public double getAltitude() {
        return this.altitude;
    }

    public boolean isActive() {
        return (state == GuidedStates.ACTIVE);
    }

    public boolean isIdle() {
        return (state == GuidedStates.IDLE);
    }

    public boolean isInitialized() {
        return !(state == GuidedStates.UNINITIALIZED);
    }

    public GuidedStates getState() {
        return state;
    }

    public static float getDefaultMinAltitude(MavLinkDrone drone) {
        final int droneType = drone.getType();
        if (Type.isCopter(droneType)) {
            return 2f;
        } else if (Type.isPlane(droneType)) {
            return 15f;
        } else {
            return 0f;
        }
    }

}
