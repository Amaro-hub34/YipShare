package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.BadRequestException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.loan.LoanStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class FeedbackServiceTest {

    private static final Loan LOAN = new Loan(
            "loan", "Drill", "Works well", null, "ana", "ana_lopez", true, "01/11/2026", "08/11/2026", LoanStatus.FINISHED, "+525500000000");

    private String reviewedLoan;
    private Integer savedStars;
    private String savedText;
    private ReportState savedState;

    private final FeedbackService service = new FeedbackService(new FeedbackRepository() {
        @Override
        public List<Review> getAllReviewsByUserId(String userId) {
            return null;
        }

        @Override
        public List<Report> getAllReportsByUserId(String userId) {
            return null;
        }

        @Override
        public void createReview(String loanId, int stars, String text) {
            reviewedLoan = loanId;
            savedStars = stars;
            savedText = text;
        }

        @Override
        public void createReport(String loanId, String text, List<String> imageUris) {
            savedText = text;
        }

        @Override
        public List<Review> getAllReviewsByCondominiumId(String condominiumId) {
            return null;
        }

        @Override
        public List<Report> getAllReportsByCondominiumId(String condominiumId) {
            return null;
        }

        @Override
        public void modifyReportState(String condominiumId, String reportId, ReportState state) {
            savedState = state;
        }
    });

    @Test
    public void reviewSendsWholeStarsOfTheLoan() {
        service.createReview("me", LOAN, 3.5f, "  Muy puntual ");

        assertEquals("loan", reviewedLoan);
        assertEquals(Integer.valueOf(4), savedStars);
        assertEquals("Muy puntual", savedText);
    }

    @Test
    public void reviewNeedsAtLeastOneStar() {
        assertThrows(BadRequestException.class, () -> service.createReview("me", LOAN, 0f, "text"));
        assertNull(savedStars);

        service.createReview("me", LOAN, 0.2f, "text");
        assertEquals(Integer.valueOf(1), savedStars);
    }

    @Test
    public void reportNeedsText() {
        assertThrows(BadRequestException.class, () -> service.createReport("me", LOAN, "   ", Collections.emptyList()));
        assertNull(savedText);

        service.createReport("me", LOAN, " Llegó tarde ", Collections.emptyList());
        assertEquals("Llegó tarde", savedText);
    }

    @Test
    public void adminChangesTheReportState() {
        service.modifyReportState("condo", new Report("r1", "text", ReportState.PENDING), ReportState.IN_REVIEW);

        assertEquals(ReportState.IN_REVIEW, savedState);
        assertEquals(ReportState.RESOLVED, ReportState.parse("resolved"));
    }

    @Test
    public void noSessionOrCondominiumMeansNoFeedback() {
        assertTrue(service.getAllReviewsByUserId(null).isEmpty());
        assertTrue(service.getAllReportsByUserId(null).isEmpty());
        assertTrue(service.getAllReviewsByCondominiumId(null).isEmpty());
        assertTrue(service.getAllReportsByCondominiumId(null).isEmpty());
    }
}
