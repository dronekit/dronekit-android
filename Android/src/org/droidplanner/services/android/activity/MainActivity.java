package org.droidplanner.services.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.droidplanner.services.android.R;

/**
 * Created by fhuya on 10/31/14.
 */
public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
