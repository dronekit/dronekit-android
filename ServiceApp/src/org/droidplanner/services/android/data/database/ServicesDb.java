package org.droidplanner.services.android.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.droidplanner.services.android.data.database.tables.ParametersMetadataTable;
import org.droidplanner.services.android.data.database.tables.SessionDataTable;

/**
 * Created by fhuya on 12/30/14.
 */
public class ServicesDb extends SQLiteOpenHelper {

    private static final String DB_NAME = "services_db";
    private static final int DB_VERSION = 1;

    private final SessionDataTable sessionData;
    private final ParametersMetadataTable parametersMetadata;

    public ServicesDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        sessionData = new SessionDataTable(this);
        parametersMetadata = new ParametersMetadataTable(this);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        sessionData.onCreate(db);
        parametersMetadata.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        sessionData.onUpgrade(db, oldVersion, newVersion);
        parametersMetadata.onUpgrade(db, oldVersion, newVersion);
    }

    public ParametersMetadataTable getParametersMetadataTable() {
        return parametersMetadata;
    }

    public SessionDataTable getSessionDataTable() {
        return sessionData;
    }

}
