package com.startupidea.yipshare.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.startupidea.yipshare.error.DatabaseUnavailable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDriver extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yipshare.db";
    private static final int DATABASE_VERSION = 8;
    private static final String SCHEMA_FILE = "schema.sql";

    private static final String getAllTables
            = "SELECT name FROM sqlite_master WHERE type = 'table'"
            + " AND name NOT LIKE 'sqlite_%' AND name <> 'android_metadata' ORDER BY rowid DESC";

    private final Context context;

    public DatabaseDriver(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String statement : readSchema().split(";")) {
            if (!statement.trim().isEmpty()) db.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<String> tables = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(getAllTables, null)) {
            while (cursor.moveToNext()) tables.add(cursor.getString(0));
        }
        for (String table : tables) db.execSQL("DROP TABLE IF EXISTS " + table);
        onCreate(db);
    }

    private String readSchema() {
        StringBuilder schema = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(SCHEMA_FILE)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("--")) schema.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new DatabaseUnavailable();
        }
        return schema.toString();
    }
}
