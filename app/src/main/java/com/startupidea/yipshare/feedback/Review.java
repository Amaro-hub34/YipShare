package com.startupidea.yipshare.feedback;

public record Review(String authorUsername, String text, int rating) {

    private static final float RATING_SCALE = 10f;

    public float getRating() {
        return rating / RATING_SCALE;
    }
}
