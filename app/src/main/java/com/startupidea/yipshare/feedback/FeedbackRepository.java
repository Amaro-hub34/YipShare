package com.startupidea.yipshare.feedback;

import java.util.List;

public interface FeedbackRepository {

    List<Review> getAllReviewsByUserId(String userId);

    List<Report> getAllReportsByUserId(String userId);

    boolean existsReview(String authorId, String loanId);

    void createReview(String authorId, String loanId, String reviewedId, int rating, String text);

    void createReport(String authorId, String loanId, String text, String imageUri);
}
