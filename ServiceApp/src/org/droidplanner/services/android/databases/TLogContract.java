package org.droidplanner.services.android.databases;

import android.provider.BaseColumns;

/**
 * Used to define the schema for the TLog database.
 */
public final class TLogContract {

    public static final String DB_NAME = "tlog_db";
    public static final int DB_VERSION = 1;

    public static final String SQL_CREATE_ENTRIES = TLogData.SQL_CREATE_ENTRIES;
    public static final String SQL_DELETE_ENTRIES = TLogData.SQL_DELETE_ENTRIES;

    //Private constructor to prevent instantiation.
    private TLogContract(){}

    /**
     * Defines the schema for the TLogData table.
     */
    public static final class TLogData implements BaseColumns {
        public static final String TABLE_NAME = "tlog_data";
        public static final String COLUMN_NAME_CONNECTION_TIME = "connection_time";
        public static final String COLUMN_NAME_PACKET_TIMESTAMP = "packet_timestamp";
        public static final String COLUMN_NAME_PACKET_SEQ = "packet_sequence";
        public static final String COLUMN_NAME_SYS_ID = "sys_id";
        public static final String COLUMN_NAME_COMPONENT_ID = "component_id";
        public static final String COLUMN_NAME_MSG_ID = "message_id";
        public static final String COLUMN_NAME_MSG_DATA = "message_data";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " +  TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_CONNECTION_TIME + " INTEGER NOT NULL," +
                        COLUMN_NAME_PACKET_TIMESTAMP + " INTEGER NOT NULL," +
                        COLUMN_NAME_PACKET_SEQ + " INTEGER NOT NULL," +
                        COLUMN_NAME_SYS_ID + " INTEGER NOT NULL," +
                        COLUMN_NAME_COMPONENT_ID + " INTEGER NOT NULL," +
                        COLUMN_NAME_MSG_ID + " INTEGER NOT NULL," +
                        COLUMN_NAME_MSG_DATA + " BLOB" +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
