package com.o3dr.android.client.apis;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.o3dr.android.client.utils.InstallServiceDialog;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;
import com.o3dr.services.android.lib.util.version.VersionUtils;

/**
 * Helper class to verify that the DroneKit-Android services APK is available and up-to-date
 * Created by Fredia Huya-Kouadio on 7/7/15.
 */
public class ApiAvailability {

    public interface ApiAvailabilityListener {
        /**
         * Dispatch the api availability result
         * @param result
         */
        void onApiAvailabilityResult(int result);
    }

    private static class LazyHolder {
        private static final ApiAvailability INSTANCE = new ApiAvailability();
    }

    private static final String TAG = ApiAvailability.class.getSimpleName();
    private static final String SERVICES_CLAZZ_NAME = IDroidPlannerServices.class.getName();

    public static final int API_CHECK_ERROR = -1;
    public static final int API_AVAILABLE = 0;
    public static final int API_MISSING = 1;
    public static final int API_UPDATE_REQUIRED = 2;

    //Private to prevent instantiation
    private ApiAvailability(){}

    public static ApiAvailability getInstance(){
        return LazyHolder.INSTANCE;
    }

    /**
     * Verifies that DroneKit-Android services is installed and enabled on this device, and that the version
     * installed is up-to-date.
     * @param context Application context. Must not be null.
     * @param handler Used to dispatch the api availability result callback. If null, the result is dispatched on the
     *                calling thread.
     * @param listener Api availability result callback.
     */
    public void checkApiAvailability(@NonNull final Context context, final Handler handler,
                                     final ApiAvailabilityListener listener){
        if(listener == null) {
            //Nobody to reply to, so let's not bother with work.
            return;
        }

        //Check if DroneKit-Android services is installed.
        final Intent serviceIntent = new Intent(SERVICES_CLAZZ_NAME);
        final ResolveInfo serviceInfo = context.getPackageManager().resolveService(serviceIntent, 0);
        if(serviceInfo == null) {
            postCallback(handler, listener, API_MISSING);
            return;
        }

        //Update the package name and class name for the service intent
        serviceIntent.setClassName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);

        //Connect to the service.
        final ServiceConnection apiCheckerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                final IDroidPlannerServices apiServices = IDroidPlannerServices.Stub.asInterface(service);
                try{
                    final int libVersionCode = apiServices.getApiVersionCode();
                    if(libVersionCode < VersionUtils.LIB_VERSION){
                        postCallback(handler, listener, API_UPDATE_REQUIRED);
                    }
                    else{
                        postCallback(handler, listener, API_AVAILABLE);
                    }
                } catch(RemoteException e){
                    Log.e(TAG, "Error occurred while retrieving the api version code.");
                    postCallback(handler, listener, API_CHECK_ERROR);
                }
                finally{
                    try {
                        context.unbindService(this);
                    }catch(Exception e){
                        Log.e(TAG, "Error occurred while unbinding from DroneKit-Android Services.");
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };

        context.bindService(serviceIntent, apiCheckerConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Display a dialog for an error code returned from callback to {@link ApiAvailability#checkApiAvailability(Context, Handler, ApiAvailabilityListener)}
     * @param context Application context
     * @param errorCode Error code returned from callback to
     * {@link ApiAvailability#checkApiAvailability(Context, Handler, ApiAvailabilityListener)}. If errorCode is
     *                  API_AVAILABLE, then this does nothing.
     */
    public void showErrorDialog(Context context, int errorCode){
        switch(errorCode){
            case API_MISSING:
                context.startActivity(new Intent(context, InstallServiceDialog.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(InstallServiceDialog.EXTRA_REQUIREMENT, InstallServiceDialog.REQUIRE_INSTALL));
                break;

            case API_UPDATE_REQUIRED:
                context.startActivity(new Intent(context, InstallServiceDialog.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(InstallServiceDialog.EXTRA_REQUIREMENT, InstallServiceDialog.REQUIRE_UPDATE));
                break;
        }
    }

    private void postCallback(Handler handler, final ApiAvailabilityListener listener, final int result){
        if(listener == null)
            return;

        if(handler == null){
            listener.onApiAvailabilityResult(result);
        }
        else{
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onApiAvailabilityResult(result);
                }
            });
        }
    }

}
