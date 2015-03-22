package org.droidplanner.services.android.mock;

import com.MAVLink.MAVLinkPacket;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;


import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;

import org.droidplanner.services.android.api.DroidPlannerService;
import org.droidplanner.services.android.api.MavLinkServiceApi;


/**
 * Created by djmedina on 3/5/15.
 */
public class MockMavLinkServiceAPI extends MavLinkServiceApi{

    private MAVLinkPacket data;

    public MockMavLinkServiceAPI(){
        super(null);
    }

    public MockMavLinkServiceAPI(DroidPlannerService service) {
        super(service);
    }

    @Override
    public boolean sendData(ConnectionParameter connParams, MAVLinkPacket packet) {
        data = packet;
        return true;
    }

    /**
     * This method is used by the test services to validate the structure of the
     * MAVLinkPacket sent during this request
     * @return  MAVLinkPacket that was sent
     */
    public MAVLinkPacket getData(){

        return data;

    }

    @Override
    public int getConnectionStatus(ConnectionParameter connParams, String tag) {
        return MavLinkConnection.MAVLINK_CONNECTED;
    }
    @Override
    public void connectMavLink(ConnectionParameter connParams, String tag,
                               MavLinkConnectionListener listener) {

    }

    @Override
    public void addLoggingFile(ConnectionParameter connParams, String tag, String loggingFilePath){

    }

    @Override
    public void removeLoggingFile(ConnectionParameter connParams, String tag){

    }

    @Override
    public void disconnectMavLink(ConnectionParameter connParams, String tag) {

    }
}
