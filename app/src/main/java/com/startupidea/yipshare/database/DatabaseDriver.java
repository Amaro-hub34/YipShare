package com.startupidea.yipshare.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseDriver extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yipshare.db";
    private static final int DATABASE_VERSION = 9;

    private static final String createResponses
            = "CREATE TABLE responses (user_id TEXT NOT NULL, path TEXT NOT NULL, body TEXT NOT NULL,"
            + " PRIMARY KEY (user_id, path))";
    private static final String getResponse = "SELECT body FROM responses WHERE user_id = ? AND path = ?";

    private static DatabaseDriver instance;

    private DatabaseDriver(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseDriver getInstance(Context context) {
        if (instance == null) instance = new DatabaseDriver(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createResponses);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try (Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'"
                + " AND name NOT LIKE 'sqlite_%' AND name <> 'android_metadata'", null)) {
            while (cursor.moveToNext()) db.execSQL("DROP TABLE IF EXISTS " + cursor.getString(0));
        }
        onCreate(db);
    }

    public void saveResponse(String userId, String path, String body) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("path", path);
        values.put("body", body);
        getWritableDatabase().insertWithOnConflict("responses", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getResponse(String userId, String path) {
        try (Cursor cursor = getReadableDatabase().rawQuery(getResponse, new String[]{userId, path})) {
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        }
    }
}
