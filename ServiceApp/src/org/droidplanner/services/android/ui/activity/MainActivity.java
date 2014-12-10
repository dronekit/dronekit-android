package org.droidplanner.services.android.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.api.DroidPlannerService;
import org.droidplanner.services.android.api.DroneAccess;
import org.droidplanner.services.android.api.DroneApi;
import org.droidplanner.services.android.ui.adapter.DroneInfoAdapter;

import java.util.List;

/**
 * Created by fhuya on 10/31/14.
 */
public class MainActivity extends FragmentActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static IntentFilter intentFilter = new IntentFilter();

    {
        intentFilter.addAction(DroidPlannerService.ACTION_DRONE_CREATED);
        intentFilter.addAction(DroidPlannerService.ACTION_DRONE_DESTROYED);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DroidPlannerService.ACTION_DRONE_CREATED.equals(action) || DroidPlannerService
                    .ACTION_DRONE_DESTROYED.equals(action)) {
                refreshDroneList();
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            droneAccess = (DroneAccess) service;
            refreshDroneList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            droneAccess = null;
        }
    };

    private LocalBroadcastManager lbm;
    private DroneAccess droneAccess;

    private TextView titleView;
    private DroneInfoAdapter droneListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();
        droneListAdapter = new DroneInfoAdapter(context);

        try {
        final TextView versionInfo = (TextView) findViewById(R.id.version_info);
            versionInfo.setText("Version " +
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to retrieve the application version.");
        }

        titleView = (TextView) findViewById(R.id.drone_infos_title);
        titleView.setText("Connected Clients");

        final ListView droneListView = (ListView) findViewById(R.id.drone_info_list);
        droneListView.setAdapter(droneListAdapter);

        lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(broadcastReceiver, intentFilter);

        bindService(new Intent(context, DroidPlannerService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lbm.unregisterReceiver(broadcastReceiver);
        unbindService(serviceConnection);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshDroneList();
    }

    private void refreshDroneList() {
        if (droneAccess == null) return;

        List<DroneApi> dronesList = droneAccess.getDroneApiList();
        droneListAdapter.refreshDroneManagerList(dronesList);

        titleView.setText("Connected Clients");
    }
}
