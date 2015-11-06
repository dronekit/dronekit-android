package org.droidplanner.services.android.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.ServicesApp;
import org.droidplanner.services.android.core.drone.profiles.ParameterMetadata;
import org.droidplanner.services.android.data.database.tables.ParametersMetadataTable;

import java.util.List;

/**
 * Created by Fredia Huya-Kouadio on 11/5/15.
 */
public class ParametersMetadataViewer extends AppCompatActivity {

    private ParametersMetadataTable paramsMetadata;
    private ArrayAdapter<ParameterMetadata> viewerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters_metadata_viewer);

        //Retrieve the table
        paramsMetadata = ((ServicesApp)getApplication()).getServicesDatabase().getParametersMetadataTable();

        viewerAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item_parameter_metadata);

        ListView viewer = (ListView) findViewById(R.id.parameters_metadata_viewer);
        viewer.setAdapter(viewerAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();

        //Retrieve all the entries in the table.
        List<ParameterMetadata> metadataList = paramsMetadata.retrieveAllParameterMetadata();
        viewerAdapter.clear();
        viewerAdapter.addAll(metadataList);
    }
}
