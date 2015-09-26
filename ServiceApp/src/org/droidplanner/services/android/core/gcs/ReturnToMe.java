package org.droidplanner.services.android.core.gcs;

import android.os.Bundle;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.gcs.returnToMe.ReturnToMeState;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.DroneInterfaces.AttributeEventListener;
import org.droidplanner.services.android.core.drone.DroneManager;
import org.droidplanner.services.android.core.drone.autopilot.Drone;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.core.drone.variables.Home;
import org.droidplanner.services.android.core.gcs.location.Location;
import org.droidplanner.services.android.core.helpers.coordinates.Coord3D;
import org.droidplanner.services.android.utils.CommonApiUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

/**
 * Return to me implementation.
 * If enabled, listen for user's gps location updates, and accordingly updates the vehicle RTL location.
 * Created by Fredia Huya-Kouadio on 9/21/15.
 */
public class ReturnToMe implements DroneInterfaces.OnDroneListener, Location.LocationReceiver {

    public static final int UPDATE_MINIMAL_DISPLACEMENT = 5; //meters

    private static final String TAG = ReturnToMe.class.getSimpleName();

    private final AtomicBoolean isEnabled = new AtomicBoolean(false);
    private final ReturnToMeState currentState;

    private final DroneManager droneMgr;
    private final Location.LocationFinder locationFinder;
    private final AttributeEventListener attributeListener;

    private ICommandListener commandListener;

    public ReturnToMe(DroneManager droneMgr, Location.LocationFinder locationFinder, AttributeEventListener listener){
        this.droneMgr = droneMgr;
        this.locationFinder = locationFinder;
        locationFinder.addLocationListener(TAG, this);

        this.attributeListener = listener;
        this.currentState = new ReturnToMeState();

        final MavLinkDrone drone = droneMgr.getDrone();
        drone.addDroneListener(this);
    }

    public void enable(ICommandListener listener){
        if(isEnabled.compareAndSet(false, true)){
            this.commandListener = listener;

            final Home droneHome = droneMgr.getDrone().getHome();
            if(droneHome.isValid()){
                currentState.setOriginalHomeLocation(droneHome.getCoord());
            }

            //Enable return to me
            Timber.i("Enabling return to me.");
            locationFinder.enableLocationUpdates();
            updateCurrentState(ReturnToMeState.STATE_WAITING_FOR_VEHICLE_GPS);
        }
    }

    public void disable(){
        if(isEnabled.compareAndSet(true, false)){
            //Disable return to me
            Timber.i("Disabling return to me.");
            locationFinder.disableLocationUpdates();

            currentState.setCurrentHomeLocation(null);
            updateCurrentState(ReturnToMeState.STATE_IDLE);

            this.commandListener = null;
        }
    }

    @Override
    public void onLocationUpdate(Location location) {
        if(location.isAccurate()){
            final Home home = getHome();
            if(!home.isValid()) {
                updateCurrentState(ReturnToMeState.STATE_WAITING_FOR_VEHICLE_GPS);
                return;
            }

            final LatLongAlt homePosition = home.getCoord();

            //Calculate the displacement between the home location and the user location.
            final Coord3D locationCoord = location.getCoord();

            final float[] results = new float[3];
            android.location.Location.distanceBetween(homePosition.getLatitude(), homePosition.getLongitude(),
                    locationCoord.getLat(), locationCoord.getLng(), results);
            final float displacement = results[0];

            if(displacement >= UPDATE_MINIMAL_DISPLACEMENT){
                MavLinkDoCmds.setVehicleHome(droneMgr.getDrone(),
                        new LatLongAlt(locationCoord.getLat(), locationCoord.getLng(), homePosition.getAltitude()),
                        new AbstractCommandListener() {
                            @Override
                            public void onSuccess() {
                                Timber.i("Updated vehicle home location to %s", locationCoord.toString());
                                home.requestHomeUpdate();
                                CommonApiUtils.postSuccessEvent(commandListener);
                                updateCurrentState(ReturnToMeState.STATE_UPDATING_HOME);
                            }

                            @Override
                            public void onError(int executionError) {
                                Timber.e("Unable to update vehicle home location: %d", executionError);
                                CommonApiUtils.postErrorEvent(executionError, commandListener);
                                updateCurrentState(ReturnToMeState.STATE_ERROR_UPDATING_HOME);

                            }

                            @Override
                            public void onTimeout() {
                                Timber.w("Vehicle home update timed out!");
                                CommonApiUtils.postTimeoutEvent(commandListener);
                                updateCurrentState(ReturnToMeState.STATE_ERROR_UPDATING_HOME);
                            }
                        });
            }
        }
        else{
            updateCurrentState(ReturnToMeState.STATE_USER_LOCATION_INACCURATE);
        }
    }

    private Home getHome(){
        return droneMgr.getDrone().getHome();
    }

    @Override
    public void onLocationUnavailable() {
        if(isEnabled.get()){
            updateCurrentState(ReturnToMeState.STATE_USER_LOCATION_UNAVAILABLE);
            disable();
        }
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, MavLinkDrone drone) {
        switch(event){
            case DISCONNECTED:
                //Stops updating the vehicle RTL location
                disable();
                break;

            case HOME:
                if(isEnabled.get()) {
                    final LatLongAlt homeCoord = drone.getHome().getCoord();
                    if (currentState.getOriginalHomeLocation() == null)
                        currentState.setOriginalHomeLocation(homeCoord);
                    else {
                        currentState.setCurrentHomeLocation(homeCoord);
                    }
                }
                break;
        }
    }

    private void updateCurrentState(@ReturnToMeState.ReturnToMeStates int state){
        this.currentState.setState(state);
        if(attributeListener != null){
            final Bundle eventInfo = new Bundle();
            eventInfo.putInt(AttributeEventExtra.EXTRA_RETURN_TO_ME_STATE, state);
            attributeListener.onAttributeEvent(AttributeEvent.RETURN_TO_ME_STATE_UPDATE, eventInfo);
        }
    }

    public DroneAttribute getState() {
        return currentState;
    }
}
