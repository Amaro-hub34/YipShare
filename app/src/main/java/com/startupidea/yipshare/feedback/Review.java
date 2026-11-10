package com.startupidea.yipshare.feedback;

public final class Review {

    private static final float RATING_SCALE = 10f;

    private final String authorUsername;
    private final String text;
    private final int rating;

    public Review(String authorUsername, String text, int rating) {
        this.authorUsername = authorUsername;
        this.text = text;
        this.rating = rating;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getText() {
        return text;
    }

    public float getRating() {
        return rating / RATING_SCALE;
    }
}
