package org.droidplanner.services.android.drone.companion.solo;

import android.content.Context;
import android.os.Handler;

import org.droidplanner.services.android.drone.companion.CompComp;
import org.droidplanner.services.android.drone.companion.solo.artoo.ArtooLinkManager;
import org.droidplanner.services.android.drone.companion.solo.sololink.SoloLinkManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sololink companion computer implementation
 * Created by Fredia Huya-Kouadio on 7/9/15.
 */
public class SoloComp implements CompComp {

    public static final String SOLO_LINK_WIFI_PREFIX = "SoloLink_";

    public static final String SSH_USERNAME = "root";
    public static final String SSH_PASSWORD = "TjSDBkAu";

    private final AbstractLinkManager.LinkListener artooLinkListener = new AbstractLinkManager.LinkListener() {
        @Override
        public void onLinkConnected() {
        }

        @Override
        public void onLinkDisconnected() {

        }
    };

    private final AbstractLinkManager.LinkListener soloLinkListener = new AbstractLinkManager.LinkListener() {
        @Override
        public void onLinkConnected() {

        }

        @Override
        public void onLinkDisconnected() {

        }
    };

    private final ArtooLinkManager artooMgr;
    private final SoloLinkManager soloLinkMgr;

    private final Context context;
    private final ExecutorService asyncExecutor;

    /**
     * Solo companion computer implementation
     * @param context Application context
     */
    public SoloComp(Context context, Handler handler){
        this.context = context;

        asyncExecutor = Executors.newCachedThreadPool();

        this.artooMgr = new ArtooLinkManager(context, handler, asyncExecutor);
        this.soloLinkMgr = new SoloLinkManager(context, handler, asyncExecutor);
    }

    public void start(){
        artooMgr.start(artooLinkListener);
        soloLinkMgr.start(soloLinkListener);
    }

    public void stop(){
        artooMgr.stop();
        soloLinkMgr.stop();
    }

    /**
     * Terminates and releases resources used by this companion computer instance. The instance should no longer be used after calling this method.
     */
    public void destroy(){
        asyncExecutor.shutdownNow();
    }

}
