package com.startupidea.yipshare.feedback;

import java.util.List;

public interface FeedbackRepository {

    List<Review> getAllReviewsByUserId(String userId);

    List<Report> getAllReportsByUserId(String userId);

    void createReview(String loanId, int stars, String text);

    void createReport(String loanId, String text, List<String> imageUris);

    List<Review> getAllReviewsByCondominiumId(String condominiumId);

    List<Report> getAllReportsByCondominiumId(String condominiumId);

    void modifyReportState(String condominiumId, String reportId, ReportState state);
}
