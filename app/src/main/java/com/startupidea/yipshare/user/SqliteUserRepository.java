package com.startupidea.yipshare.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseContract.Users;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.UnauthorizedException;
import com.startupidea.yipshare.utils.ImageFileUtil;
import com.startupidea.yipshare.utils.PasswordHashUtil;

import java.util.Optional;
import java.util.UUID;

public class SqliteUserRepository implements UserRepository {

    private static final String getActiveUserByUsername
            = "SELECT " + Users.ID + ", " + Users.PASSWORD_HASH + " FROM " + Users.TABLE
            + " WHERE " + Users.USERNAME + " = ? AND " + Users.STATE + " = 'active'";

    private static final String getUserProfileById
            = "SELECT " + Users.ID + ", " + Users.USERNAME + ", " + Users.NAME + ", " + Users.PATERNAL_SURNAME + ", "
            + Users.MATERNAL_SURNAME + ", " + Users.PHONE_NUMBER + ", " + Users.EMAIL + ", "
            + Users.COMPUTED_RATING + ", " + Users.PROFILE_PICTURE
            + " FROM " + Users.TABLE + " WHERE " + Users.ID + " = ? AND " + Users.STATE + " = 'active'";

    private static final String getPasswordHash
            = "SELECT " + Users.PASSWORD_HASH + " FROM " + Users.TABLE + " WHERE " + Users.ID + " = ?";

    private static final String getActiveUser
            = "SELECT 1 FROM " + Users.TABLE + " WHERE " + Users.ID + " = ? AND " + Users.STATE + " = 'active'";

    private static final String getOtherUserByColumn
            = "SELECT 1 FROM " + Users.TABLE + " WHERE %s = ? AND " + Users.ID + " <> ?";

    private final DatabaseDriver databaseDriver;
    private final Context context;

    public SqliteUserRepository(Context context) {
        this.databaseDriver = DatabaseDriver.getInstance(context);
        this.context = context.getApplicationContext();
    }

    @Override
    public Optional<User> tryLogin(LoginPayload payload) {
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getActiveUserByUsername, new String[]{payload.username().trim()})) {
            if (!cursor.moveToFirst() || !PasswordHashUtil.matches(payload.password(), cursor.getString(1))) return Optional.empty();
            return Optional.of(new User(cursor.getString(0), payload.username().trim()));
        }
    }

    @Override
    public User createUser(RegistrationPayload payload) {
        String id = UUID.randomUUID().toString();
        ensureUnique(id, payload.username(), payload.email(), payload.phoneNumber());

        ContentValues values = new ContentValues();
        values.put(Users.ID, id);
        values.put(Users.USERNAME, payload.username());
        values.put(Users.EMAIL, payload.email());
        values.put(Users.PHONE_NUMBER, payload.phoneNumber());
        values.put(Users.PASSWORD_HASH, PasswordHashUtil.hash(payload.password()));
        values.put(Users.NAME, payload.firstNames());
        values.put(Users.PATERNAL_SURNAME, payload.paternalLastName());
        values.put(Users.MATERNAL_SURNAME, payload.maternalLastName());
        databaseDriver.getWritableDatabase().insertOrThrow(Users.TABLE, null, values);
        return new User(id, payload.username());
    }

    @Override
    public Optional<UserProfile> getUserProfileById(String id) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getUserProfileById, new String[]{id})) {
            if (!cursor.moveToFirst()) return Optional.empty();
            return Optional.of(new UserProfile(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8)));
        }
    }

    @Override
    public void modifyUser(String id, ProfilePayload payload, String newPassword, String currentPassword) {
        if (newPassword != null && !matchesPassword(id, currentPassword)) throw new UnauthorizedException("Current password does not match", R.string.error_contrasena_actual);
        ensureUnique(id, payload.username(), payload.email(), payload.phoneNumber());

        ContentValues values = new ContentValues();
        values.put(Users.USERNAME, payload.username());
        values.put(Users.NAME, payload.firstNames());
        values.put(Users.PATERNAL_SURNAME, payload.paternalLastName());
        values.put(Users.MATERNAL_SURNAME, payload.maternalLastName());
        values.put(Users.PHONE_NUMBER, payload.phoneNumber());
        values.put(Users.EMAIL, payload.email());
        if (newPassword != null) values.put(Users.PASSWORD_HASH, PasswordHashUtil.hash(newPassword));
        databaseDriver.getWritableDatabase().update(Users.TABLE, values, Users.ID + " = ?", new String[]{id});
    }

    @Override
    public void modifyProfilePicture(String id, String imageUri) {
        ContentValues values = new ContentValues();
        values.put(Users.PROFILE_PICTURE, ImageFileUtil.saveProfilePicture(context, imageUri));
        databaseDriver.getWritableDatabase().update(Users.TABLE, values, Users.ID + " = ?", new String[]{id});
    }

    @Override
    public void deleteUser(String id) {
        ContentValues values = new ContentValues();
        values.put(Users.STATE, "deleted");
        databaseDriver.getWritableDatabase().update(Users.TABLE, values, Users.ID + " = ?", new String[]{id});
    }

    @Override
    public boolean isActiveUser(String id) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getActiveUser, new String[]{id})) {
            return cursor.moveToFirst();
        }
    }

    private boolean matchesPassword(String id, String password) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getPasswordHash, new String[]{id})) {
            return cursor.moveToFirst() && PasswordHashUtil.matches(password, cursor.getString(0));
        }
    }

    private void ensureUnique(String id, String username, String email, String phoneNumber) {
        if (isTaken(Users.USERNAME, username, id)) throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_duplicado);
        if (isTaken(Users.EMAIL, email, id)) throw new InvalidField(RegistrationField.EMAIL, R.string.error_correo_duplicado);
        if (isTaken(Users.PHONE_NUMBER, phoneNumber, id)) throw new InvalidField(RegistrationField.PHONE_NUMBER, R.string.error_telefono_duplicado);
    }

    private boolean isTaken(String column, String value, String id) {
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(String.format(getOtherUserByColumn, column), new String[]{value, id})) {
            return cursor.moveToFirst();
        }
    }
}
