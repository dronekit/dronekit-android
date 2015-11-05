package org.droidplanner.services.android.data.database.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.droidplanner.services.android.data.database.ServicesDb;

import java.util.Date;

/**
 * Defines the schema for the SessionData table.
 */
public final class SessionDataTable extends ServicesTable {
    private static final String TABLE_NAME = "session_data";

    private static final String COLUMN_NAME_APP_ID = "app_id";
    private static final String COLUMN_NAME_START_TIME = "start_time";
    private static final String COLUMN_NAME_END_TIME = "end_time";
    private static final String COLUMN_NAME_CONNECTION_TYPE = "connection_type";
    private static final String COLUMN_NAME_DSHARE_UPLOAD_TIME = "dshare_upload_time";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_APP_ID + " TEXT," +
                    COLUMN_NAME_START_TIME + " INTEGER NOT NULL," +
                    COLUMN_NAME_END_TIME + " INTEGER," +
                    COLUMN_NAME_CONNECTION_TYPE + " TEXT," +
                    COLUMN_NAME_DSHARE_UPLOAD_TIME + " INTEGER" +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public SessionDataTable(ServicesDb dbHandler) {
        super(dbHandler);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getSqlCreateStatement() {
        return SQL_CREATE_ENTRIES;
    }

    @Override
    protected String getSqlDeleteStatement() {
        return SQL_DELETE_ENTRIES;
    }

    public void startSession(Date startDate, String connectionType) {
        //Get the data repository in write mode.
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionDataTable.COLUMN_NAME_START_TIME, startDate.getTime());
        values.put(SessionDataTable.COLUMN_NAME_CONNECTION_TYPE, connectionType);

        db.insert(SessionDataTable.TABLE_NAME, null, values);
    }

    public void endSession(Date startDate, String connectionType, Date endDate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionDataTable.COLUMN_NAME_END_TIME, endDate.getTime());

        String selection = SessionDataTable.COLUMN_NAME_START_TIME + " LIKE ? AND " + SessionDataTable
                .COLUMN_NAME_CONNECTION_TYPE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(startDate.getTime()), connectionType};

        db.update(SessionDataTable.TABLE_NAME, values, selection, selectionArgs);
    }

    public void updateDroneShareUploadTime(Date startDate, String connectionType, Date uploadDate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionDataTable.COLUMN_NAME_DSHARE_UPLOAD_TIME, uploadDate.getTime());

        String selection = SessionDataTable.COLUMN_NAME_START_TIME + " LIKE ? AND " + SessionDataTable
                .COLUMN_NAME_CONNECTION_TYPE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(startDate.getTime()), connectionType};

        db.update(SessionDataTable.TABLE_NAME, values, selection, selectionArgs);
    }
}
