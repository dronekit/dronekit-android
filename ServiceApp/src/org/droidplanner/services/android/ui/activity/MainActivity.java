package org.droidplanner.services.android.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.api.DroidPlannerService;
import org.droidplanner.services.android.api.DroneAccess;
import org.droidplanner.services.android.ui.fragment.AppConnectionsFragment;

/**
 * Created by fhuya on 10/31/14.
 */
public class MainActivity extends ActionBarActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            droneAccess = (DroneAccess) service;
            showOrRefreshFragments();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            droneAccess = null;
        }
    };

    private void showOrRefreshFragments() {
        final FragmentManager fm = getSupportFragmentManager();
        AppConnectionsFragment fragment = (AppConnectionsFragment) fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new AppConnectionsFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        } else {
            fragment.refreshDroneList();
        }
    }

    private DroneAccess droneAccess;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();

        try {
            final TextView versionInfo = (TextView) findViewById(R.id.version_info);
            versionInfo.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to retrieve the version name.", e);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                //TODO: Launch the selected fragment.
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //TODO: update the app title
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

//        ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null){
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//        }
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

        if (droneAccess != null)
            showOrRefreshFragments();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (drawerToggle != null)
            drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (drawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            drawerToggle.syncState();
        }
    }

    public DroneAccess getDroneAccess() {
        return droneAccess;
    }
}
