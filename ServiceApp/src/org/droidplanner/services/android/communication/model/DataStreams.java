package org.droidplanner.services.android.communication.model;

import com.o3dr.services.android.lib.model.ICommandListener;

public class DataStreams {

    public interface DataOutputStream<T> {

        void sendMessage(T message, ICommandListener listener);

        boolean isConnected();

        void openConnection();

        void closeConnection();

    }

    public interface DataInputStream<T> {
        void notifyStartingConnection();

        void notifyConnected();

        void notifyDisconnected();

        void notifyReceivedData(T packet);

        void onStreamError(String errorMsg);
    }
}
