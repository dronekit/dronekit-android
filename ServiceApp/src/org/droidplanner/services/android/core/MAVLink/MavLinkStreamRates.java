package org.droidplanner.services.android.core.MAVLink;

import org.droidplanner.services.android.communication.model.DataLink.DataLinkProvider;

import com.MAVLink.common.msg_request_data_stream;
import com.MAVLink.enums.MAV_DATA_STREAM;

public class MavLinkStreamRates {

	public static void setupStreamRates(DataLinkProvider MAVClient, byte sysid, byte compid,
			int extendedStatus, int extra1, int extra2, int extra3, int position, int rcChannels,
			int rawSensors, int rawControler) {
		requestMavlinkDataStream(MAVClient, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS,
				extendedStatus);
		requestMavlinkDataStream(MAVClient, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1, extra1);
		requestMavlinkDataStream(MAVClient, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2, extra2);
		requestMavlinkDataStream(MAVClient, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3, extra3);
		requestMavlinkDataStream(MAVClient, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, position);
		requestMavlinkDataStream(MAVClient, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, rawSensors);
		requestMavlinkDataStream(MAVClient, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_CONTROLLER,
				rawControler);
		requestMavlinkDataStream(MAVClient, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS, rcChannels);
	}

	private static void requestMavlinkDataStream(DataLinkProvider mAVClient, byte sysid,
			byte compid, int stream_id, int rate) {
		msg_request_data_stream msg = new msg_request_data_stream();
		msg.target_system = sysid;
		msg.target_component = compid;

		msg.req_message_rate = rate;
		msg.req_stream_id = (short) stream_id;

		if (rate > 0) {
			msg.start_stop = 1;
		} else {
			msg.start_stop = 0;
		}
		mAVClient.sendMessage(msg, null);
	}
}
