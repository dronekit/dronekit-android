package org.droidplanner.services.android.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.api.DroidPlannerService;
import org.droidplanner.services.android.api.DroneAccess;
import org.droidplanner.services.android.ui.fragment.ViewCategoryFragment;
import org.droidplanner.services.android.utils.Utils;

/**
 * User interface for the 3DR Services app.
 */
public class MainActivity extends ActionBarActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String ACTION_SERVICE_CONNECTED = Utils.PACKAGE_NAME + ".action.SERVICE_CONNECTED";

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            droneAccess = (DroneAccess) service;
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent
                    (ACTION_SERVICE_CONNECTED));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            droneAccess = null;
        }
    };

    private DroneAccess droneAccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            final TextView versionInfo = (TextView) findViewById(R.id.version_info);
            versionInfo.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to retrieve the version name.", e);
        }

        if (savedInstanceState == null) {
            final FragmentManager fm = getSupportFragmentManager();
            ViewCategoryFragment categoryViewFragment = new ViewCategoryFragment();
            fm.beginTransaction().add(R.id.fragment_container, categoryViewFragment).commit();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindService(new Intent(getApplicationContext(), DroidPlannerService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);

        if (droneAccess != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent
                    (ACTION_SERVICE_CONNECTED));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_learn_more:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://dronekit.io/")));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public DroneAccess getDroneAccess() {
        return droneAccess;
    }
}
