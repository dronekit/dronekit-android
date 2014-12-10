package org.droidplanner.services.android.ui.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.connection.StreamRates;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.api.DroneApi;

import java.util.ArrayList;
import java.util.List;

/**
* Created by fhuya on 12/10/14.
*/
public class DroneInfoAdapter extends ArrayAdapter<DroneApi> {

    private static class ViewHolder {
        ImageView clientIcon;
        TextView clientId;
        TextView clientConnectionInfo;
    }

    private final LayoutInflater inflater;
    private final List<DroneApi> droneApiList = new ArrayList<DroneApi>();

    public DroneInfoAdapter(Context context) {
        super(context, 0);
        inflater = LayoutInflater.from(context);
    }

    public void refreshDroneManagerList(List<DroneApi> list) {
        droneApiList.clear();

        if (list != null && !list.isEmpty()) {
            droneApiList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return droneApiList.size();
    }

    @Override
    public DroneApi getItem(int position) {
        return droneApiList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DroneApi droneApi = getItem(position);
        View view;
        if (convertView == null)
            view = inflater.inflate(R.layout.list_item_drone_info, parent, false);
        else
            view = convertView;

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.clientIcon = (ImageView) view.findViewById(R.id.client_icon);
            viewHolder.clientId = (TextView) view.findViewById(R.id.client_id);
            viewHolder.clientConnectionInfo = (TextView) view.findViewById(R.id.client_connection_info);

            view.setTag(viewHolder);
        }

        final String ownerId = droneApi.getOwnerId();
        if(!TextUtils.isEmpty(ownerId)) {
            try {
            final PackageManager pm = getContext().getPackageManager();
                final ApplicationInfo appInfo = pm.getApplicationInfo(ownerId, 0);
                Drawable appIcon = pm.getApplicationIcon(appInfo);
                viewHolder.clientIcon.setVisibility(View.VISIBLE);
                viewHolder.clientIcon.setImageDrawable(appIcon);

                CharSequence appLabel = pm.getApplicationLabel(appInfo);
                viewHolder.clientId.setText(appLabel + " ( " + ownerId + " ) ");
            } catch (PackageManager.NameNotFoundException e) {
                viewHolder.clientIcon.setVisibility(View.GONE);
                viewHolder.clientId.setText(ownerId);
            }
        }
        else{
            viewHolder.clientIcon.setVisibility(View.GONE);
            viewHolder.clientId.setText("--");
        }

        ConnectionParameter connParams = droneApi.getDroneManager().getConnectionParameter();
        if(connParams == null){
            viewHolder.clientConnectionInfo.setVisibility(View.GONE);
        }
        else {
            DroneSharePrefs droneSharePrefs = connParams.getDroneSharePrefs();
            StreamRates rates = connParams.getStreamRates();

            final String connectionInfo = "Connection parameters: " + connParams + "\n\n"
                    + "Vehicle stream rates: " + rates + "\n\n"
                    + "Drone Share Info: " + droneSharePrefs;

            viewHolder.clientConnectionInfo.setText(connectionInfo);
            viewHolder.clientConnectionInfo.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
