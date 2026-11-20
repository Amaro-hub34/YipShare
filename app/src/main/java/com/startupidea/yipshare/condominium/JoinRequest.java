package com.startupidea.yipshare.condominium;

public record JoinRequest(
        String userId,
        String username,
        String fullName,
        String phoneNumber,
        String condominiumId,
        String condominiumName,
        String unit,
        String suggestedUnitId,
        int computedRating) {

    private static final float RATING_SCALE = 10f;

    public float getRating() {
        return computedRating / RATING_SCALE;
    }
}
