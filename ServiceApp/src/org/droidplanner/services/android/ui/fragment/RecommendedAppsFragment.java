package org.droidplanner.services.android.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.ui.adapter.RecommendedAppsAdapter;

/**
 * Provide a view of recommended apps that are compatible with 3DR Services.
 */
public class RecommendedAppsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_recommended_apps, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity().getApplicationContext();

        final RecyclerView recommendedList = (RecyclerView) view.findViewById(R.id.recommended_apps_list);

        //Use this setting to improve performance if you know that changes in content do not change the layout side
        // of the recycler view.
        recommendedList.setHasFixedSize(true);

        //Use a grid layout manager
        final int colCount = getResources().getInteger(R.integer.recommendedAppsColCount);
        final RecyclerView.LayoutManager gridLayoutMgr = new GridLayoutManager(context, colCount);
        recommendedList.setLayoutManager(gridLayoutMgr);

        recommendedList.setAdapter(new RecommendedAppsAdapter(context));
    }
}
