package org.droidplanner.services.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidplanner.services.android.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Loads the recommended apps data from the assets directory.
 */
public class RecommendedAppsAdapter extends RecyclerView.Adapter<RecommendedAppsAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView appTitle;
        final TextView appDescription;
        final ImageView appIcon;
        final Button actionButton;
        final View actionButtonContainer;

        public ViewHolder(View itemView, TextView title, TextView description, ImageView icon,
                          View actionButtonContainer, Button actionButton) {
            super(itemView);
            this.appTitle = title;
            this.appDescription = description;
            this.appIcon = icon;
            this.actionButton = actionButton;
            this.actionButtonContainer = actionButtonContainer;
        }
    }

    private static final String TAG = RecommendedAppsAdapter.class.getSimpleName();

    private static final String RECOMMENDED_APPS_DATA_PATH = "AppStore/recommendedApps.json";

    /* JSON Attributes */
    private static final String APPS_DATA_ATTRIBUTE = "apps";
    private static final String APP_ID_ATTRIBUTE = "applicationId";
    private static final String APP_NAME_ATTRIBUTE = "appName";
    private static final String APP_DESCRIPTION_ATTRIBUTE = "appDescription";
    private static final String APP_ICON_URL_ATTRIBUTE = "appIconUrl";

    private final Context context;
    private final JSONArray recommendedApps;
    private final WeakHashMap<String, Bitmap> appsIconPerId;

    public RecommendedAppsAdapter(Context context) {
        this.context = context;

        JSONArray tempApps = null;
        try {
            final InputStream inputStream = context.getAssets().open(RECOMMENDED_APPS_DATA_PATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final StringBuilder appsData = new StringBuilder();

            while (reader.ready())
                appsData.append(reader.readLine());

            //Parse the read data in a json object.
            JSONObject readData = new JSONObject(appsData.toString());
            tempApps = readData.optJSONArray(APPS_DATA_ATTRIBUTE);
        } catch (IOException | JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        recommendedApps = tempApps == null ? new JSONArray() : tempApps;
        appsIconPerId = new WeakHashMap<>(recommendedApps.length());
    }

    @Override
    public int getItemCount() {
        return recommendedApps.length();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_app_info,
                parent, false);

        final ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        final TextView appTitle = (TextView) view.findViewById(R.id.app_name);
        final TextView appDescription = (TextView) view.findViewById(R.id.app_description);
        final View actionButtonContainer = view.findViewById(R.id.action_button_container);
        final Button actionButton = (Button) view.findViewById(R.id.action_button);

        return new ViewHolder(view, appTitle, appDescription, appIcon, actionButtonContainer, actionButton);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final JSONObject appData = recommendedApps.optJSONObject(position);
        if (appData == null)
            return;

        viewHolder.actionButtonContainer.setVisibility(View.GONE);

        final String appId = appData.optString(APP_ID_ATTRIBUTE, null);
        final String appName = appData.optString(APP_NAME_ATTRIBUTE);
        final String appDescription = appData.optString(APP_DESCRIPTION_ATTRIBUTE);
        final String appIconUrl = appData.optString(APP_ICON_URL_ATTRIBUTE, null);

        viewHolder.appTitle.setText(appName);
        viewHolder.appDescription.setText(appDescription);

        if (appId != null) {
            final ImageView appImageView = viewHolder.appIcon;
            final Bitmap cachedBmp = appsIconPerId.get(appId);
            if (cachedBmp != null) {
                appImageView.setImageBitmap(cachedBmp);
            } else if (appIconUrl != null) {
                new DownloadImageTask(this.context, appId, appImageView, appsIconPerId).execute(appIconUrl);
            }

            viewHolder.actionButtonContainer.setVisibility(View.VISIBLE);

            Intent tmpIntent = context.getPackageManager().getLaunchIntentForPackage(appId);
            if (tmpIntent != null) {
                //The app is installed on the device.
                viewHolder.actionButton.setText(R.string.label_action_button_open);
                viewHolder.actionButton.setBackgroundResource(R.drawable.action_button_open_bg);
            } else {
                //The app is not installed on the device.
                tmpIntent = new Intent(Intent.ACTION_VIEW)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setData(Uri.parse("market://details?id=" + appId));

                viewHolder.actionButton.setText(R.string.label_action_button_install);
                viewHolder.actionButton.setBackgroundResource(R.drawable.action_button_install_bg);
            }

            final Intent launchIntent = tmpIntent;
            viewHolder.actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(launchIntent);
                }
            });
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private static final String ROOT_DIR = "/app_store/icons";
        private static final int BUFFER_SIZE = 1024;

        final ImageView bmImage;
        final String appId;
        final Map<String, Bitmap> cachedMap;
        final File appIconFile;

        public DownloadImageTask(Context context, String appId, ImageView bmImage, Map<String, Bitmap> cachedMap) {
            this.bmImage = bmImage;
            this.appId = appId;
            this.cachedMap = cachedMap;

            final File rootDir = new File(context.getExternalFilesDir(null), ROOT_DIR);
            if (!rootDir.exists() && !rootDir.mkdirs()) {
                throw new IllegalStateException("Unable to create app store icons cache directory.");
            }

            this.appIconFile = new File(rootDir, appId);
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            if (this.appIconFile.isFile() && this.appIconFile.length() > 0) {
                mIcon11 = BitmapFactory.decodeFile(this.appIconFile.getAbsolutePath());
                if (mIcon11 != null)
                    return mIcon11;
            }

            String urlDisplay = urls[0];
            if (urlDisplay == null)
                return null;

            try {
                BufferedInputStream in = new BufferedInputStream(new URL(urlDisplay).openStream());
                final FileOutputStream fos = new FileOutputStream(this.appIconFile);

                final byte[] writeBuffer = new byte[BUFFER_SIZE];
                int byteCount;
                do {
                    byteCount = in.read(writeBuffer);
                    if (byteCount > -1) {
                        fos.write(writeBuffer, 0, byteCount);
                        fos.flush();
                    }
                } while (byteCount != -1);

                fos.close();

                mIcon11 = BitmapFactory.decodeFile(this.appIconFile.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                if (bmImage != null)
                    bmImage.setImageBitmap(result);

                if (cachedMap != null)
                    cachedMap.put(appId, result);
            }
        }
    }
}
