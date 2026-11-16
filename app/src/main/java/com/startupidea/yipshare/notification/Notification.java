package com.startupidea.yipshare.notification;

public final class Notification {

    private final String id;
    private final String header;
    private final String body;
    private final boolean read;

    public Notification(String id, String header, String body, boolean read) {
        this.id = id;
        this.header = header;
        this.body = body;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public boolean isRead() {
        return read;
    }
}
