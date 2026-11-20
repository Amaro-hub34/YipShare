package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.BadRequestException;
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
        if (stars <= 0) throw new BadRequestException("Rating is empty", R.string.error_calificacion_vacia);
        feedbackRepository.createReview(loan.id(), Math.max(1, Math.min(MAX_STARS, Math.round(stars))), text.trim());
    }

    public void createReport(String authorId, Loan loan, String text, List<String> imageUris) {
        if (text.trim().isEmpty()) throw new BadRequestException("Report text is empty", R.string.error_campo_obligatorio);
        feedbackRepository.createReport(loan.id(), text.trim(), imageUris);
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
        feedbackRepository.modifyReportState(condominiumId, report.id(), state);
    }
}
