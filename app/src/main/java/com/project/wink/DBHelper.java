package com.project.wink;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MEssages.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Constants.DBConstants.TABLE_NAME + " (" +
                    Constants.DBConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Constants.DBConstants.COLUMN_NAME_OBJECT_ID + " TEXT," +
                    Constants.DBConstants.COLUMN_NAME_COUNT_MESSAGES + " INTEGER)";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Constants.DBConstants.TABLE_NAME;
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}