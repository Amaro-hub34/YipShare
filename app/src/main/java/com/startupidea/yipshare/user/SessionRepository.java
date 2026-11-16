package com.startupidea.yipshare.user;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionRepository {

    private static final String PREFERENCES = "session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_CONDOMINIUM_ID = "condominium_id";

    private final SharedPreferences preferences;

    public SessionRepository(Context context) {
        this.preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public void createSession(User user) {
        preferences.edit().putString(KEY_USER_ID, user.getId()).apply();
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public String getCondominiumId() {
        return preferences.getString(KEY_CONDOMINIUM_ID, null);
    }

    public void modifyCondominiumId(String condominiumId) {
        preferences.edit().putString(KEY_CONDOMINIUM_ID, condominiumId).apply();
    }

    public void deleteSession() {
        preferences.edit().remove(KEY_USER_ID).remove(KEY_CONDOMINIUM_ID).remove(KEY_TOKEN).apply();
    }
}
