package com.startupidea.yipshare.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.UUID;

public class DatabaseDriver extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yipshare.db";
    private static final int DATABASE_VERSION = 10;

    private static final String createUsers
            = "CREATE TABLE users ("
            + " user_id TEXT PRIMARY KEY,"
            + " username TEXT NOT NULL UNIQUE,"
            + " email TEXT NOT NULL UNIQUE,"
            + " phone_number TEXT NOT NULL UNIQUE,"
            + " password_hash TEXT NOT NULL,"
            + " name TEXT NOT NULL,"
            + " paternal_surname TEXT NOT NULL,"
            + " maternal_surname TEXT,"
            + " profile_picture TEXT,"
            + " computed_rating INTEGER NOT NULL DEFAULT 0,"
            + " state TEXT NOT NULL DEFAULT 'active' CHECK (state IN ('active', 'deleted')))";

    private static final String createCondominiums
            = "CREATE TABLE condominiums ("
            + " condominium_id TEXT PRIMARY KEY,"
            + " short_code TEXT NOT NULL UNIQUE,"
            + " admin_id TEXT NOT NULL REFERENCES users (user_id),"
            + " name TEXT NOT NULL,"
            + " street TEXT NOT NULL,"
            + " neighborhood TEXT NOT NULL,"
            + " postal_code TEXT NOT NULL,"
            + " municipality TEXT NOT NULL,"
            + " city TEXT NOT NULL,"
            + " state TEXT NOT NULL,"
            + " deed TEXT NOT NULL,"
            + " minutes TEXT NOT NULL,"
            + " tax_id TEXT NOT NULL,"
            + " identification TEXT NOT NULL)";

    private static final String createUnits
            = "CREATE TABLE condominium_units ("
            + " condominium_unit_id TEXT PRIMARY KEY,"
            + " condominium_id TEXT NOT NULL REFERENCES condominiums (condominium_id),"
            + " name TEXT NOT NULL)";

    private static final String createMembers
            = "CREATE TABLE condominium_members ("
            + " user_id TEXT NOT NULL REFERENCES users (user_id),"
            + " condominium_id TEXT NOT NULL REFERENCES condominiums (condominium_id),"
            + " condominium_unit_id TEXT NOT NULL REFERENCES condominium_units (condominium_unit_id),"
            + " can_request_loans INTEGER NOT NULL DEFAULT 1,"
            + " PRIMARY KEY (user_id, condominium_unit_id))";

    private static final String createPendingMembers
            = "CREATE TABLE pending_condominium_members ("
            + " user_id TEXT NOT NULL REFERENCES users (user_id),"
            + " condominium_id TEXT NOT NULL REFERENCES condominiums (condominium_id),"
            + " unit TEXT NOT NULL,"
            + " suggested_unit_id TEXT REFERENCES condominium_units (condominium_unit_id),"
            + " PRIMARY KEY (user_id, condominium_id))";

    private static final String createArticles
            = "CREATE TABLE articles ("
            + " article_id TEXT PRIMARY KEY,"
            + " condominium_id TEXT NOT NULL REFERENCES condominiums (condominium_id),"
            + " lending_user TEXT NOT NULL REFERENCES users (user_id),"
            + " name TEXT NOT NULL,"
            + " category INTEGER NOT NULL,"
            + " description TEXT NOT NULL,"
            + " physical_condition INTEGER NOT NULL,"
            + " image TEXT,"
            + " loan_durations TEXT NOT NULL,"
            + " availability TEXT NOT NULL DEFAULT 'AVAILABLE',"
            + " deleted INTEGER NOT NULL DEFAULT 0)";

    private static final String createLoans
            = "CREATE TABLE loans ("
            + " loan_id TEXT PRIMARY KEY,"
            + " article_id TEXT NOT NULL REFERENCES articles (article_id),"
            + " lender_id TEXT NOT NULL REFERENCES users (user_id),"
            + " user_id TEXT NOT NULL REFERENCES users (user_id),"
            + " request_text TEXT NOT NULL,"
            + " days INTEGER NOT NULL,"
            + " state TEXT NOT NULL DEFAULT 'PENDING',"
            + " code TEXT,"
            + " start_time TEXT,"
            + " return_time TEXT)";

    private static final String createReviews
            = "CREATE TABLE reviews ("
            + " review_id TEXT PRIMARY KEY,"
            + " loan_id TEXT NOT NULL REFERENCES loans (loan_id),"
            + " author_id TEXT NOT NULL REFERENCES users (user_id),"
            + " user_id TEXT NOT NULL REFERENCES users (user_id),"
            + " review_text TEXT NOT NULL,"
            + " rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 50),"
            + " UNIQUE (loan_id, author_id))";

    private static final String createReports
            = "CREATE TABLE reports ("
            + " report_id TEXT PRIMARY KEY,"
            + " loan_id TEXT NOT NULL REFERENCES loans (loan_id),"
            + " author_id TEXT NOT NULL REFERENCES users (user_id),"
            + " user_id TEXT NOT NULL REFERENCES users (user_id),"
            + " report_text TEXT NOT NULL,"
            + " state TEXT NOT NULL DEFAULT 'PENDING',"
            + " UNIQUE (loan_id, author_id))";

    private static final String createReportEvidences
            = "CREATE TABLE report_evidences ("
            + " report_evidence_id TEXT PRIMARY KEY,"
            + " report_id TEXT NOT NULL REFERENCES reports (report_id),"
            + " image TEXT NOT NULL)";

    private static final String createNotifications
            = "CREATE TABLE notifications ("
            + " notification_id TEXT PRIMARY KEY,"
            + " user_id TEXT NOT NULL REFERENCES users (user_id),"
            + " header TEXT NOT NULL,"
            + " body TEXT NOT NULL,"
            + " is_read INTEGER NOT NULL DEFAULT 0)";

    private static DatabaseDriver instance;

    private DatabaseDriver(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseDriver getInstance(Context context) {
        if (instance == null) instance = new DatabaseDriver(context);
        return instance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createUsers);
        db.execSQL(createCondominiums);
        db.execSQL(createUnits);
        db.execSQL(createMembers);
        db.execSQL(createPendingMembers);
        db.execSQL(createArticles);
        db.execSQL(createLoans);
        db.execSQL(createReviews);
        db.execSQL(createReports);
        db.execSQL(createReportEvidences);
        db.execSQL(createNotifications);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try (Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'"
                + " AND name NOT LIKE 'sqlite_%' AND name <> 'android_metadata' ORDER BY rowid DESC", null)) {
            while (cursor.moveToNext()) db.execSQL("DROP TABLE IF EXISTS " + cursor.getString(0));
        }
        onCreate(db);
    }

    public void createNotification(String userId, String header, String body) {
        getWritableDatabase().execSQL("INSERT INTO notifications (notification_id, user_id, header, body) VALUES (?, ?, ?, ?)",
                new Object[]{UUID.randomUUID().toString(), userId, header, body});
    }
}
