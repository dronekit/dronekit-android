package org.droidplanner.services.android.api;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.three_dr.services.android.lib.drone.event.Event;
import com.three_dr.services.android.lib.drone.event.Extra;
import com.three_dr.services.android.lib.drone.property.Altitude;
import com.three_dr.services.android.lib.drone.property.Attitude;
import com.three_dr.services.android.lib.drone.property.Battery;
import com.three_dr.services.android.lib.drone.property.Gps;
import com.three_dr.services.android.lib.drone.property.Home;
import com.three_dr.services.android.lib.drone.property.Mission;
import com.three_dr.services.android.lib.drone.property.Parameters;
import com.three_dr.services.android.lib.drone.property.Speed;
import com.three_dr.services.android.lib.drone.property.State;
import com.three_dr.services.android.lib.drone.property.Type;
import com.three_dr.services.android.lib.drone.property.VehicleMode;
import com.three_dr.services.android.lib.model.IDroidPlannerApi;
import com.three_dr.services.android.lib.model.IDroidPlannerApiCallback;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.services.android.utils.file.IO.ParameterMetadataLoader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created by fhuya on 10/30/14.
*/
final class DPApi extends IDroidPlannerApi.Stub implements DroneInterfaces.OnDroneListener, DroneInterfaces.OnParameterManagerListener {

    private final static String TAG = DPApi.class.getSimpleName();

    private final Bundle emptyBundle = new Bundle();
    private final Context context;

    private IDroidPlannerApiCallback apiCallback;
    private Drone drone;

    DPApi(Context context, Drone drone, IDroidPlannerApiCallback callback) throws RemoteException {
        this.context = context;
        this.apiCallback = callback;
        this.drone = drone;
        start();
    }

    private void start() throws RemoteException {
        final Drone drone = getDrone();
        final IDroidPlannerApiCallback callback = getCallback();

        drone.addDroneListener(this);

        if(!drone.getMavClient().isConnected())
            drone.getMavClient().toggleConnectionState();
        else
            callback.onDroneEvent(Event.EVENT_CONNECTED, emptyBundle);

        drone.getParameters().setParameterListener(this);
    }

    private void stop() throws RemoteException {
        final Drone drone = getDrone();
        final IDroidPlannerApiCallback callback = getCallback();

        drone.removeDroneListener(this);
        drone.getParameters().setParameterListener(null);

        if(drone.getMavClient().isConnected())
            drone.getMavClient().toggleConnectionState();

        this.apiCallback = null;
        this.drone = null;

        callback.onDroneEvent(Event.EVENT_DISCONNECTED, emptyBundle);
    }

    private Drone getDrone(){
        if(drone == null)
            throw new IllegalStateException("Disconnected from drone");

        return drone;
    }

    private IDroidPlannerApiCallback getCallback(){
        if(apiCallback == null)
            throw new IllegalStateException("Disconnected from drone");

        return apiCallback;
    }

    @Override
    public Gps getGps() throws RemoteException {
        final GPS droneGps = getDrone().getGps();
        return new Gps((float) droneGps.getPosition().getLat(), (float) droneGps.getPosition()
                .getLng(), (float) droneGps.getGpsEPH(), droneGps.getSatCount(),
                droneGps.getFixTypeNumeric());
    }

    @Override
    public State getState() throws RemoteException {
        final Drone drone = getDrone();
        org.droidplanner.core.drone.variables.State droneState = drone.getState();
        ApmModes droneMode = droneState.getMode();

        State proxyState = new State(null, getDroneProxyType(drone.getType()), droneState.isArmed(),
                droneState.isFlying(), droneState.getWarning());

        return proxyState;
    }

    private static Type getDroneProxyType(int originalType){
        switch(originalType){
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return new Type(Type.TYPE_COPTER);

            case MAV_TYPE.MAV_TYPE_FIXED_WING:
                return new Type(Type.TYPE_PLANE);

            case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
            case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
                return new Type(Type.TYPE_ROVER);

            default:
                return null;
        }
    }

    @Override
    public Parameters getParameters() throws RemoteException {
        final Drone drone = getDrone();
        final Map<String, com.three_dr.services.android.lib.drone.property.Parameter> proxyParams =
                new HashMap<String, com.three_dr.services.android.lib.drone.property.Parameter>();

        List<Parameter> droneParameters = drone.getParameters().getParametersList();
        if(!droneParameters.isEmpty()){
            for(Parameter param : droneParameters){
                proxyParams.put(param.name, new com.three_dr.services.android.lib.drone.property
                        .Parameter(param.name, param.value, param.type));
            }

            try {
                //TODO: implement drone metadata type
                ParameterMetadataLoader.load(context, null, proxyParams);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return new Parameters(new ArrayList<com.three_dr.services.android.lib.drone.property
                .Parameter>(proxyParams.values()));
    }

    @Override
    public Speed getSpeed() throws RemoteException {
        org.droidplanner.core.drone.variables.Speed droneSpeed = getDrone().getSpeed();
        return new Speed(droneSpeed.getVerticalSpeed().valueInMetersPerSecond(),
                droneSpeed.getGroundSpeed().valueInMetersPerSecond(),
                droneSpeed.getAirSpeed().valueInMetersPerSecond());
    }

    @Override
    public Attitude getAttitude() throws RemoteException {
        Orientation droneOrientation = getDrone().getOrientation();
        return new Attitude(droneOrientation.getRoll(), droneOrientation.getPitch(),
                droneOrientation.getYaw());
    }

    @Override
    public Home getHome() throws RemoteException {
        org.droidplanner.core.drone.variables.Home droneHome = getDrone().getHome();
        return new Home((float) droneHome.getCoord().getLat(), (float) droneHome.getCoord()
                .getLng(), (float) droneHome.getAltitude().valueInMeters());
    }

    @Override
    public Battery getBattery() throws RemoteException {
        org.droidplanner.core.drone.variables.Battery droneBattery = getDrone().getBattery();
        return new Battery(droneBattery.getBattVolt(), droneBattery.getBattRemain(),
                droneBattery.getBattCurrent());
    }

    @Override
    public Altitude getAltitude() throws RemoteException {
        org.droidplanner.core.drone.variables.Altitude droneAltitude = getDrone().getAltitude();
        return new Altitude(droneAltitude.getAltitude(), droneAltitude.getTargetAltitude());
    }

    @Override
    public Mission getMission() throws RemoteException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public boolean isConnected() throws RemoteException {
        return drone != null && drone.getMavClient().isConnected();
    }

    @Override
    public void changeVehicleMode(String newModeName) throws RemoteException {
        VehicleMode newMode = VehicleMode.valueOf(newModeName);

        int mavType;
        switch(newMode.getDroneType()){
            default:
            case Type.TYPE_COPTER:
                mavType = MAV_TYPE.MAV_TYPE_QUADROTOR;
                break;

            case Type.TYPE_PLANE:
                mavType = MAV_TYPE.MAV_TYPE_FIXED_WING;
                break;

            case Type.TYPE_ROVER:
                mavType = MAV_TYPE.MAV_TYPE_GROUND_ROVER;
                break;
        }

        getDrone().getState().changeFlightMode(ApmModes.getMode(newMode.getMode(), mavType));
    }

    @Override
    public void disconnectFromDrone() throws RemoteException {
        stop();
    }

    @Override
    public void refreshParameters() throws RemoteException {
        getDrone().getParameters().refreshParameters();
    }

    @Override
    public void writeParameters(Parameters parameters) throws RemoteException {
        if(parameters == null) return;

        List<com.three_dr.services.android.lib.drone.property.Parameter> parametersList = parameters
                .getParameters();
        if(parametersList.isEmpty())
            return;

        final Drone drone = getDrone();
        org.droidplanner.core.drone.profiles.Parameters droneParams = drone.getParameters();
        for(com.three_dr.services.android.lib.drone.property.Parameter proxyParam : parametersList){
            droneParams.sendParameter(new Parameter(proxyParam.getName(), proxyParam.getValue(),
                    proxyParam.getType()));
        }
    }

    @Override
    public void sendMission(Mission mission) throws RemoteException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        final IDroidPlannerApiCallback callback = getCallback();

        try {
            switch (event) {
                case DISCONNECTED:
                    disconnectFromDrone();
                    break;

                case TYPE:
                    callback.onDroneEvent(Event.EVENT_TYPE_UPDATED, emptyBundle);
                    break;
            }
        }catch(RemoteException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        try {
            getCallback().onDroneEvent(Event.EVENT_PARAMETERS_REFRESH_STARTED, emptyBundle);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        try{
            Bundle paramsBundle = new Bundle(2);
            paramsBundle.putInt(Extra.EXTRA_PARAMETER_INDEX, index);
            paramsBundle.putInt(Extra.EXTRA_PARAMETERS_COUNT, count);
            getCallback().onDroneEvent(Event.EVENT_PARAMETERS_RECEIVED, paramsBundle);
        } catch(RemoteException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameter) {
        try{
            getCallback().onDroneEvent(Event.EVENT_PARAMETERS_REFRESH_ENDED, emptyBundle);
        } catch(RemoteException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
