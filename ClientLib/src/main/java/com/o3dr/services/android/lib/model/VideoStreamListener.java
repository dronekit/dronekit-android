package com.o3dr.services.android.lib.model;

/**
 * Listens to changes in the video state.
 */
public abstract class VideoStreamListener extends AbstractCommandListener{

    public abstract void onVideoStarted();

    public abstract void onVideoStopped();
}
