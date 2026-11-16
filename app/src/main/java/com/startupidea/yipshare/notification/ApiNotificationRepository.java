package com.startupidea.yipshare.notification;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.ServerUnavailable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiNotificationRepository implements NotificationRepository {

    private final ApiClient apiClient;

    public ApiNotificationRepository(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<Notification> getAllNotifications() {
        try {
            JSONArray array = apiClient.getArray("/notifications/me");
            List<Notification> notifications = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                notifications.add(new Notification(json.getString("id"), json.optString("header"),
                        json.optString("body"), json.optBoolean("read")));
            }
            return notifications;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void markAsRead(String id) {
        apiClient.post("/notifications/me/" + id + "/read", null);
    }

    @Override
    public void deleteNotification(String id) {
        apiClient.delete("/notifications/me/" + id);
    }
}
