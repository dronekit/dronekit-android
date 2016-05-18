package com.o3dr.android.client.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.R;

/**
 * Created by fhuya on 11/14/14.
 */
public class InstallServiceDialog extends FragmentActivity {

    public static final String EXTRA_REQUIREMENT = "extra_requirement";

    public static final int REQUIRE_INSTALL = 0;
    public static final int REQUIRE_UPDATE = 1;

    private Button installButton;
    private TextView dialogTitle;
    private TextView dialogMsg;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_service_dialog);

        final Button cancelButton = (Button) findViewById(R.id.dialog_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dialogTitle = (TextView) findViewById(R.id.dialog_title);
        dialogMsg = (TextView) findViewById(R.id.dialog_message);

        installButton = (Button) findViewById(R.id.dialog_install_button);
        installButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PackageManager pm = getPackageManager();

                Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=org.droidplanner.services.android"));
                if(pm.resolveActivity(marketIntent, 0) == null){
                    marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.droidplanner.services.android"));
                    if(pm.resolveActivity(marketIntent, 0) == null){
                        marketIntent = null;
                    }
                }

                if(marketIntent != null)
                    startActivity(marketIntent);
                else{
                    Toast.makeText(getApplicationContext(), "No app found to complete the process.", Toast
                            .LENGTH_LONG).show();
                }

                finish();
            }
        });

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if(intent == null){
            finish();
            return;
        }

        final int requirement = intent.getIntExtra(EXTRA_REQUIREMENT, REQUIRE_INSTALL);
        switch(requirement){
            case REQUIRE_INSTALL:
                installButton.setText(R.string.install_service_dialog_install_button);
                dialogTitle.setText(R.string.install_service_dialog_title);
                dialogMsg.setText(R.string.install_service_dialog_message);
                break;

            case REQUIRE_UPDATE:
                installButton.setText(R.string.update_service_dialog_update_button);
                dialogTitle.setText(R.string.update_service_dialog_title);
                dialogMsg.setText(R.string.update_service_dialog_message);
                break;
        }
    }
}
