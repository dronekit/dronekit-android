package org.droidplanner.services.android.communication.model;

import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.ICommandListener;

public class DataLink {

    public interface DataLinkProvider<T> {

        void sendMessage(T message, ICommandListener listener);

        boolean isConnected();

        void openConnection();

        void closeConnection();

    }

    public interface DataLinkListener<T> {

        void notifyReceivedData(T packet);

        void onConnectionStatus(LinkConnectionStatus connectionStatus);
    }
}
