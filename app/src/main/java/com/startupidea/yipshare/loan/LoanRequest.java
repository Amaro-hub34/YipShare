package com.startupidea.yipshare.loan;

public final class LoanRequest {

    // computed_rating is stored in tenths, so 45 means 4.5 stars.
    private static final float RATING_SCALE = 10f;

    private final String loanId;
    private final String itemName;
    private final String requesterId;
    private final String requesterUsername;
    private final int requesterRating;
    private final String requestText;

    public LoanRequest(String loanId, String itemName, String requesterId, String requesterUsername,
                       int requesterRating, String requestText) {
        this.loanId = loanId;
        this.itemName = itemName;
        this.requesterId = requesterId;
        this.requesterUsername = requesterUsername;
        this.requesterRating = requesterRating;
        this.requestText = requestText;
    }

    public String getLoanId() {
        return loanId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public float getRequesterRating() {
        return requesterRating / RATING_SCALE;
    }

    public String getRequestText() {
        return requestText;
    }
}
