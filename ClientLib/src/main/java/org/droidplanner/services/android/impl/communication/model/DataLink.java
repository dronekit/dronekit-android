package org.droidplanner.services.android.impl.communication.model;

import org.droidplanner.services.android.lib.gcs.link.LinkConnectionStatus;
import org.droidplanner.services.android.lib.model.ICommandListener;

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
