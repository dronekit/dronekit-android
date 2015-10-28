// IVideoStreamObserver.aidl
package com.o3dr.services.android.lib.model;

import com.o3dr.services.android.lib.drone.companion.solo.video.VideoPacket;

oneway interface IVideoStreamObserver {

    /**
    * Notify observer that the specified encoded video packet was received.
    * @param messageWrapper Wrapper for the received raw video data.
    */
    void onVideoPacketReceived(in VideoPacket videoPacket);
}
