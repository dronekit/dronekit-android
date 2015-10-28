package com.o3dr.android.client;

import com.o3dr.services.android.lib.drone.companion.solo.video.VideoPacket;
import com.o3dr.services.android.lib.model.IVideoStreamObserver;

public abstract class VideoStreamObserver extends IVideoStreamObserver.Stub
{
    @Override
    public abstract void onVideoPacketReceived(VideoPacket videoPacket);
}
