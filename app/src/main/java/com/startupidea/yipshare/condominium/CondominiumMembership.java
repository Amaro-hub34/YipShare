package com.startupidea.yipshare.condominium;

/** A condominium the user belongs to and the role they have there; one user can be admin in one and resident in another. */
public final class CondominiumMembership {

    private final String condominiumId;
    private final String name;
    private final boolean admin;
    private final int pendingNotifications;

    public CondominiumMembership(String condominiumId, String name, boolean admin, int pendingNotifications) {
        this.condominiumId = condominiumId;
        this.name = name;
        this.admin = admin;
        this.pendingNotifications = pendingNotifications;
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

    /** Loan requests for the user's items there, plus join requests when the user is its admin. */
    public int getPendingNotifications() {
        return pendingNotifications;
    }
}
