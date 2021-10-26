package com.o3dr.services.android.lib.drone.connection;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.droidplanner.services.android.impl.communication.connection.AndroidMavLinkConnection;

import timber.log.Timber;

/**
 * Base type used to pass the drone connection parameters over ipc.
 */
public class ConnectionParameter implements Parcelable {

    private final @ConnectionType.Type int connectionType;
    private final Bundle paramsBundle;
    private final Uri tlogLoggingUri;
    private final AndroidMavLinkConnection customConnection;

    /**
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     * @return Returns a new {@link ConnectionParameter} with type {@link ConnectionType#TYPE_USB}
     * and baud rate {@link ConnectionType#DEFAULT_USB_BAUD_RATE}.
     */
    public static ConnectionParameter newUsbConnection(@Nullable Uri tlogLoggingUri) {
        return newUsbConnection(ConnectionType.DEFAULT_USB_BAUD_RATE, tlogLoggingUri);
    }
    /**
     *
     * @param usbBaudRate Baud rate for USB connection.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     * @return Returns a new {@link ConnectionParameter} with type {@link ConnectionType#TYPE_USB}.
     */
    public static ConnectionParameter newUsbConnection(int usbBaudRate, @Nullable Uri tlogLoggingUri) {
        Bundle paramsBundle = new Bundle(1);
        paramsBundle.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, usbBaudRate);

        return new ConnectionParameter(ConnectionType.TYPE_USB, paramsBundle, tlogLoggingUri, null);
    }

    /**
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}, using
     * {@link ConnectionType#DEFAULT_UDP_SERVER_PORT} port.
     */
    public static ConnectionParameter newUdpConnection(@Nullable Uri tlogLoggingUri) {
        return newUdpConnection(ConnectionType.DEFAULT_UDP_SERVER_PORT, tlogLoggingUri);
    }

    /**
     *
     * @param udpPort Port for the UDP connection.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}.
     */
    public static ConnectionParameter newUdpConnection(int udpPort, @Nullable Uri tlogLoggingUri) {
        return newUdpConnection(null, udpPort, null, 0, null, tlogLoggingUri);
    }

    /**
     * @param udpIP IP addresss for the UDP connection.
     * @param udpPort Port for the UDP connection.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}.
     */
    public static ConnectionParameter newUdpConnection(String udpIP, int udpPort, @Nullable Uri tlogLoggingUri) {
        return newUdpConnection(udpIP, udpPort, null, 0, null, tlogLoggingUri);
    }

    /**
     *
     * @param udpIP IP address for the UDP connection.
     * @param udpPort Port for the UDP connection.
     * @param udpPingReceiverIp IP address of the UDP server to ping. If this value is null, it is ignored
     *                          along with udpPingReceiverPort and udpPingPayload.
     * @param udpPingReceiverPort Port of the UDP server to ping.
     * @param udpPingPayload Ping payload.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}. The ping
     * period is set to {@link ConnectionType#DEFAULT_UDP_PING_PERIOD}
     */
    public static ConnectionParameter newUdpConnection(String udpIP, int udpPort, @Nullable String udpPingReceiverIp, int udpPingReceiverPort,
                                                       byte[] udpPingPayload, @Nullable Uri tlogLoggingUri) {
        return newUdpConnection(udpIP, udpPort, udpPingReceiverIp, udpPingReceiverPort, udpPingPayload, ConnectionType.DEFAULT_UDP_PING_PERIOD, tlogLoggingUri);
    }

    /**
     *
     * @param udpIP IP address for the UDP connection.
     * @param udpPort Port for the UDP connection.
     * @param udpPingReceiverIp IP address of the UDP server to ping. If this value is null, it is ignored
     *                          along with udpPingReceiverPort, udpPingPayload, and pingPeriod.
     * @param udpPingReceiverPort Port of the UDP server to ping.
     * @param udpPingPayload Ping payload.
     * @param pingPeriod How often should the udp ping be performed.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}.
     */
    public static ConnectionParameter newUdpConnection(String udpIP, int udpPort, @Nullable String udpPingReceiverIp, int udpPingReceiverPort,
                                                       byte[] udpPingPayload, long pingPeriod, @Nullable Uri tlogLoggingUri) {
        Bundle paramsBundle = new Bundle();
        paramsBundle.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, udpPort);

        Timber.d("newUdpConnection(%s, %d, %s, %d, %s, %d, %s)",
                udpIP, udpPort, udpPingReceiverIp, udpPingReceiverPort, udpPingPayload, pingPeriod, tlogLoggingUri);

        if(!TextUtils.isEmpty(udpIP)) {
            paramsBundle.putString(ConnectionType.EXTRA_UDP_SERVER_IP, udpIP);
        }

        if (!TextUtils.isEmpty(udpPingReceiverIp)) {
            paramsBundle.putString(ConnectionType.EXTRA_UDP_PING_RECEIVER_IP, udpPingReceiverIp);
            paramsBundle.putInt(ConnectionType.EXTRA_UDP_PING_RECEIVER_PORT, udpPingReceiverPort);
            paramsBundle.putByteArray(ConnectionType.EXTRA_UDP_PING_PAYLOAD, udpPingPayload);
            paramsBundle.putLong(ConnectionType.EXTRA_UDP_PING_PERIOD, pingPeriod);
        }

        return new ConnectionParameter(ConnectionType.TYPE_UDP, paramsBundle, tlogLoggingUri, null);
    }

    /**
     *
     * @param tcpServerIp TCP server IP address.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_TCP}, using
     * {@link ConnectionType#DEFAULT_TCP_SERVER_PORT}.
     */
    public static ConnectionParameter newTcpConnection(String tcpServerIp, @Nullable Uri tlogLoggingUri) {
        return newTcpConnection(tcpServerIp, ConnectionType.DEFAULT_TCP_SERVER_PORT, tlogLoggingUri);
    }

    /**
     *
     * @param tcpServerIp TCP server IP address.
     * @param tcpServerPort TCP server port.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_TCP}.
     */
    public static ConnectionParameter newTcpConnection(String tcpServerIp, int tcpServerPort, @Nullable Uri tlogLoggingUri) {
        Bundle paramsBundle = new Bundle(2);
        paramsBundle.putString(ConnectionType.EXTRA_TCP_SERVER_IP, tcpServerIp);
        paramsBundle.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, tcpServerPort);

        return new ConnectionParameter(ConnectionType.TYPE_TCP, paramsBundle, tlogLoggingUri, null);
    }

    /**
     *
     * @param bluetoothAddress Bluetooth address.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_BLUETOOTH}.
     */
    public static ConnectionParameter newBluetoothConnection(String bluetoothAddress, @Nullable Uri tlogLoggingUri){
        Bundle paramsBundle = new Bundle(1);
        paramsBundle.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, bluetoothAddress);

        return new ConnectionParameter(ConnectionType.TYPE_BLUETOOTH, paramsBundle, tlogLoggingUri, null);
    }

    public static ConnectionParameter newCustomConnection(AndroidMavLinkConnection connection, String connectionId) {
        Bundle paramsBundle = new Bundle(1);
        paramsBundle.putString(ConnectionType.EXTRA_CUSTOM_CONNECTION_ID, connectionId);

        return new ConnectionParameter(ConnectionType.TYPE_CUSTOM, paramsBundle, null, connection);
    }

    public static ConnectionParameter newCustomConnection(AndroidMavLinkConnection connection, String connectionId, @Nullable Uri tlogUri) {
        Bundle paramsBundle = new Bundle(1);
        paramsBundle.putString(ConnectionType.EXTRA_CUSTOM_CONNECTION_ID, connectionId);

        return new ConnectionParameter(ConnectionType.TYPE_CUSTOM, paramsBundle, tlogUri, connection);
    }

    /**
     *
     * @param ssid Wifi SSID of the solo vehicle link. This will remove a leading and/or trailing quotation.
     * @param password Password to access the solo wifi network. This value can be null as long as the wifi
     *                 configuration has been set up and stored in the mobile device's system.
     * @param tlogLoggingUri Uri where the tlog data should be logged. Pass null if the tlog data shouldn't be logged.
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_SOLO}.
     */
    public static ConnectionParameter newSoloConnection(String ssid, @Nullable String password, @Nullable Uri tlogLoggingUri){
        String ssidWithoutQuotes = ssid.replaceAll("^\"|\"$", "");

        Bundle paramsBundle = new Bundle(2);
        paramsBundle.putString(ConnectionType.EXTRA_SOLO_LINK_ID, ssidWithoutQuotes);
        paramsBundle.putString(ConnectionType.EXTRA_SOLO_LINK_PASSWORD, password);

        return new ConnectionParameter(ConnectionType.TYPE_SOLO, paramsBundle, tlogLoggingUri, null);
    }

    public static ConnectionParameter newSoloConnection(String ssid, @Nullable String password, ConnectionParameter params) {
        String ssidWithoutQuotes = ssid.replaceAll("^\"|\"$", "");

        Bundle paramsBundle = new Bundle(params.getParamsBundle());
        paramsBundle.putString(ConnectionType.EXTRA_SOLO_LINK_ID, ssidWithoutQuotes);
        paramsBundle.putString(ConnectionType.EXTRA_SOLO_LINK_PASSWORD, password);

        return new ConnectionParameter(ConnectionType.TYPE_SOLO, paramsBundle, params.getTLogLoggingUri(), null);
    }

    private ConnectionParameter(@ConnectionType.Type int connectionType, Bundle paramsBundle){
        this(connectionType, paramsBundle, null, null);
    }

    /**
     */
    private ConnectionParameter(@ConnectionType.Type int connectionType, Bundle paramsBundle, Uri tlogLoggingUri, AndroidMavLinkConnection customConnection) {
        this.connectionType = connectionType;
        this.paramsBundle = paramsBundle;
        this.tlogLoggingUri = tlogLoggingUri;
        this.customConnection = customConnection;
    }

    public @ConnectionType.Type int getConnectionType() {
        return connectionType;
    }

    public Bundle getParamsBundle() {
        return paramsBundle;
    }

    /**
     * Return the uri where the tlog data should be logged.
     * @return Uri where to log the tlog data, or null if it shouldn't be logged.
     */
    public Uri getTLogLoggingUri(){
        return tlogLoggingUri;
    }

    public String getUniqueId(){
        final String uniqueId;
        switch(connectionType){

            case ConnectionType.TYPE_UDP:
                int udpPort = ConnectionType.DEFAULT_UDP_SERVER_PORT;
                if(paramsBundle != null){
                    udpPort = paramsBundle.getInt(ConnectionType.EXTRA_UDP_SERVER_PORT, udpPort);
                }
                uniqueId = "udp:" + udpPort;
                break;

            case ConnectionType.TYPE_BLUETOOTH:
                String btAddress = "";
                if(paramsBundle != null){
                    btAddress = paramsBundle.getString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, "");
                }

                uniqueId = TextUtils.isEmpty(btAddress) ? "bluetooth" : "bluetooth:" + btAddress;
                break;

            case ConnectionType.TYPE_TCP:
                String tcpIp = "";
                int tcpPort = ConnectionType.DEFAULT_TCP_SERVER_PORT;
                if(paramsBundle != null){
                    tcpIp = paramsBundle.getString(ConnectionType.EXTRA_TCP_SERVER_IP, "");
                    tcpPort = paramsBundle.getInt(ConnectionType.EXTRA_TCP_SERVER_PORT, tcpPort);
                }

                uniqueId = "tcp"  + ":" + tcpIp + ":" + tcpPort;
                break;

            case ConnectionType.TYPE_USB:
                uniqueId = "usb";
                break;

            case ConnectionType.TYPE_SOLO:
                String soloLinkId = "";
                if(paramsBundle != null){
                    soloLinkId = paramsBundle.getString(ConnectionType.EXTRA_SOLO_LINK_ID, "");
                }
                uniqueId = "solo:" + soloLinkId;
                break;

            case ConnectionType.TYPE_CUSTOM:
                uniqueId = "custom: " + paramsBundle.getString(ConnectionType.EXTRA_CUSTOM_CONNECTION_ID, "");
                break;

            default:
                uniqueId = "";
                break;
        }

        return uniqueId;
    }

    public AndroidMavLinkConnection getCustomConnection() { return customConnection; }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof ConnectionParameter)) return false;

        ConnectionParameter that = (ConnectionParameter) o;
        return getUniqueId().equals(that.getUniqueId());
    }

    @Override
    public int hashCode(){
        return getUniqueId().hashCode();
    }

    @Override
    public String toString() {
        String toString = "ConnectionParameter{" +
            "connectionType=" + connectionType +
            ", paramsBundle=[";

        if (paramsBundle != null && !paramsBundle.isEmpty()) {
            boolean isFirst = true;
            for (String key : paramsBundle.keySet()) {
                if (isFirst)
                    isFirst = false;
                else
                    toString += ", ";

                toString += key + "=" + paramsBundle.get(key);
            }
        }

        toString += "]}";
        return toString;
    }

    @Override
    public ConnectionParameter clone(){
        return new ConnectionParameter(this.connectionType, this.paramsBundle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.connectionType);
        dest.writeBundle(paramsBundle);
        dest.writeParcelable(tlogLoggingUri, flags);
    }

    private ConnectionParameter(Parcel in) {
        @ConnectionType.Type int type = in.readInt();
        this.connectionType = type;
        paramsBundle = in.readBundle(getClass().getClassLoader());
        tlogLoggingUri = in.readParcelable(Uri.class.getClassLoader());
        customConnection = null;
    }

    public static final Creator<ConnectionParameter> CREATOR = new Creator<ConnectionParameter>() {
        public ConnectionParameter createFromParcel(Parcel source) {
            return new ConnectionParameter(source);
        }

        public ConnectionParameter[] newArray(int size) {
            return new ConnectionParameter[size];
        }
    };
}