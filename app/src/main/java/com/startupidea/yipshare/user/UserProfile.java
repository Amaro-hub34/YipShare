package com.startupidea.yipshare.user;

public record UserProfile(
        String id,
        String username,
        String firstNames,
        String paternalLastName,
        String maternalLastName,
        String phoneNumber,
        String email,
        int computedRating,
        String profilePictureUri) {

    private static final float RATING_SCALE = 10f;

    public float getRating() {
        return computedRating / RATING_SCALE;
    }
}
