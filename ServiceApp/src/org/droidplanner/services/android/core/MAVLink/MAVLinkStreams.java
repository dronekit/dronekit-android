package org.droidplanner.services.android.core.MAVLink;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.model.ICommandListener;

public class MAVLinkStreams {

	public interface MAVLinkOutputStream {

		void sendMessage(MAVLinkMessage message, ICommandListener listener);

		boolean isConnected();

        void openConnection();

        void closeConnection();

	}

	public interface MavlinkInputStream {
        public void notifyStartingConnection();

		public void notifyConnected();

		public void notifyDisconnected();

		public void notifyReceivedData(MAVLinkPacket packet);

        public void onStreamError(String errorMsg);
	}
}
