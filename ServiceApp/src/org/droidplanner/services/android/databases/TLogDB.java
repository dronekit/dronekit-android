package org.droidplanner.services.android.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import org.droidplanner.services.android.databases.TLogContract.TLogData;

/**
 * Created by fhuya on 12/30/14.
 */
public class TLogDB extends SQLiteOpenHelper {

    private final static String TAG = TLogDB.class.getSimpleName();

    public TLogDB(Context context) {
        super(context, TLogContract.DB_NAME, null, TLogContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database.");
        db.execSQL(TLogContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO
    }

    public long insertTLogData(long connectionTime, long packetTimestamp, MAVLinkPacket mavPacket){
        //Gets the data repository in write mode.
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TLogData.COLUMN_NAME_CONNECTION_TIME, connectionTime);
        values.put(TLogData.COLUMN_NAME_PACKET_TIMESTAMP, packetTimestamp);
        values.put(TLogData.COLUMN_NAME_PACKET_SEQ, mavPacket.seq);
        values.put(TLogData.COLUMN_NAME_SYS_ID, mavPacket.sysid);
        values.put(TLogData.COLUMN_NAME_COMPONENT_ID, mavPacket.compid);
        values.put(TLogData.COLUMN_NAME_MSG_ID, mavPacket.msgid);
        values.put(TLogData.COLUMN_NAME_MSG_DATA, mavPacket.encodePacket());

        return db.insert(TLogData.TABLE_NAME, null, values);
    }
}
