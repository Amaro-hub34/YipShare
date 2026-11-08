package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.loan.Loan;

import java.util.Collections;
import java.util.List;

public class FeedbackService {

    // The rating bar gives stars; the database keeps tenths (1 to 50).
    private static final int RATING_SCALE = 10;

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public List<Review> getAllReviewsByUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        return feedbackRepository.getAllReviewsByUserId(userId);
    }

    public List<Report> getAllReportsByUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        return feedbackRepository.getAllReportsByUserId(userId);
    }

    public void createReview(String authorId, Loan loan, float stars, String text) {
        int rating = Math.round(stars * RATING_SCALE);
        if (rating < 1) throw new EmptyRating();
        if (feedbackRepository.existsReview(authorId, loan.getId())) throw new AlreadyReviewed();

        feedbackRepository.createReview(authorId, loan.getId(), loan.getOtherUserId(), Math.min(rating, 50), text.trim());
    }

    public void createReport(String authorId, Loan loan, String text, String imageUri) {
        if (text.trim().isEmpty()) throw new EmptyReportText();
        feedbackRepository.createReport(authorId, loan.getId(), text.trim(), imageUri);
    }
}
