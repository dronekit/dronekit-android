package org.droidplanner.services.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidplanner.services.android.R;

/**
 * Created by Fredia Huya-Kouadio on 2/4/15.
 */
public class RecommendedAppsAdapter extends RecyclerView.Adapter<RecommendedAppsAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder{

        final View containerView;
        final TextView appTitle;
        final TextView appDescription;
        final ImageView appIcon;

        public ViewHolder(View itemView, TextView title, TextView description, ImageView icon) {
            super(itemView);
            this.containerView = itemView;
            this.appTitle = title;
            this.appDescription = description;
            this.appIcon = icon;
        }
    }

    private final Context context;

    public RecommendedAppsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount(){
        //TODO: update once complete
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_app_info,
                parent, false);

        final ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        final TextView appTitle = (TextView) view.findViewById(R.id.app_name);
        final TextView appDescription = (TextView) view.findViewById(R.id.app_description);

        return new ViewHolder(view, appTitle, appDescription, appIcon);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position){

    }
}
