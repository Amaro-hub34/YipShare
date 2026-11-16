package com.startupidea.yipshare.notification;

import java.util.List;

public interface NotificationRepository {

    List<Notification> getAllNotifications();

    void markAsRead(String id);

    void deleteNotification(String id);
}
