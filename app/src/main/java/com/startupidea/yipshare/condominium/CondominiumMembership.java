package com.startupidea.yipshare.condominium;

public final class CondominiumMembership {

    private final String condominiumId;
    private final String name;
    private final boolean admin;
    private final int pendingNotifications;
    private final String code;

    public CondominiumMembership(String condominiumId, String name, boolean admin, int pendingNotifications, String code) {
        this.condominiumId = condominiumId;
        this.name = name;
        this.admin = admin;
        this.pendingNotifications = pendingNotifications;
        this.code = code;
    }

    public String getCondominiumId() {
        return condominiumId;
    }

    public String getName() {
        return name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public int getPendingNotifications() {
        return pendingNotifications;
    }

    public String getCode() {
        return code;
    }
}
