package com.startupidea.yipshare.condominium;

public final class JoinRequest {

    // computed_rating is stored in tenths, so 45 means 4.5 stars.
    private static final float RATING_SCALE = 10f;

    private final String userId;
    private final String username;
    private final String fullName;
    private final String phoneNumber;
    private final String condominiumId;
    private final String condominiumName;
    private final String unit;
    private final int computedRating;

    public JoinRequest(String userId, String username, String fullName, String phoneNumber, String condominiumId,
                       String condominiumName, String unit, int computedRating) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.condominiumId = condominiumId;
        this.condominiumName = condominiumName;
        this.unit = unit;
        this.computedRating = computedRating;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCondominiumId() {
        return condominiumId;
    }

    public String getCondominiumName() {
        return condominiumName;
    }

    public String getUnit() {
        return unit;
    }

    public float getRating() {
        return computedRating / RATING_SCALE;
    }
}
