package com.startupidea.yipshare.notification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.startupidea.yipshare.database.DatabaseContract.Notifications;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.user.SessionRepository;

import java.util.ArrayList;
import java.util.List;

public class SqliteNotificationRepository implements NotificationRepository {

    private static final String getAllNotificationsByUserId
            = "SELECT " + Notifications.ID + ", " + Notifications.HEADER + ", " + Notifications.BODY + ", " + Notifications.IS_READ
            + " FROM " + Notifications.TABLE + " WHERE " + Notifications.USER_ID + " = ? ORDER BY rowid DESC";

    private final DatabaseDriver databaseDriver;
    private final SessionRepository sessionRepository;

    public SqliteNotificationRepository(Context context) {
        this.databaseDriver = DatabaseDriver.getInstance(context);
        this.sessionRepository = new SessionRepository(context);
    }

    @Override
    public List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getAllNotificationsByUserId, new String[]{sessionRepository.getUserId()})) {
            while (cursor.moveToNext()) {
                notifications.add(new Notification(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3) == 1));
            }
        }
        return notifications;
    }

    @Override
    public void markAsRead(String id) {
        ContentValues values = new ContentValues();
        values.put(Notifications.IS_READ, 1);
        databaseDriver.getWritableDatabase().update(Notifications.TABLE, values, Notifications.ID + " = ?", new String[]{id});
    }

    @Override
    public void deleteNotification(String id) {
        databaseDriver.getWritableDatabase().delete(Notifications.TABLE, Notifications.ID + " = ?", new String[]{id});
    }
}
