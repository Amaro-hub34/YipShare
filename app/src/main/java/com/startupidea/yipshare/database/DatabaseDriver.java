package com.startupidea.yipshare.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.startupidea.yipshare.database.DatabaseContract.Articles;
import com.startupidea.yipshare.database.DatabaseContract.Condominiums;
import com.startupidea.yipshare.database.DatabaseContract.Loans;
import com.startupidea.yipshare.database.DatabaseContract.Members;
import com.startupidea.yipshare.database.DatabaseContract.Notifications;
import com.startupidea.yipshare.database.DatabaseContract.PendingMembers;
import com.startupidea.yipshare.database.DatabaseContract.ReportEvidences;
import com.startupidea.yipshare.database.DatabaseContract.Reports;
import com.startupidea.yipshare.database.DatabaseContract.Reviews;
import com.startupidea.yipshare.database.DatabaseContract.Units;
import com.startupidea.yipshare.database.DatabaseContract.Users;

import java.util.UUID;

public class DatabaseDriver extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yipshare.db";
    private static final int DATABASE_VERSION = 10;

    private static final String createUsers
            = "CREATE TABLE " + Users.TABLE + " ("
            + Users.ID + " TEXT PRIMARY KEY,"
            + Users.USERNAME + " TEXT NOT NULL UNIQUE,"
            + Users.EMAIL + " TEXT NOT NULL UNIQUE,"
            + Users.PHONE_NUMBER + " TEXT NOT NULL UNIQUE,"
            + Users.PASSWORD_HASH + " TEXT NOT NULL,"
            + Users.NAME + " TEXT NOT NULL,"
            + Users.PATERNAL_SURNAME + " TEXT NOT NULL,"
            + Users.MATERNAL_SURNAME + " TEXT,"
            + Users.PROFILE_PICTURE + " TEXT,"
            + Users.COMPUTED_RATING + " INTEGER NOT NULL DEFAULT 0,"
            + Users.STATE + " TEXT NOT NULL DEFAULT 'active' CHECK (" + Users.STATE + " IN ('active', 'deleted')))";

    private static final String createCondominiums
            = "CREATE TABLE " + Condominiums.TABLE + " ("
            + Condominiums.ID + " TEXT PRIMARY KEY,"
            + Condominiums.SHORT_CODE + " TEXT NOT NULL UNIQUE,"
            + Condominiums.ADMIN_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Condominiums.NAME + " TEXT NOT NULL,"
            + Condominiums.STREET + " TEXT NOT NULL,"
            + Condominiums.NEIGHBORHOOD + " TEXT NOT NULL,"
            + Condominiums.POSTAL_CODE + " TEXT NOT NULL,"
            + Condominiums.MUNICIPALITY + " TEXT NOT NULL,"
            + Condominiums.CITY + " TEXT NOT NULL,"
            + Condominiums.STATE + " TEXT NOT NULL,"
            + Condominiums.DEED + " TEXT NOT NULL,"
            + Condominiums.MINUTES + " TEXT NOT NULL,"
            + Condominiums.TAX_ID + " TEXT NOT NULL,"
            + Condominiums.IDENTIFICATION + " TEXT NOT NULL)";

    private static final String createUnits
            = "CREATE TABLE " + Units.TABLE + " ("
            + Units.ID + " TEXT PRIMARY KEY,"
            + Units.CONDOMINIUM_ID + " TEXT NOT NULL REFERENCES " + Condominiums.TABLE + " (" + Condominiums.ID + "),"
            + Units.NAME + " TEXT NOT NULL)";

    private static final String createMembers
            = "CREATE TABLE " + Members.TABLE + " ("
            + Members.USER_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Members.CONDOMINIUM_ID + " TEXT NOT NULL REFERENCES " + Condominiums.TABLE + " (" + Condominiums.ID + "),"
            + Members.UNIT_ID + " TEXT NOT NULL REFERENCES " + Units.TABLE + " (" + Units.ID + "),"
            + Members.CAN_REQUEST_LOANS + " INTEGER NOT NULL DEFAULT 1,"
            + " PRIMARY KEY (" + Members.USER_ID + ", " + Members.UNIT_ID + "))";

    private static final String createPendingMembers
            = "CREATE TABLE " + PendingMembers.TABLE + " ("
            + PendingMembers.USER_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + PendingMembers.CONDOMINIUM_ID + " TEXT NOT NULL REFERENCES " + Condominiums.TABLE + " (" + Condominiums.ID + "),"
            + PendingMembers.UNIT + " TEXT NOT NULL,"
            + PendingMembers.SUGGESTED_UNIT_ID + " TEXT REFERENCES " + Units.TABLE + " (" + Units.ID + "),"
            + " PRIMARY KEY (" + PendingMembers.USER_ID + ", " + PendingMembers.CONDOMINIUM_ID + "))";

    private static final String createArticles
            = "CREATE TABLE " + Articles.TABLE + " ("
            + Articles.ID + " TEXT PRIMARY KEY,"
            + Articles.CONDOMINIUM_ID + " TEXT NOT NULL REFERENCES " + Condominiums.TABLE + " (" + Condominiums.ID + "),"
            + Articles.LENDING_USER + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Articles.NAME + " TEXT NOT NULL,"
            + Articles.CATEGORY + " INTEGER NOT NULL,"
            + Articles.DESCRIPTION + " TEXT NOT NULL,"
            + Articles.PHYSICAL_CONDITION + " INTEGER NOT NULL,"
            + Articles.IMAGE + " TEXT,"
            + Articles.LOAN_DURATIONS + " TEXT NOT NULL,"
            + Articles.AVAILABILITY + " TEXT NOT NULL DEFAULT 'AVAILABLE',"
            + Articles.DELETED + " INTEGER NOT NULL DEFAULT 0)";

    private static final String createLoans
            = "CREATE TABLE " + Loans.TABLE + " ("
            + Loans.ID + " TEXT PRIMARY KEY,"
            + Loans.ARTICLE_ID + " TEXT NOT NULL REFERENCES " + Articles.TABLE + " (" + Articles.ID + "),"
            + Loans.LENDER_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Loans.USER_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Loans.REQUEST_TEXT + " TEXT NOT NULL,"
            + Loans.DAYS + " INTEGER NOT NULL,"
            + Loans.STATE + " TEXT NOT NULL DEFAULT 'PENDING',"
            + Loans.CODE + " TEXT,"
            + Loans.START_TIME + " TEXT,"
            + Loans.RETURN_TIME + " TEXT)";

    private static final String createReviews
            = "CREATE TABLE " + Reviews.TABLE + " ("
            + Reviews.ID + " TEXT PRIMARY KEY,"
            + Reviews.LOAN_ID + " TEXT NOT NULL REFERENCES " + Loans.TABLE + " (" + Loans.ID + "),"
            + Reviews.AUTHOR_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Reviews.USER_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Reviews.REVIEW_TEXT + " TEXT NOT NULL,"
            + Reviews.RATING + " INTEGER NOT NULL CHECK (" + Reviews.RATING + " BETWEEN 1 AND 50),"
            + " UNIQUE (" + Reviews.LOAN_ID + ", " + Reviews.AUTHOR_ID + "))";

    private static final String createReports
            = "CREATE TABLE " + Reports.TABLE + " ("
            + Reports.ID + " TEXT PRIMARY KEY,"
            + Reports.LOAN_ID + " TEXT NOT NULL REFERENCES " + Loans.TABLE + " (" + Loans.ID + "),"
            + Reports.AUTHOR_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Reports.USER_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Reports.REPORT_TEXT + " TEXT NOT NULL,"
            + Reports.STATE + " TEXT NOT NULL DEFAULT 'PENDING',"
            + " UNIQUE (" + Reports.LOAN_ID + ", " + Reports.AUTHOR_ID + "))";

    private static final String createReportEvidences
            = "CREATE TABLE " + ReportEvidences.TABLE + " ("
            + ReportEvidences.ID + " TEXT PRIMARY KEY,"
            + ReportEvidences.REPORT_ID + " TEXT NOT NULL REFERENCES " + Reports.TABLE + " (" + Reports.ID + "),"
            + ReportEvidences.IMAGE + " TEXT NOT NULL)";

    private static final String createNotifications
            = "CREATE TABLE " + Notifications.TABLE + " ("
            + Notifications.ID + " TEXT PRIMARY KEY,"
            + Notifications.USER_ID + " TEXT NOT NULL REFERENCES " + Users.TABLE + " (" + Users.ID + "),"
            + Notifications.HEADER + " TEXT NOT NULL,"
            + Notifications.BODY + " TEXT NOT NULL,"
            + Notifications.IS_READ + " INTEGER NOT NULL DEFAULT 0)";

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

    /*
     * Version history: v1-v10 were development schemas with no production users.
     * Strategy: drop every table and recreate the latest schema, so local test data is lost on upgrade.
     * Once the app ships, replace this with incremental "if (oldVersion < N) ALTER TABLE ..." steps.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try (Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'"
                + " AND name NOT LIKE 'sqlite_%' AND name <> 'android_metadata' ORDER BY rowid DESC", null)) {
            while (cursor.moveToNext()) db.execSQL("DROP TABLE IF EXISTS " + cursor.getString(0));
        }
        onCreate(db);
    }

    public void createNotification(String userId, String header, String body) {
        ContentValues values = new ContentValues();
        values.put(Notifications.ID, UUID.randomUUID().toString());
        values.put(Notifications.USER_ID, userId);
        values.put(Notifications.HEADER, header);
        values.put(Notifications.BODY, body);
        getWritableDatabase().insertOrThrow(Notifications.TABLE, null, values);
    }
}
