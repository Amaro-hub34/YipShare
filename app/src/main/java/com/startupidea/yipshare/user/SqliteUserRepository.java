package com.startupidea.yipshare.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.DatabaseUnavailable;
import com.startupidea.yipshare.utils.PasswordHashUtil;

import java.util.UUID;

public class SqliteUserRepository implements UserRepository {

    private static final String getActiveUserByUsername
            = "SELECT user_id, password_hash FROM users WHERE username = ? AND state = 'active'";

    private static final String getUserProfileById
            = "SELECT user_id, username, name, paternal_surname, maternal_surname, phone_number, computed_rating"
            + " FROM users WHERE user_id = ?";

    private static final String getPasswordHash
            = "SELECT password_hash FROM users WHERE user_id = ? AND state = 'active'";

    private static final String getActiveUser
            = "SELECT 1 FROM users WHERE user_id = ? AND state = 'active'";

    private static final String deleteUser
            = "UPDATE users SET state = 'deleted' WHERE user_id = ?";

    private static final String getUserByUsername
            = "SELECT 1 FROM users WHERE username = ?";

    private static final String getUserByPhoneNumber
            = "SELECT 1 FROM users WHERE phone_number = ?";

    private final DatabaseDriver databaseDriver;

    public SqliteUserRepository(DatabaseDriver databaseDriver) {
        this.databaseDriver = databaseDriver;
    }

    @Override
    public User tryLogin(LoginPayload payload) {
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getActiveUserByUsername, new String[]{payload.getUsername()})) {
            if (!cursor.moveToFirst()) return null;
            if (!PasswordHashUtil.matches(payload.getPassword(), cursor.getString(1))) return null;
            return new User(cursor.getString(0), payload.getUsername());
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public boolean existsUsername(String username) {
        return exists(getUserByUsername, username);
    }

    @Override
    public boolean existsPhoneNumber(String phoneNumber) {
        return exists(getUserByPhoneNumber, phoneNumber);
    }

    @Override
    public User createUser(RegistrationPayload payload) {
        String id = UUID.randomUUID().toString();

        ContentValues values = new ContentValues();
        values.put("user_id", id);
        values.put("username", payload.getUsername());
        values.put("password_hash", PasswordHashUtil.hash(payload.getPassword()));
        values.put("paternal_surname", payload.getPaternalLastName());
        values.put("maternal_surname", payload.getMaternalLastName());
        values.put("name", payload.getFirstNames());
        values.put("phone_number", payload.getPhoneNumber());
        values.put("computed_rating", 0);
        values.put("state", "active");

        try {
            databaseDriver.getWritableDatabase().insertOrThrow("users", null, values);
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return new User(id, payload.getUsername());
    }

    @Override
    public UserProfile getUserProfileById(String id) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getUserProfileById, new String[]{id})) {
            if (!cursor.moveToFirst()) return null;
            return new UserProfile(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6));
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public boolean matchesPassword(String id, String password) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getPasswordHash, new String[]{id})) {
            return cursor.moveToFirst() && PasswordHashUtil.matches(password, cursor.getString(0));
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void modifyUser(String id, ProfilePayload payload, String newPassword) {
        ContentValues values = new ContentValues();
        values.put("username", payload.getUsername());
        values.put("name", payload.getFirstNames());
        values.put("paternal_surname", payload.getPaternalLastName());
        values.put("maternal_surname", payload.getMaternalLastName());
        values.put("phone_number", payload.getPhoneNumber());
        if (newPassword != null) values.put("password_hash", PasswordHashUtil.hash(newPassword));

        try {
            databaseDriver.getWritableDatabase().update("users", values, "user_id = ?", new String[]{id});
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void deleteUser(String id) {
        try {
            databaseDriver.getWritableDatabase().execSQL(deleteUser, new Object[]{id});
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public boolean isActiveUser(String id) {
        return exists(getActiveUser, id);
    }

    private boolean exists(String query, String value) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(query, new String[]{value})) {
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }
}
