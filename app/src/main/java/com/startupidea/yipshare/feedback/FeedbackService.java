package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.loan.Loan;

import java.util.Collections;
import java.util.List;

public class FeedbackService {

    private static final int MAX_STARS = 5;

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
        if (stars <= 0) throw new EmptyRating();
        feedbackRepository.createReview(loan.getId(), Math.max(1, Math.min(MAX_STARS, Math.round(stars))), text.trim());
    }

    public void createReport(String authorId, Loan loan, String text, String imageUri) {
        if (text.trim().isEmpty()) throw new EmptyReportText();
        feedbackRepository.createReport(loan.getId(), text.trim(), imageUri);
    }

    public List<Review> getAllReviewsByCondominiumId(String condominiumId) {
        if (condominiumId == null) return Collections.emptyList();
        return feedbackRepository.getAllReviewsByCondominiumId(condominiumId);
    }

    public List<Report> getAllReportsByCondominiumId(String condominiumId) {
        if (condominiumId == null) return Collections.emptyList();
        return feedbackRepository.getAllReportsByCondominiumId(condominiumId);
    }

    public void modifyReportState(String condominiumId, Report report, ReportState state) {
        feedbackRepository.modifyReportState(condominiumId, report.getId(), state);
    }
}
