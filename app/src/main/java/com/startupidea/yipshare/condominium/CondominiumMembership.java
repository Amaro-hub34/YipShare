package com.startupidea.yipshare.condominium;

public record CondominiumMembership(
        String condominiumId,
        String name,
        boolean admin,
        int pendingNotifications,
        String code) {
}
