package org.droidplanner.services.android.data.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import org.droidplanner.services.android.core.drone.profiles.ParameterMetadata;
import org.droidplanner.services.android.data.database.ServicesDb;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Defines the schema for the parameters metadata table.
 * That table is filled from the data in the assets/Parameters/apm.pdef xml file.
 */
public final class ParametersMetadataTable extends ServicesTable {
    private static final String TABLE_NAME = "parameters_metadata";

    private static final String COLUMN_NAME_FAMILY = "autopilot_family";
    private static final String COLUMN_NAME_AUTOPILOT_TYPE = "autopilot_type";
    private static final String COLUMN_NAME_PARAMETER_GROUP = "param_group";
    private static final String COLUMN_NAME_PARAMETER_NAME = "param_name";
    private static final String COLUMN_NAME_PARAMETER_DATATYPE = "param_datatype";
    private static final String COLUMN_NAME_SHORT_DESCRIPTION = "short_description";
    private static final String COLUMN_NAME_DESCRIPTION = "description";
    private static final String COLUMN_NAME_UNITS = "units";
    private static final String COLUMN_NAME_RANGE = "range";
    private static final String COLUMN_NAME_VALUES = "values";

    private static final ArrayMap<String, String> COLUMNS_DEF = new ArrayMap<>(8);

    static {
        COLUMNS_DEF.put(_ID, "INTEGER PRIMARY KEY");
        COLUMNS_DEF.put(COLUMN_NAME_FAMILY, "INTEGER NOT NULL");
        COLUMNS_DEF.put(COLUMN_NAME_AUTOPILOT_TYPE, "TEXT");
        COLUMNS_DEF.put(COLUMN_NAME_PARAMETER_GROUP, "TEXT");
        COLUMNS_DEF.put(COLUMN_NAME_PARAMETER_NAME, "TEXT NOT NULL");
        COLUMNS_DEF.put(COLUMN_NAME_PARAMETER_DATATYPE, "INTEGER NOT NULL");
        COLUMNS_DEF.put(COLUMN_NAME_SHORT_DESCRIPTION, "TEXT");
        COLUMNS_DEF.put(COLUMN_NAME_DESCRIPTION, "TEXT");
        COLUMNS_DEF.put(COLUMN_NAME_UNITS, "TEXT");
        COLUMNS_DEF.put(COLUMN_NAME_RANGE, "TEXT");
        COLUMNS_DEF.put(COLUMN_NAME_VALUES, "TEXT");
    }

    private static final String SQL_CREATE_CONSTRAINT =
            String.format(Locale.US, "UNIQUE (%s, %s, %s, %s) ON CONFLICT REPLACE",
                    COLUMN_NAME_FAMILY, COLUMN_NAME_AUTOPILOT_TYPE, COLUMN_NAME_PARAMETER_GROUP, COLUMN_NAME_PARAMETER_NAME);

    private static final String SQL_CREATE = generateSqlCreateEntries(TABLE_NAME, COLUMNS_DEF, SQL_CREATE_CONSTRAINT);

    private static final String SQL_DELETE = generateSqlDeleteEntries(TABLE_NAME);

    public ParametersMetadataTable(ServicesDb dbHandler) {
        super(dbHandler);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getSqlCreateStatement() {
        return SQL_CREATE;
    }

    @Override
    protected String getSqlDeleteStatement() {
        return SQL_DELETE;
    }

    /**
     * Inserts the given parameter metadata argument into the database, replacing the previous entry if it exists.
     * @param metadata {@link ParameterMetadata} object.
     */
    public void insertParameterMetadata(ParameterMetadata metadata){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_FAMILY, metadata.getAutopilotFamily());
        values.put(COLUMN_NAME_AUTOPILOT_TYPE, metadata.getAutopilotType());
        values.put(COLUMN_NAME_PARAMETER_GROUP, metadata.getGroup());
        values.put(COLUMN_NAME_PARAMETER_NAME, metadata.getName());
        values.put(COLUMN_NAME_PARAMETER_DATATYPE, metadata.getDataType());
        values.put(COLUMN_NAME_SHORT_DESCRIPTION, metadata.getDisplayName());
        values.put(COLUMN_NAME_DESCRIPTION, metadata.getDescription());
        values.put(COLUMN_NAME_UNITS, metadata.getUnits());
        values.put(COLUMN_NAME_RANGE, metadata.getRange());
        values.put(COLUMN_NAME_VALUES, metadata.getValues());

        db.insert(TABLE_NAME, null, values);
    }

    /**
     * Retrieves a parameter metadata from the database that matches the given arguments.
     * @param family Parameter family (i.e: PX4, APM). One of {@link com.MAVLink.enums.MAV_AUTOPILOT} constants.
     * @param autopilotType Parameter autopilot type. Can be null if the parameter is not tied to a specific autopilot.
     * @param paramGroup Parameter group. Can be null if the parameter has no group.
     * @param paramName Non null parameter name.
     * @return {@link ParameterMetadata} object if one is found, null otherwise.
     */
    public ParameterMetadata retrieveParameterMetadata(int family, String autopilotType, String paramGroup, @NonNull String paramName){
        if(TextUtils.isEmpty(paramName))
            return null;

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {COLUMN_NAME_PARAMETER_DATATYPE, COLUMN_NAME_SHORT_DESCRIPTION,
                COLUMN_NAME_DESCRIPTION, COLUMN_NAME_UNITS, COLUMN_NAME_RANGE, COLUMN_NAME_VALUES};

        List<String> selectionArgs = new ArrayList<>();

        StringBuilder selection = new StringBuilder(COLUMN_NAME_FAMILY).append(" = ?");
        selectionArgs.add(String.valueOf(family));
        if(!TextUtils.isEmpty(autopilotType)){
            selection.append(" AND ").append(COLUMN_NAME_AUTOPILOT_TYPE).append(" = ?")
                    .append(" OR ").append(COLUMN_NAME_AUTOPILOT_TYPE).append(" IS NULL")
            .append(" OR ").append(COLUMN_NAME_AUTOPILOT_TYPE).append(" = ''");

            selectionArgs.add(autopilotType);
        }

        if(!TextUtils.isEmpty(paramGroup)){
            selection.append(" AND ").append(COLUMN_NAME_PARAMETER_GROUP).append(" = ?")
                    .append(" OR ").append(COLUMN_NAME_PARAMETER_GROUP).append(" IS NULL")
                    .append(" OR ").append(COLUMN_NAME_PARAMETER_GROUP).append(" = ''");

            selectionArgs.add(paramGroup);
        }

        selection.append(" AND ").append(COLUMN_NAME_PARAMETER_NAME).append(" = ?");
        selectionArgs.add(paramName);

        Cursor cursor = db.query(TABLE_NAME, projection, selection.toString(),
                selectionArgs.toArray(new String[selectionArgs.size()]), null, null, null);

        if(cursor == null)
            return null;

        if(cursor.moveToFirst()){
            ParameterMetadata result = new ParameterMetadata(family);
            result.setAutopilotType(autopilotType);
            result.setGroup(paramGroup);
            result.setName(paramName);

            result.setDataType(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_PARAMETER_DATATYPE)));
            result.setDisplayName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SHORT_DESCRIPTION)));
            result.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DESCRIPTION)));
            result.setUnits(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_UNITS)));
            result.setRange(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_RANGE)));
            result.setValues(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_VALUES)));

            cursor.close();
            return result;
        }

        return null;
    }
}
