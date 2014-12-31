package org.droidplanner.core.MAVLink.connection;

/**
 * List the supported mavlink connection types.
 */
public class MavLinkConnectionTypes {

	/**
	 * Bluetooth mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_BLUETOOTH = 0;

	/**
	 * USP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_USB = 1;

	/**
	 * UDP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_UDP = 2;

	/**
	 * TCP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_TCP = 3;

    public static String getConnectionTypeLabel(int connectionType){
        switch(connectionType){
            case MavLinkConnectionTypes.MAVLINK_CONNECTION_BLUETOOTH:
                return "bluetooth";

            case MavLinkConnectionTypes.MAVLINK_CONNECTION_TCP:
                return "tcp";

            case MavLinkConnectionTypes.MAVLINK_CONNECTION_UDP:
                return "udp";

            case MavLinkConnectionTypes.MAVLINK_CONNECTION_USB:
                return "usb";

            default:
                return null;
        }
    }

	// Not instantiable
	private MavLinkConnectionTypes() {
	}
}
