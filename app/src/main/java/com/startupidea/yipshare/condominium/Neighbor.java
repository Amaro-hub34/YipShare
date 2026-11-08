package com.startupidea.yipshare.condominium;

public final class Neighbor {

    // computed_rating is stored in tenths, so 45 means 4.5 stars.
    private static final float RATING_SCALE = 10f;

    private final String userId;
    private final String username;
    private final String unit;
    private final String phoneNumber;
    private final int computedRating;
    private final boolean admin;
    private final boolean canRequestLoans;

    public Neighbor(String userId, String username, String unit, String phoneNumber, int computedRating,
                    boolean admin, boolean canRequestLoans) {
        this.userId = userId;
        this.username = username;
        this.unit = unit;
        this.phoneNumber = phoneNumber;
        this.computedRating = computedRating;
        this.admin = admin;
        this.canRequestLoans = canRequestLoans;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUnit() {
        return unit;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public float getRating() {
        return computedRating / RATING_SCALE;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean canRequestLoans() {
        return canRequestLoans;
    }
}
