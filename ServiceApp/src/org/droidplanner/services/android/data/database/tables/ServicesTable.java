package org.droidplanner.services.android.data.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import org.droidplanner.services.android.data.database.ServicesDb;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 11/4/15.
 */
public abstract class ServicesTable implements BaseColumns {

    private final ServicesDb dbHandler;

    public ServicesTable(ServicesDb dbHandler){
        this.dbHandler = dbHandler;
    }

    protected final SQLiteDatabase getWritableDatabase(){
        return dbHandler.getWritableDatabase();
    }

    protected final SQLiteDatabase getReadableDatabase(){
        return dbHandler.getReadableDatabase();
    }

    public final void onCreate(SQLiteDatabase db){
        Timber.i("Creating %s table", getTableName());
        db.execSQL(getSqlCreateStatement());
    }

    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Timber.i("Upgrading %s table from version %d to %d", getTableName(), oldVersion, newVersion);
        db.execSQL(getSqlDeleteStatement());
        onCreate(db);
    }

    protected abstract String getTableName();

    protected abstract String getSqlCreateStatement();

    protected abstract String getSqlDeleteStatement();

    protected static String generateSqlCreateEntries(String tableName, Map<String, String> columnsDef, String constraint){
        StringBuilder sqlCreate = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        if(!columnsDef.isEmpty()){
            boolean isFirst = true;
            for(Map.Entry<String, String> columnDef: columnsDef.entrySet()){
                if(!isFirst){
                    sqlCreate.append(", ");
                }

                sqlCreate.append(columnDef.getKey()).append(" ").append(columnDef.getValue());
                isFirst = false;
            }
        }
        if(!TextUtils.isEmpty(constraint)){
            sqlCreate.append(", ").append(constraint);
        }

        sqlCreate.append(" )");
        return sqlCreate.toString();
    }

    protected static String generateSqlDeleteEntries(String tableName){
        return "DROP TABLE IF EXISTS " + tableName;
    }
}
