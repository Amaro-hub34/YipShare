package com.startupidea.yipshare.database;

import android.database.sqlite.SQLiteDatabase;

public final class DemoData {

    public static final String CARLOS = "demo_carlos";
    public static final String ANA = "demo_ana";
    public static final String LUIS = "demo_luis";
    public static final String MARIA = "demo_maria";
    public static final String JORGE = "demo_jorge";
    public static final String GLOBAL_ADMIN = "demo_global_admin";

    private static final String insertDemoUser
            = "INSERT OR IGNORE INTO users"
            + " (user_id, username, password_hash, name, paternal_surname, phone_number, computed_rating, state)"
            + " VALUES (?, ?, '!', ?, ?, ?, ?, 'active')";

    private DemoData() {
    }

    public static void createDemoNeighbors(SQLiteDatabase db) {
        db.execSQL(insertDemoUser, new Object[]{CARLOS, "carlos_ramirez", "Carlos", "Ramírez", "+525511112222", 45});
        db.execSQL(insertDemoUser, new Object[]{ANA, "ana_lopez", "Ana", "López", "+525564449105", 38});
        db.execSQL(insertDemoUser, new Object[]{LUIS, "luis_torres", "Luis", "Torres", "+525555556666", 0});
        db.execSQL(insertDemoUser, new Object[]{MARIA, "maria_gomez", "María", "Gómez", "+525577778888", 0});
        db.execSQL(insertDemoUser, new Object[]{JORGE, "jorge_diaz", "Jorge", "Díaz", "+525599990000", 0});
        db.execSQL(insertDemoUser, new Object[]{GLOBAL_ADMIN, "admin_global", "Admin", "Global", null, 0});
    }
}
