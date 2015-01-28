package org.droidplanner.services.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

import org.droidplanner.services.android.data.SessionContract.SessionData;

/**
 * Created by fhuya on 12/30/14.
 */
public class SessionDB extends SQLiteOpenHelper {

    private static final String TAG = SessionDB.class.getSimpleName();

    public SessionDB(Context context) {
        super(context, SessionContract.DB_NAME, null, SessionContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating session database.");
        db.execSQL(SessionContract.getSqlCreateEntries());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SessionContract.getSqlDeleteEntries());
        onCreate(db);
    }

    public void startSession(Date startDate, String connectionType){
        //Get the data repository in write mode.
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionData.COLUMN_NAME_START_TIME, startDate.getTime());
        values.put(SessionData.COLUMN_NAME_CONNECTION_TYPE, connectionType);

        db.insert(SessionData.TABLE_NAME, null, values);
    }

    public void endSession(Date startDate, String connectionType, Date endDate){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionData.COLUMN_NAME_END_TIME, endDate.getTime());

        String selection = SessionData.COLUMN_NAME_START_TIME + " LIKE ? AND " + SessionData
                .COLUMN_NAME_CONNECTION_TYPE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(startDate.getTime()), connectionType};

        db.update(SessionData.TABLE_NAME, values, selection, selectionArgs);
    }

    public void updateDroneShareUploadTime(Date startDate, String connectionType, Date uploadDate){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionData.COLUMN_NAME_DSHARE_UPLOAD_TIME, uploadDate.getTime());

        String selection = SessionData.COLUMN_NAME_START_TIME + " LIKE ? AND " + SessionData
                .COLUMN_NAME_CONNECTION_TYPE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(startDate.getTime()), connectionType};

        db.update(SessionData.TABLE_NAME, values, selection, selectionArgs);
    }
}
