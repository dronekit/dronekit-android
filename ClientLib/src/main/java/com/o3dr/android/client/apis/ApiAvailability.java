package com.o3dr.android.client.apis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.o3dr.android.client.utils.InstallServiceDialog;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;
import com.o3dr.services.android.lib.util.version.VersionUtils;

/**
 * Helper class to verify that the DroneKit-Android services APK is available and up-to-date
 * Created by Fredia Huya-Kouadio on 7/7/15.
 */
public class ApiAvailability {

    private static class LazyHolder {
        private static final ApiAvailability INSTANCE = new ApiAvailability();
    }

    private static final String TAG = ApiAvailability.class.getSimpleName();

    private static final String SERVICES_CLAZZ_NAME = IDroidPlannerServices.class.getName();
    private static final String METADATA_KEY = "com.o3dr.dronekit.android.core.version";

    public static final int API_AVAILABLE = 0;
    public static final int API_MISSING = 1;
    public static final int API_UPDATE_REQUIRED = 2;

    //Private to prevent instantiation
    private ApiAvailability() {
    }

    public static ApiAvailability getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Verifies that DroneKit-Android services is installed and enabled on this device, and that the version
     * installed is up-to-date.
     *
     * @param context Application context. Must not be null.
     * @return status code indicating the availability of the api.
     */
    public int checkApiAvailability(@NonNull final Context context) {
        final PackageManager pm = context.getPackageManager();

        //Check if DroneKit-Android services is installed.
        final Intent serviceIntent = new Intent(SERVICES_CLAZZ_NAME);
        final ResolveInfo serviceInfo = pm.resolveService(serviceIntent, PackageManager.GET_META_DATA);
        if (serviceInfo == null) {
            return API_MISSING;
        }

        final Bundle metaData = serviceInfo.serviceInfo.metaData;
        if (metaData == null)
            return API_UPDATE_REQUIRED;

        final int coreLibVersion = metaData.getInt(METADATA_KEY);
        if (coreLibVersion < VersionUtils.getCoreLibVersion(context))
            return API_UPDATE_REQUIRED;
        else
            return API_AVAILABLE;
    }

    /**
     * Display a dialog for an error code returned from callback to {@link ApiAvailability#checkApiAvailability(Context)}
     *
     * @param context   Application context
     * @param errorCode Error code returned from callback to
     *                  {@link ApiAvailability#checkApiAvailability(Context)}. If errorCode is API_AVAILABLE, then this does nothing.
     */
    public void showErrorDialog(Context context, int errorCode) {
        switch (errorCode) {
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

}
