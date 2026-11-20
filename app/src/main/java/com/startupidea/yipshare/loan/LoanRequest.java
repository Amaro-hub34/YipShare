package com.startupidea.yipshare.loan;

public record LoanRequest(
        String loanId,
        String itemName,
        String requesterId,
        String requesterUsername,
        int requesterRating,
        String requestText) {

    private static final float RATING_SCALE = 10f;

    public float getRequesterRating() {
        return requesterRating / RATING_SCALE;
    }
}
