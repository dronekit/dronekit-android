package org.droidplanner.core.MAVLink;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;

public class MAVLinkStreams {

	public interface MAVLinkOutputStream {

		void sendMavPacket(MAVLinkMessage message, int sysId, int compId);

		void sendMavPacket(MAVLinkMessage message);

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
