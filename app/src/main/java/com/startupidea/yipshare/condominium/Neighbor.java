package com.startupidea.yipshare.condominium;

public record Neighbor(
        String userId,
        String username,
        String unit,
        String phoneNumber,
        int computedRating,
        boolean admin,
        boolean canRequestLoans) {

    private static final float RATING_SCALE = 10f;

    public float getRating() {
        return computedRating / RATING_SCALE;
    }

    public boolean canRequestLoans() {
        return canRequestLoans;
    }
}
