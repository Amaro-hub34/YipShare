package com.startupidea.yipshare.notification;

import android.content.Context;
import android.database.Cursor;

import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.user.SessionRepository;

import java.util.ArrayList;
import java.util.List;

public class SqliteNotificationRepository implements NotificationRepository {

    private static final String getAllNotificationsByUserId
            = "SELECT notification_id, header, body, is_read FROM notifications WHERE user_id = ? ORDER BY rowid DESC";

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
        databaseDriver.getWritableDatabase().execSQL("UPDATE notifications SET is_read = 1 WHERE notification_id = ?", new Object[]{id});
    }

    @Override
    public void deleteNotification(String id) {
        databaseDriver.getWritableDatabase().delete("notifications", "notification_id = ?", new String[]{id});
    }
}
