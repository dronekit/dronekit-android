package org.droidplanner.services.android.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.o3dr.services.android.lib.data.ServiceDataContract;

import org.droidplanner.services.android.R;
import org.droidplanner.services.android.utils.file.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * TLog file selector activity.
 */
public class TLogFileSelector extends ActionBarActivity {

    private static final String TAG = TLogFileSelector.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tlog_file_selector);

        final Context context = getApplicationContext();

        final Intent pickerIntent = getIntent();
        final String appId = pickerIntent.getStringExtra(ServiceDataContract.EXTRA_REQUEST_TLOG_APP_ID);

        //Set up an intent to send back to apps that request a file.
        final Intent resultIntent = new Intent(ServiceDataContract.ACTION_RETURN_TLOG);

        //Set the activity's result to null to begin with
        setResult(Activity.RESULT_CANCELED, null);

        final File[] tlogFiles = FileUtils.getTLogFileList(context, appId);
        final TreeMap<String, File> sortedFiles = new TreeMap<>();
        for(File file : tlogFiles){
            sortedFiles.put(file.getName(), file);
        }

        final ArrayList<File> sortedList = new ArrayList<>(sortedFiles.size());
        for(Map.Entry<String, File> entry : sortedFiles.entrySet()){
            sortedList.add(entry.getValue());
        }

        final FileAdapter filesAdapter = new FileAdapter(context, sortedList);

        final ListView tlogListView = (ListView) findViewById(R.id.tlog_files_list);
        tlogListView.setAdapter(filesAdapter);
        tlogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get a file for the selected file name.
                File requestFile = tlogFiles[position];
                resultIntent.putExtra(ServiceDataContract.EXTRA_TLOG_ABSOLUTE_PATH, requestFile.getAbsolutePath());
                //Set the result
                setResult(Activity.RESULT_OK, resultIntent);

//                //Use the FileProvider to get a content URI
//                try{
//                    Uri fileUri = FileProvider.getUriForFile(context, ServiceDataContract.FILE_PROVIDER_AUTHORITY,
//                            requestFile);
//
//                    //Grant temporary read permission to the content URI
//                    resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                    //Put the uri and mime type in the result intent
//                    final String mimeType = getContentResolver().getType(fileUri);
//                    resultIntent.setDataAndType(fileUri, mimeType);
//
//                    //Set the result
//                    setResult(Activity.RESULT_OK, resultIntent);
//                }
//                catch(IllegalArgumentException e){
//                    Log.e(TAG, "The selected file can't be shared: " + requestFile.getName());
//
//                    resultIntent.setDataAndType(null, "");
//                    setResult(Activity.RESULT_CANCELED, resultIntent);
//                }

                finish();
            }
        });
    }

    private static class FileAdapter extends ArrayAdapter<File> {

        public FileAdapter(Context context, List<File> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            File file = getItem(position);
            if (convertView != null) {
                view = (TextView) convertView;
            } else {
                view = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.list_item_tlog_info, parent, false);
            }

            view.setText(file.getName());
            return view;
        }
    }
}
