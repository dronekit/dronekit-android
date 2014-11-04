package org.droidplanner.services.android.api;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.drone.connection.ConnectionResult;
import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.event.Extra;
import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.Home;
import com.ox3dr.services.android.lib.drone.property.Mission;
import com.ox3dr.services.android.lib.drone.property.Speed;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.Type;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;
import com.ox3dr.services.android.lib.drone.property.Parameters;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerApiCallback;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.services.android.drone.DroneManager;
import org.droidplanner.services.android.exception.ConnectionException;
import org.droidplanner.services.android.interfaces.DroneEventsListener;
import org.droidplanner.services.android.utils.file.IO.ParameterMetadataLoader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created by fhuya on 10/30/14.
*/
final class DPApi extends IDroidPlannerApi.Stub implements DroneEventsListener {

    private final static String TAG = DPApi.class.getSimpleName();

    private final Bundle emptyBundle = new Bundle();
    private final WeakReference<DroidPlannerService> serviceRef;

    private IDroidPlannerApiCallback apiCallback;
    private ConnectionParameter connParams;
    private DroneManager droneMgr;

    DPApi(DroidPlannerService dpService, ConnectionParameter connParams,
          IDroidPlannerApiCallback callback) throws RemoteException {
        serviceRef = new WeakReference<DroidPlannerService>(dpService);

        this.apiCallback = callback;
        this.connParams = connParams;

        try {
            this.droneMgr = dpService.getDroneForConnection(connParams);
            start();
        } catch (ConnectionException e) {
            callback.onConnectionFailed(new ConnectionResult(0, e.getMessage()));
            disconnectFromDrone();
        }
    }

    private DroidPlannerService getService() {
        final DroidPlannerService service = serviceRef.get();
        if (service == null)
            throw new IllegalStateException("Lost reference to parent service.");

        return service;
    }

    private DroneManager getDroneMgr(){
        if(droneMgr == null)
            throw new IllegalStateException("Invalid state: drone manager is null");

        return droneMgr;
    }

    private IDroidPlannerApiCallback getCallback(){
        if(apiCallback == null)
            throw new IllegalStateException("Invalid state: api callback is null");

        return apiCallback;
    }

    private ConnectionParameter getConnectionParameter(){
        if(connParams == null)
            throw new IllegalStateException("Invalid state: connection parameter is null");

        return connParams;
    }

    private void start() throws RemoteException {
        getDroneMgr().addDroneEventsListener(this);
    }

    @Override
    public void disconnectFromDrone() throws RemoteException {
        getDroneMgr().removeDroneEventsListener(this);
        getService().disconnectFromApi(getConnectionParameter(), getCallback());
        this.apiCallback = null;
        this.connParams = null;
        this.droneMgr = null;
    }

    @Override
    public Gps getGps() throws RemoteException {
        final GPS droneGps = getDroneMgr().getDrone().getGps();
        return new Gps((float) droneGps.getPosition().getLat(), (float) droneGps.getPosition()
                .getLng(), (float) droneGps.getGpsEPH(), droneGps.getSatCount(),
                droneGps.getFixTypeNumeric());
    }

    @Override
    public State getState() throws RemoteException {
        final Drone drone =getDroneMgr().getDrone();
        org.droidplanner.core.drone.variables.State droneState = drone.getState();
        ApmModes droneMode = droneState.getMode();

        State proxyState = new State(getProxyMode(droneMode), droneState.isArmed(),
                droneState.isFlying(), droneState.getWarning());

        return proxyState;
    }

    private static VehicleMode getProxyMode(ApmModes mode){
        final int proxyType = getDroneProxyType(mode.getType());
        if(proxyType == -1) return null;

        return new VehicleMode(mode.getNumber(), proxyType, mode.getName());
    }

    @Override
    public VehicleMode[] getAllVehicleModes() throws RemoteException {
        final int droneType = getDroneMgr().getDrone().getType();
        final int proxyType = getDroneProxyType(droneType);

        List<ApmModes> typeModes = ApmModes.getModeList(droneType);
        final int modesCount = typeModes.size();
        VehicleMode[] vehicleModes = new VehicleMode[modesCount];
        for(int i = 0; i < modesCount; i++){
            ApmModes mode = typeModes.get(i);
            vehicleModes[i] = new VehicleMode(mode.getNumber(), proxyType, mode.getName());
        }

        return vehicleModes;
    }

    private static int getDroneProxyType(int originalType){
        switch(originalType){
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return Type.TYPE_COPTER;

            case MAV_TYPE.MAV_TYPE_FIXED_WING:
                return Type.TYPE_PLANE;

            case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
            case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
                return Type.TYPE_ROVER;

            default:
                return -1;
        }
    }

    @Override
    public Parameters getParameters() throws RemoteException {
        final Drone drone =getDroneMgr().getDrone();
        final Map<String, com.ox3dr.services.android.lib.drone.property.Parameter> proxyParams =
                new HashMap<String, com.ox3dr.services.android.lib.drone.property.Parameter>();

        List<Parameter> droneParameters = drone.getParameters().getParametersList();
        if(!droneParameters.isEmpty()){
            for(Parameter param : droneParameters){
                proxyParams.put(param.name, new com.ox3dr.services.android.lib.drone.property
                        .Parameter(param.name, param.value, param.type));
            }

            try {
                //TODO: implement drone metadata type
                ParameterMetadataLoader.load(getService().getApplicationContext(), null, proxyParams);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return new Parameters(new ArrayList<com.ox3dr.services.android.lib.drone.property
                .Parameter>(proxyParams.values()));
    }

    @Override
    public Speed getSpeed() throws RemoteException {
        org.droidplanner.core.drone.variables.Speed droneSpeed =getDroneMgr().getDrone().getSpeed();
        return new Speed(droneSpeed.getVerticalSpeed().valueInMetersPerSecond(),
                droneSpeed.getGroundSpeed().valueInMetersPerSecond(),
                droneSpeed.getAirSpeed().valueInMetersPerSecond());
    }

    @Override
    public Attitude getAttitude() throws RemoteException {
        Orientation droneOrientation =getDroneMgr().getDrone().getOrientation();
        return new Attitude(droneOrientation.getRoll(), droneOrientation.getPitch(),
                droneOrientation.getYaw());
    }

    @Override
    public Home getHome() throws RemoteException {
        org.droidplanner.core.drone.variables.Home droneHome =getDroneMgr().getDrone().getHome();
        return new Home((float) droneHome.getCoord().getLat(), (float) droneHome.getCoord()
                .getLng(), (float) droneHome.getAltitude().valueInMeters());
    }

    @Override
    public Battery getBattery() throws RemoteException {
        org.droidplanner.core.drone.variables.Battery droneBattery =getDroneMgr().getDrone().getBattery();
        return new Battery(droneBattery.getBattVolt(), droneBattery.getBattRemain(),
                droneBattery.getBattCurrent());
    }

    @Override
    public Altitude getAltitude() throws RemoteException {
        org.droidplanner.core.drone.variables.Altitude droneAltitude =getDroneMgr().getDrone().getAltitude();
        return new Altitude(droneAltitude.getAltitude(), droneAltitude.getTargetAltitude());
    }

    @Override
    public Mission getMission() throws RemoteException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Type getType() throws RemoteException {
        final Drone drone = getDroneMgr().getDrone();
        return new Type(getDroneProxyType(drone.getType()), drone.getFirmwareVersion());
    }

    @Override
    public boolean isConnected() throws RemoteException {
        return getDroneMgr().isConnected();
    }

    @Override
    public void changeVehicleMode(VehicleMode newMode) throws RemoteException {
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

       getDroneMgr().getDrone().getState().changeFlightMode(ApmModes.getMode(newMode.getMode(), mavType));
    }

    @Override
    public void refreshParameters() throws RemoteException {
       getDroneMgr().getDrone().getParameters().refreshParameters();
    }

    @Override
    public void writeParameters(Parameters parameters) throws RemoteException {
        if(parameters == null) return;

        List<com.ox3dr.services.android.lib.drone.property.Parameter> parametersList = parameters
                .getParameters();
        if(parametersList.isEmpty())
            return;

        final Drone drone =getDroneMgr().getDrone();
        org.droidplanner.core.drone.profiles.Parameters droneParams = drone.getParameters();
        for(com.ox3dr.services.android.lib.drone.property.Parameter proxyParam : parametersList){
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
                    callback.onDroneEvent(Event.EVENT_DISCONNECTED, emptyBundle);
                    break;

                case GUIDEDPOINT:
                    break;
                case NAVIGATION:
                    break;

                case RADIO:
                    callback.onDroneEvent(Event.EVENT_RADIO, emptyBundle);
                    break;

                case RC_IN:
                    break;
                case RC_OUT:
                    break;

                case ARMING:
                    callback.onDroneEvent(Event.EVENT_ARMING, emptyBundle);
                    break;

                case AUTOPILOT_WARNING:
                    callback.onDroneEvent(Event.EVENT_AUTOPILOT_FAILSAFE, emptyBundle);
                    break;

                case MODE:
                    callback.onDroneEvent(Event.EVENT_VEHICLE_MODE, emptyBundle);
                    break;

                case ATTITUDE:
                case ORIENTATION:
                    callback.onDroneEvent(Event.EVENT_ATTITUDE, emptyBundle);
                    break;

                case SPEED:
                    callback.onDroneEvent(Event.EVENT_SPEED, emptyBundle);
                    break;

                case BATTERY:
                    callback.onDroneEvent(Event.EVENT_BATTERY, emptyBundle);
                    break;

                case STATE:
                    callback.onDroneEvent(Event.EVENT_STATE, emptyBundle);
                    break;

                case MISSION_UPDATE:
                    break;
                case MISSION_RECEIVED:
                    break;

                case FIRMWARE:
                case TYPE:
                    callback.onDroneEvent(Event.EVENT_TYPE_UPDATED, emptyBundle);
                    break;

                case HOME:
                    callback.onDroneEvent(Event.EVENT_HOME, emptyBundle);
                    break;

                case GPS:
                case GPS_FIX:
                case GPS_COUNT:
                    callback.onDroneEvent(Event.EVENT_GPS, emptyBundle);
                    break;

                case PARAMETER:
                case PARAMETERS_DOWNLOADED:
                    callback.onDroneEvent(Event.EVENT_PARAMETERS_RECEIVED, emptyBundle);
                    break;

                case CALIBRATION_IMU:
                    callback.onDroneEvent(Event.EVENT_CALIBRATION_IMU, emptyBundle);
                    break;

                case CALIBRATION_TIMEOUT:
                    callback.onDroneEvent(Event.EVENT_CALIBRATION_TIMEOUT, emptyBundle);
                    break;

                case HEARTBEAT_TIMEOUT:
                    callback.onDroneEvent(Event.EVENT_HEARTBEAT_TIMEOUT, emptyBundle);
                    break;

                case HEARTBEAT_FIRST:
                    callback.onDroneEvent(Event.EVENT_HEARTBEAT_FIRST, emptyBundle);
                    break;

                case HEARTBEAT_RESTORED:
                    callback.onDroneEvent(Event.EVENT_HEARTBEAT_RESTORED, emptyBundle);
                    break;

                case CONNECTED:
                    callback.onDroneEvent(Event.EVENT_CONNECTED, emptyBundle);
                    break;

                case MISSION_SENT:
                    break;
                case ARMING_STARTED:
                    break;
                case INVALID_POLYGON:
                    break;
                case MISSION_WP_UPDATE:
                    break;
                case FOLLOW_START:
                    break;
                case FOLLOW_STOP:
                    break;
                case FOLLOW_UPDATE:
                    break;
                case FOLLOW_CHANGE_TYPE:
                    break;
                case WARNING_400FT_EXCEEDED:
                    break;
                case WARNING_SIGNAL_WEAK:
                    break;
                case WARNING_NO_GPS:
                    break;
                case MAGNETOMETER:
                    break;
                case FOOTPRINT:
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
