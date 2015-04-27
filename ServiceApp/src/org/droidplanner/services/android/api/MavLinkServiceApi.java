package org.droidplanner.services.android.api;

import com.MAVLink.MAVLinkPacket;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;
import org.droidplanner.services.android.communication.connection.AndroidMavLinkConnection;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * MavLinkService app api.
 */
public class MavLinkServiceApi {

    private final SoftReference<DroidPlannerService> mServiceRef;

    public MavLinkServiceApi(DroidPlannerService service) {
        mServiceRef = new SoftReference<>(service);
    }

    private DroidPlannerService getService() {
        DroidPlannerService service = mServiceRef.get();
        if (service == null)
            throw new IllegalStateException("Lost reference to parent service.");

        return service;
    }

    public boolean sendData(ConnectionParameter connParams, MAVLinkPacket packet) {
        final AndroidMavLinkConnection mavConnection = getService().mavConnections.get(connParams.getUniqueId());
        if (mavConnection == null) return false;

        if (mavConnection.getConnectionStatus() != MavLinkConnection.MAVLINK_DISCONNECTED) {
            mavConnection.sendMavPacket(packet);
            return true;
        }

        return false;
    }

    public int getConnectionStatus(ConnectionParameter connParams, String tag) {
        final AndroidMavLinkConnection mavConnection = getService().mavConnections.get(connParams.getUniqueId());
        if (mavConnection == null || !mavConnection.hasMavLinkConnectionListener(tag)) {
            return MavLinkConnection.MAVLINK_DISCONNECTED;
        }

        return mavConnection.getConnectionStatus();
    }

    public void connectMavLink(ConnectionParameter connParams, String tag, MavLinkConnectionListener listener) {
        getService().connectMAVConnection(connParams, tag, listener);
    }

    public void addLoggingFile(ConnectionParameter connParams, String tag, String loggingFilePath){
        getService().addLoggingFile(connParams, tag, loggingFilePath);
    }

    public void removeLoggingFile(ConnectionParameter connParams, String tag){
        getService().removeLoggingFile(connParams, tag);
    }

    public void disconnectMavLink(ConnectionParameter connParams, String tag) {
        getService().disconnectMAVConnection(connParams, tag);
    }
}
