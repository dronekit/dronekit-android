package org.droidplanner.services.android.api;

import android.content.Context;
import android.os.RemoteException;

import com.ox3dr.services.android.lib.drone.mission.item.raw.GlobalPositionIntMessage;
import com.ox3dr.services.android.lib.model.ITLogApi;

/**
 * Created by fhuya on 11/9/14.
 */
public class TLogApi extends ITLogApi.Stub {

    private final Context context;

    public TLogApi(Context context){
        this.context = context;
    }

    @Override
    public GlobalPositionIntMessage[] loadGlobalPositionIntMessages(String filename) throws RemoteException {
        return new GlobalPositionIntMessage[0];
    }
}
