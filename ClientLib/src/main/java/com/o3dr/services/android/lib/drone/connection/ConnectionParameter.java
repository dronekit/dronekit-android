package com.o3dr.services.android.lib.drone.connection;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Base type used to pass the drone connection parameters over ipc.
 */
public class ConnectionParameter implements Parcelable {

    private final int connectionType;
    private final Bundle paramsBundle;
    private final DroneSharePrefs droneSharePrefs;

    /**
     * @return Returns a new {@link ConnectionParameter} with type {@link ConnectionType#TYPE_USB}
     * and baud rate {@link ConnectionType#DEFAULT_USB_BAUD_RATE}.
     */
    public static ConnectionParameter newUsbConnection() {
        return newUsbConnection(ConnectionType.DEFAULT_USB_BAUD_RATE);
    }

    /**
     *
     * @param usbBaudRate Baud rate for USB connection.
     *
     * @return Returns a new {@link ConnectionParameter} with type {@link ConnectionType#TYPE_USB}.
     */
    public static ConnectionParameter newUsbConnection(int usbBaudRate) {
        Bundle paramsBundle = new Bundle(1);
        paramsBundle.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, usbBaudRate);

        return new ConnectionParameter(ConnectionType.TYPE_USB, paramsBundle);
    }

    /**
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}, using
     * {@link ConnectionType#DEFAULT_UDP_SERVER_PORT} port.
     */
    public static ConnectionParameter newUdpConnection() {
        return newUdpConnection(ConnectionType.DEFAULT_UDP_SERVER_PORT);
    }

    /**
     *
     * @param udpPort Port for the UDP connection.
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}.
     */
    public static ConnectionParameter newUdpConnection(int udpPort) {
        return newUdpConnection(udpPort, null, 0, null);
    }

    /**
     *
     * @param udpPort Port for the UDP connection.
     * @param udpPingReceiverIp IP address of the UDP server to ping. If this value is null, it is ignored
     *                          along with udpPingReceiverPort and udpPingPayload.
     * @param udpPingReceiverPort Port of the UDP server to ping.
     * @param udpPingPayload Ping payload.
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}. The ping
     * period is set to {@link ConnectionType#DEFAULT_UDP_PING_PERIOD}
     */
    public static ConnectionParameter newUdpConnection(int udpPort, @Nullable String udpPingReceiverIp, int udpPingReceiverPort,
                                                       byte[] udpPingPayload) {
        return newUdpConnection(udpPort, udpPingReceiverIp, udpPingReceiverPort, udpPingPayload, ConnectionType.DEFAULT_UDP_PING_PERIOD);
    }

    /**
     *
     * @param udpPort Port for the UDP connection.
     * @param udpPingReceiverIp IP address of the UDP server to ping. If this value is null, it is ignored
     *                          along with udpPingReceiverPort, udpPingPayload, and pingPeriod.
     * @param udpPingReceiverPort Port of the UDP server to ping.
     * @param udpPingPayload Ping payload.
     * @param pingPeriod How often should the udp ping be performed.
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_UDP}.
     */
    public static ConnectionParameter newUdpConnection(int udpPort, @Nullable String udpPingReceiverIp, int udpPingReceiverPort,
                                                       byte[] udpPingPayload, long pingPeriod) {
        Bundle paramsBundle = new Bundle();
        paramsBundle.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, udpPort);

        if (!TextUtils.isEmpty(udpPingReceiverIp)) {
            paramsBundle.putString(ConnectionType.EXTRA_UDP_PING_RECEIVER_IP, udpPingReceiverIp);
            paramsBundle.putInt(ConnectionType.EXTRA_UDP_PING_RECEIVER_PORT, udpPingReceiverPort);
            paramsBundle.putByteArray(ConnectionType.EXTRA_UDP_PING_PAYLOAD, udpPingPayload);
            paramsBundle.putLong(ConnectionType.EXTRA_UDP_PING_PERIOD, pingPeriod);
        }

        return new ConnectionParameter(ConnectionType.TYPE_UDP, paramsBundle);
    }

    /**
     *
     * @param tcpServerIp TCP server IP address.
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_TCP}, using
     * {@link ConnectionType#DEFAULT_TCP_SERVER_PORT}.
     */
    public static ConnectionParameter newTcpConnection(String tcpServerIp) {
        return newTcpConnection(tcpServerIp, ConnectionType.DEFAULT_TCP_SERVER_PORT);
    }

    /**
     *
     * @param tcpServerIp TCP server IP address.
     * @param tcpServerPort TCP server port.
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_TCP}.
     */
    public static ConnectionParameter newTcpConnection(String tcpServerIp, int tcpServerPort) {
        Bundle paramsBundle = new Bundle(2);
        paramsBundle.putString(ConnectionType.EXTRA_TCP_SERVER_IP, tcpServerIp);
        paramsBundle.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, tcpServerPort);

        return new ConnectionParameter(ConnectionType.TYPE_TCP, paramsBundle);
    }

    /**
     *
     * @param bluetoothAddress Bluetooth address.
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_BLUETOOTH}.
     */
    public static ConnectionParameter newBluetoothConnection(String bluetoothAddress) {
        Bundle paramsBundle = new Bundle(1);
        paramsBundle.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, bluetoothAddress);

        return new ConnectionParameter(ConnectionType.TYPE_BLUETOOTH, paramsBundle);
    }

    /**
     *
     * @param ssid Wifi SSID of the solo vehicle link. This will remove a leading and/or trailing quotation.
     * @param password Password to access the solo wifi network. This value can be null as long as the wifi
     *                 configuration has been set up and stored in the mobile device's system.
     *
     * @return Returns {@link ConnectionParameter} with type {@link ConnectionType#TYPE_SOLO}.
     */
    public static ConnectionParameter newSoloConnection(String ssid, @Nullable String password) {
        String ssidWithoutQuotes = ssid.replaceAll("^\"|\"$", "");

        Bundle paramsBundle = new Bundle(2);
        paramsBundle.putString(ConnectionType.EXTRA_SOLO_LINK_ID, ssidWithoutQuotes);
        paramsBundle.putString(ConnectionType.EXTRA_SOLO_LINK_PASSWORD, password);

        return new ConnectionParameter(ConnectionType.TYPE_SOLO, paramsBundle);
    }

    /**
     * @deprecated Use one of specified static methods
     */
    //TODO: make this private version 3.0
    public ConnectionParameter(int connectionType, Bundle paramsBundle){
        this.connectionType = connectionType;
        this.paramsBundle = paramsBundle;
        this.droneSharePrefs = null;
    }

    /**
     * @deprecated Use {@link ConnectionParameter#ConnectionParameter(int, Bundle)} instead.
     * @since 2.8.0
     */
    public ConnectionParameter(int connectionType, Bundle paramsBundle, DroneSharePrefs droneSharePrefs){
        this.connectionType = connectionType;
        this.paramsBundle = paramsBundle;
        this.droneSharePrefs = droneSharePrefs;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public Bundle getParamsBundle() {
        return paramsBundle;
    }

    public DroneSharePrefs getDroneSharePrefs() {
        return droneSharePrefs;
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

            default:
                uniqueId = "";
                break;
        }

        return uniqueId;
    }

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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.connectionType);
        dest.writeBundle(paramsBundle);
        dest.writeParcelable(this.droneSharePrefs, 0);
    }

    private ConnectionParameter(Parcel in) {
        this.connectionType = in.readInt();
        paramsBundle = in.readBundle();
        this.droneSharePrefs = in.readParcelable(DroneSharePrefs.class.getClassLoader());
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