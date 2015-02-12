package org.droidplanner.services.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.api.DroneApi;
import com.o3dr.services.android.lib.util.SpannableUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 12/10/14.
 */
public class AppConnectionAdapter extends RecyclerView.Adapter<AppConnectionAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View containerView;
        final ImageView clientIcon;
        final TextView clientId;
        final TextView clientConnectionInfo;
        final TextView clientName;

        public ViewHolder(View container, ImageView clientIcon, TextView clientName, TextView clientId,
                          TextView clientConnectionState) {
            super(container);
            this.containerView = container;
            this.clientIcon = clientIcon;
            this.clientId = clientId;
            this.clientConnectionInfo = clientConnectionState;
            this.clientName = clientName;
        }
    }

    private final Context context;
    private final List<DroneApi> droneApiList = new ArrayList<DroneApi>();

    public AppConnectionAdapter(Context context) {
        this.context = context;
    }

    public void refreshDroneManagerList(List<DroneApi> list) {
        droneApiList.clear();

        if (list != null && !list.isEmpty()) {
            droneApiList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return droneApiList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_drone_info, parent,
                false);

        final ImageView clientIcon = (ImageView) view.findViewById(R.id.client_icon);
        final TextView clientId = (TextView) view.findViewById(R.id.client_id);
        final TextView clientConnectionInfo = (TextView) view.findViewById(R.id.client_connection_info);
        final TextView clientName = (TextView) view.findViewById(R.id.client_name);

        return new ViewHolder(view, clientIcon, clientName, clientId, clientConnectionInfo);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final DroneApi droneApi = droneApiList.get(position);

        viewHolder.containerView.setOnClickListener(null);
        viewHolder.clientName.setText("UNKNOWN");
        viewHolder.clientId.setText("AppId: unknown");

        final String ownerId = droneApi.getOwnerId();
        if (!TextUtils.isEmpty(ownerId)) {
            viewHolder.clientId.setText("AppId: " + ownerId);

            viewHolder.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(ownerId);
                    if(launchIntent != null)
                        context.startActivity(launchIntent);
                }
            });

            try {
                final PackageManager pm = context.getPackageManager();

                final ApplicationInfo appInfo = pm.getApplicationInfo(ownerId, 0);
                Drawable appIcon = pm.getApplicationIcon(appInfo);
                viewHolder.clientIcon.setImageDrawable(appIcon);

                CharSequence appLabel = pm.getApplicationLabel(appInfo);
                viewHolder.clientName.setText(appLabel);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        if (droneApi.isConnected()) {
            viewHolder.clientConnectionInfo.setText(SpannableUtils.normal("Status: ", SpannableUtils.color(Color
                    .GREEN, "Connected")));
        } else {
            viewHolder.clientConnectionInfo.setText(SpannableUtils.normal("Status: ", SpannableUtils.color(Color.RED,
                    "Disconnected")));
        }
    }
}
