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
            if (!notification.isRead()) unread++;
        }
        return unread;
    }

    public void markAsRead(Notification notification) {
        if (!notification.isRead()) notificationRepository.markAsRead(notification.getId());
    }

    public void deleteNotification(Notification notification) {
        notificationRepository.deleteNotification(notification.getId());
    }
}
