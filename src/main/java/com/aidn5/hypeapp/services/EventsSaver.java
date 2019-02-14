package com.aidn5.hypeapp.services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.File;

public class EventsSaver {
    private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS 'data' ('key' TEXT PRIMARY KEY, 'value' TEXT, modifyTime INTEGER);";
    private static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS data;";
    private static final String QUERY_PUT = "INSERT OR REPLACE INTO 'data' (key, value, modifyTime) VALUES (?, ?, ?);";
    private static final String QUERY_GET = "SELECT * FROM 'data' where key = ?;";
    private static final String QUERY_GET_ALL = "SELECT * FROM 'data';";
    private static String databaseFile; // Cache. probably has no big effect on performance :)
    private final DB db;
    private final SQLiteDatabase sql;
    private final SQLiteStatement PREPARED_PUT;
    public int TimeController = -1;

    public EventsSaver(Context context) throws SnappydbException {
        this.db = DBFactory.open(context, EventsSaver.class.getSimpleName());

        String path = getDatabaseDir(context) + "";
        this.sql = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);

        createTable();

        PREPARED_PUT = this.sql.compileStatement(QUERY_PUT);
    }

    private String getDatabaseDir(Context context) {
        if (databaseFile == null) {
            File file = new File(context.getFilesDir().getAbsolutePath() + "/databases/");
            if (!file.exists()) //noinspection ResultOfMethodCallIgnored
                file.mkdir();

            databaseFile = file.getAbsolutePath();
        }
        return databaseFile;
    }

    public long put(String key, String value) {
        synchronized (PREPARED_PUT) {
            PREPARED_PUT.clearBindings();

            PREPARED_PUT.bindString(1, key);
            PREPARED_PUT.bindString(2, value);
            PREPARED_PUT.bindLong(3, System.currentTimeMillis());
            return PREPARED_PUT.executeInsert();
        }
    }

    public Set get(String key) {
        Cursor cursor = this.sql.rawQuery(QUERY_GET, new String[]{key});

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        Set set = new Set();
        set.key = cursor.getString(0);
        set.value = cursor.getString(1);
        set.modifyTime = cursor.getInt(3);

        cursor.close();
        return set;
    }

    public boolean clean() {
        return false;
    }

    public void cleanDatabase() {
        this.sql.beginTransaction();

        dropTable();
        createTable();

        this.sql.setTransactionSuccessful();
    }

    private void createTable() {
        this.sql.execSQL(QUERY_CREATE_TABLE);
    }

    private void dropTable() {
        this.sql.execSQL(QUERY_DROP_TABLE);
    }

    public class Set {
        public String key;
        public String value;
        public Integer modifyTime = -1;
    }
}