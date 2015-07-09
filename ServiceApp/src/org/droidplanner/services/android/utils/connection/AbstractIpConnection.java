package org.droidplanner.services.android.utils.connection;

import android.os.Process;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

/**
 * Base class for ip connection (tcp, udp).
 */
public abstract class AbstractIpConnection {

    private static final String TAG = AbstractIpConnection.class.getSimpleName();

    public static final int CONNECTION_TIMEOUT = 15 * 1000; //5 seconds

    /*
    Connection state
     */
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    /**
     * Size of the buffer used to read messages from the connection.
     */
    private static final int DEFAULT_READ_BUFFER_SIZE = 4096;

    private IpConnectionListener ipConnectionListener;

    /**
     * Queue the set of packets to send.
     * A thread will be blocking on it until there's element(s) available to send.
     */
    private final LinkedBlockingQueue<PacketData> packetsToSend = new LinkedBlockingQueue<>();

    private final AtomicInteger connectionStatus = new AtomicInteger(STATE_DISCONNECTED);

    private final boolean isSendingDisabled;
    private final boolean isReadingDisabled;

    private final ByteBuffer readBuffer;

    private final Runnable managerTask = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);

            Thread sendingThread = null;

            try {
                try {
                    open();
                    connectionStatus.set(STATE_CONNECTED);
                    if(ipConnectionListener != null)
                        ipConnectionListener.onIpConnected();
                } catch (IOException e) {
                    Timber.e( "Unable to open ip connection.", e);
                    return;
                }

                if(!isSendingDisabled) {
                    //Launch the packet dispatching thread
                    sendingThread = new Thread(sendingTask, "IP Connection-Sending Thread");
                    sendingThread.start();
                }

                if(!isReadingDisabled) {
                    try {
                        while (connectionStatus.get() == STATE_CONNECTED) {
                            readBuffer.clear();
                            try {
                                int packetSize = read(readBuffer);
                                if (packetSize > 0) {
                                    readBuffer.limit(packetSize);

                                    if (ipConnectionListener != null) {
                                        readBuffer.rewind();
                                        ipConnectionListener.onPacketReceived(readBuffer);
                                    }
                                }
                            }catch(InterruptedIOException e){
                                if(!isPolling)
                                    throw e;
                            }
                        }
                    } catch (IOException e) {
                        Timber.e("Error occurred while reading from the connection.", e);
                    }
                }
                else if(sendingThread != null){
                    try {
                        sendingThread.join();
                    } catch (InterruptedException e) {
                        Timber.e( "Error while waiting for sending thread to complete.", e);
                    }
                }
            }
            finally{
                if(sendingThread != null && sendingThread.isAlive())
                    sendingThread.interrupt();

                disconnect();
                Timber.i( "Exiting connection manager thread.");
            }
        }
    };

    /**
     * Blocks until there's packet(s) to send, then dispatch them.
     */
    private final Runnable sendingTask = new Runnable() {
        @Override
        public void run() {
            try{
                while(connectionStatus.get() == STATE_CONNECTED){
                    final PacketData packetData = packetsToSend.take();

                    try {
                        send(packetData);
                    } catch (IOException e) {
                        Timber.e( "Error occurred while sending packet.", e);
                    }
                }
            } catch (InterruptedException e) {
                Timber.e( "Dispatching thread was interrupted.", e);
            }
            finally{
                disconnect();
                Timber.i( "Exiting packet dispatcher thread.");
            }
        }
    };

    private final boolean isPolling;

    private Thread managerThread;

    public AbstractIpConnection(){
        this(false, false);
    }

    public AbstractIpConnection(int readBufferSize, boolean isPolling){
        this(readBufferSize, false, false, isPolling);
    }

    public AbstractIpConnection(boolean disableSending, boolean disableReading){
        this(DEFAULT_READ_BUFFER_SIZE, disableSending, disableReading, false);
    }

    public AbstractIpConnection(int readBufferSize, boolean disableSending, boolean disableReading, boolean isPolling){
        this.readBuffer = ByteBuffer.allocate(readBufferSize);
        isReadingDisabled = disableReading;
        isSendingDisabled = disableSending;
        this.isPolling = isPolling;
    }

    protected abstract void open() throws IOException;

    protected abstract int read(ByteBuffer buffer) throws IOException;

    protected abstract void send(PacketData data) throws IOException;

    protected abstract void close() throws IOException;

    /**
     * Establish an ip connection. If successful, ConnectionListener#onIpConnected() is called.
     */
    public void connect(){
        if(connectionStatus.compareAndSet(STATE_DISCONNECTED, STATE_CONNECTING)){
            Timber.i( "Starting manager thread.");
            managerThread = new Thread(managerTask, "IP Connection-Manager Thread");
            managerThread.setPriority(Thread.MAX_PRIORITY);
            managerThread.start();
        }
    }

    /**
     * Disconnect an existing ip connection. If successful, ConnectionListener#onIpDisconnected() is called.
     */
    public void disconnect(){
        if(connectionStatus.get() == STATE_DISCONNECTED || managerThread == null)
            return;

        connectionStatus.set(STATE_DISCONNECTED);
        if(managerThread != null && managerThread.isAlive() && !managerThread.isInterrupted()){
            managerThread.interrupt();
        }

        try {
            close();
        } catch (IOException e) {
            Timber.e( "Error occurred while closing ip connection.", e);
        }

        if(ipConnectionListener != null)
            ipConnectionListener.onIpDisconnected();
    }

    public void setIpConnectionListener(IpConnectionListener ipConnectionListener) {
        this.ipConnectionListener = ipConnectionListener;
    }

    public void sendPacket(byte[] packet, int packetSize){
        if(packet == null || packetSize <= 0)
            return;

        packetsToSend.offer(new PacketData(packetSize, packet));
    }

    public int getConnectionStatus(){
        return connectionStatus.get();
    }

    protected static final class PacketData {
        public final int dataLength;
        public final byte[] data;

        public PacketData(int dataLength, byte[] data) {
            this.dataLength = dataLength;
            this.data = data;
        }
    }
}
