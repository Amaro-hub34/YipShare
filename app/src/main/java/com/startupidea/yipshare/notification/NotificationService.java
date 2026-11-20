package com.startupidea.yipshare.notification;

import java.util.List;

public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.getAllNotifications();
    }

    public int countUnread() {
        int unread = 0;
        for (Notification notification : notificationRepository.getAllNotifications()) {
            if (!notification.read()) unread++;
        }
        return unread;
    }

    public void markAsRead(Notification notification) {
        if (!notification.read()) notificationRepository.markAsRead(notification.id());
    }

    public void deleteNotification(Notification notification) {
        notificationRepository.deleteNotification(notification.id());
    }
}
