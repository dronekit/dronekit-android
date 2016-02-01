package com.o3dr.android.client;

/**
 * Callback for retrieving video packets from VideoStreamObserver.
 */
public interface IVideoStreamCallback {
    void onVideoStreamPacketRecieved(byte[] data, int dataSize);
}
