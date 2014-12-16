package org.droidplanner.services.android.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.ui.activity.MainActivity;

/**
 * Created by fhuya on 12/13/14.
 */
public class ConnectionCategoriesFragment extends Fragment {

    private static final String EXTRA_SELECTED_CATEGORY_INDEX = "extra_selected_category_index";

    private MainActivity parent;
    private ViewPager viewPager;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof MainActivity)){
            throw new IllegalStateException("Parent activity must be an instance of " +
                    MainActivity.class.getName());
        }

        parent = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_connections, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        viewPager = (ViewPager) view.findViewById(R.id.connections_view_pager);
        viewPager.setAdapter(new ConnectionCategoryAdapter(getChildFragmentManager()));

        int categoryIndex = 0;
        if(savedInstanceState != null){
            categoryIndex = savedInstanceState.getInt(EXTRA_SELECTED_CATEGORY_INDEX, categoryIndex);
        }

        viewPager.setCurrentItem(categoryIndex);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_CATEGORY_INDEX, viewPager.getCurrentItem());
    }

    private static class ConnectionCategoryAdapter extends FragmentPagerAdapter{

        public ConnectionCategoryAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }
}
