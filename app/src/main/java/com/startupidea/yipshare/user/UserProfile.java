package com.startupidea.yipshare.user;

public final class UserProfile {

    private static final float RATING_SCALE = 10f;

    private final String id;
    private final String username;
    private final String firstNames;
    private final String paternalLastName;
    private final String maternalLastName;
    private final String phoneNumber;
    private final String email;
    private final int computedRating;

    public UserProfile(
            String id,
            String username,
            String firstNames,
            String paternalLastName,
            String maternalLastName,
            String phoneNumber,
            String email,
            int computedRating) {
        this.id = id;
        this.username = username;
        this.firstNames = firstNames;
        this.paternalLastName = paternalLastName;
        this.maternalLastName = maternalLastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.computedRating = computedRating;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getPaternalLastName() {
        return paternalLastName;
    }

    public String getMaternalLastName() {
        return maternalLastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public float getRating() {
        return computedRating / RATING_SCALE;
    }
}
