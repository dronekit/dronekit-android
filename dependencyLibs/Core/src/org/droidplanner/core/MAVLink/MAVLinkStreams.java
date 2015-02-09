package org.droidplanner.core.MAVLink;

import com.MAVLink.MAVLinkPacket;

public class MAVLinkStreams {

	public interface MAVLinkOutputStream {

		void sendMavPacket(MAVLinkPacket pack);

		boolean isConnected();

		void toggleConnectionState();

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
