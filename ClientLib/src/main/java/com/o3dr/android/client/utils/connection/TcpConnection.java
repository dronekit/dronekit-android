package com.o3dr.android.client.utils.connection;

import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 2/18/15.
 */
public class TcpConnection extends AbstractIpConnection {

    private final String serverIp;
    private final int serverPort;

    private Socket socket;
    private BufferedOutputStream connOut;
    private BufferedInputStream connIn;

    public TcpConnection(Handler handler, String serverIp, int serverPort){
        super(handler);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    @Override
    protected void open() throws IOException {
        final InetAddress serverAddr = InetAddress.getByName(serverIp);
        socket = new Socket();
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress(serverAddr, serverPort), CONNECTION_TIMEOUT);
        connOut = new BufferedOutputStream(socket.getOutputStream());
        connIn = new BufferedInputStream(socket.getInputStream());
    }

    @Override
    protected int read(ByteBuffer buffer) throws IOException {
        return connIn.read(buffer.array());
    }

    @Override
    protected void send(PacketData data) throws IOException {
        connOut.write(data.data, 0, data.dataLength);
        connOut.flush();
    }

    @Override
    protected void close() throws IOException {
        if(socket != null)
            socket.close();
    }
}
