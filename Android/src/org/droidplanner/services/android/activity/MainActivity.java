package org.droidplanner.services.android.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.api.DroidPlannerService;
import org.droidplanner.services.android.api.DroneAccess;
import org.droidplanner.services.android.drone.DroneManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 10/31/14.
 */
public class MainActivity extends FragmentActivity {

    private final static IntentFilter intentFilter = new IntentFilter();
    {
        intentFilter.addAction(DroidPlannerService.ACTION_DRONE_CREATED);
        intentFilter.addAction(DroidPlannerService.ACTION_DRONE_DESTROYED);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(DroidPlannerService.ACTION_DRONE_CREATED.equals(action) || DroidPlannerService
                    .ACTION_DRONE_DESTROYED.equals(action)){
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
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();
        droneListAdapter = new DroneInfoAdapter(context);

        titleView = (TextView) findViewById(R.id.drone_infos_title);
        titleView.setText("Connected drones ( 0 )");

        final ListView droneListView = (ListView) findViewById(R.id.drone_info_list);
        droneListView.setAdapter(droneListAdapter);

        lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(broadcastReceiver, intentFilter);

        bindService(new Intent(context, DroidPlannerService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        lbm.unregisterReceiver(broadcastReceiver);
        unbindService(serviceConnection);
    }

    @Override
    public void onStart(){
        super.onStart();
        refreshDroneList();
    }

    private void refreshDroneList(){
        if(droneAccess == null) return;

        List<DroneManager> dronesList = droneAccess.getDroneManagerList();
        droneListAdapter.refreshDroneManagerList(dronesList);

        titleView.setText("Connected drones ( " + dronesList.size() + " )");
    }

    private static class DroneInfoAdapter extends ArrayAdapter<DroneManager> {

        private final LayoutInflater inflater;
        private final List<DroneManager> droneMgrList = new ArrayList<DroneManager>();

        public DroneInfoAdapter(Context context){
            super(context, 0);
            inflater = LayoutInflater.from(context);
        }

        public void refreshDroneManagerList(List<DroneManager> list){
            droneMgrList.clear();

            if(list != null && !list.isEmpty()) {
                droneMgrList.addAll(list);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount(){
            return droneMgrList.size();
        }

        @Override
        public DroneManager getItem(int position){
            return droneMgrList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            final DroneManager droneMgr = getItem(position);
            View view;
            if(convertView == null)
                view = inflater.inflate(R.layout.list_item_drone_info, parent, false);
            else
                view = convertView;

            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if(viewHolder == null){
                viewHolder = new ViewHolder();
                viewHolder.listenersCount = (TextView) view.findViewById(R.id
                        .drone_info_listeners_count);
                viewHolder.infoPanel = (TextView) view.findViewById(R.id
                        .drone_info_connection_params);

                view.setTag(viewHolder);
            }

            viewHolder.listenersCount.setText("Listeners count: " + droneMgr.getListenersCount());
            viewHolder.infoPanel.setText("Connection parameters: " + droneMgr
                    .getConnectionParameter().toString());

            return view;
        }
    }

    private static class ViewHolder {
        TextView listenersCount;
        TextView infoPanel;
    }
}
