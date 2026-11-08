package com.startupidea.yipshare.feedback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.loan.LoanStatus;

import org.junit.Test;

import java.util.List;

public class FeedbackServiceTest {

    private static final Loan LOAN = new Loan(
            "loan", "Drill", "Works well", null, "ana", "ana_lopez", true, "01/11/2026", "08/11/2026", LoanStatus.FINISHED, "+525500000000");

    private boolean reviewExists;
    private String reviewed;
    private Integer savedRating;
    private String savedText;

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
        public boolean existsReview(String authorId, String loanId) {
            return reviewExists;
        }

        @Override
        public void createReview(String authorId, String loanId, String reviewedId, int rating, String text) {
            reviewed = reviewedId;
            savedRating = rating;
            savedText = text;
        }

        @Override
        public void createReport(String authorId, String loanId, String text, String imageUri) {
            savedText = text;
        }
    });

    @Test
    public void reviewGoesToTheOtherPersonInTenths() {
        service.createReview("me", LOAN, 4f, "  Muy puntual ");

        assertEquals("ana", reviewed);
        assertEquals(Integer.valueOf(40), savedRating);
        assertEquals("Muy puntual", savedText);
    }

    @Test
    public void reviewNeedsAtLeastOneStar() {
        assertThrows(EmptyRating.class, () -> service.createReview("me", LOAN, 0f, "text"));
        assertNull(savedRating);
    }

    @Test
    public void reviewsOncePerLoan() {
        reviewExists = true;

        assertThrows(AlreadyReviewed.class, () -> service.createReview("me", LOAN, 5f, "text"));
        assertNull(savedRating);
    }

    @Test
    public void reportNeedsText() {
        assertThrows(EmptyReportText.class, () -> service.createReport("me", LOAN, "   ", null));
        assertNull(savedText);

        service.createReport("me", LOAN, " Llegó tarde ", null);
        assertEquals("Llegó tarde", savedText);
    }

    @Test
    public void noSessionMeansNoFeedback() {
        assertTrue(service.getAllReviewsByUserId(null).isEmpty());
        assertTrue(service.getAllReportsByUserId(null).isEmpty());
    }
}
