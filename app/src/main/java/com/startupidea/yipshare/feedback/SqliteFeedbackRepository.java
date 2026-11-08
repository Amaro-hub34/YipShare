package com.startupidea.yipshare.feedback;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.DatabaseUnavailable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteFeedbackRepository implements FeedbackRepository {

    // Received = written by someone else in a loan where the user took part (as lender or borrower).
    private static final String getAllReviewsByUserId
            = "SELECT author.username, review.review_text, review.rating"
            + " FROM reviews review"
            + " JOIN loans loan ON loan.loan_id = review.loan_id"
            + " JOIN users author ON author.user_id = review.author_id"
            + " WHERE review.author_id <> ? AND (loan.lender_id = ? OR loan.user_id = ?)"
            + " ORDER BY review.rowid DESC";

    private static final String getAllReportsByUserId
            = "SELECT report.report_text, report.resolved_at IS NOT NULL"
            + " FROM reports report"
            + " JOIN loans loan ON loan.loan_id = report.loan_id"
            + " WHERE report.author_id <> ? AND (loan.lender_id = ? OR loan.user_id = ?)"
            + " ORDER BY report.rowid DESC";

    private static final String getReview
            = "SELECT 1 FROM reviews WHERE author_id = ? AND loan_id = ?";

    // users.computed_rating caches the average of the reviews the user has received.
    private static final String modifyComputedRating
            = "UPDATE users SET computed_rating = ("
            + " SELECT CAST(ROUND(AVG(review.rating)) AS INTEGER) FROM reviews review"
            + " JOIN loans loan ON loan.loan_id = review.loan_id"
            + " WHERE review.author_id <> ?1 AND (loan.lender_id = ?1 OR loan.user_id = ?1))"
            + " WHERE user_id = ?1";

    private final DatabaseDriver databaseDriver;

    public SqliteFeedbackRepository(DatabaseDriver databaseDriver) {
        this.databaseDriver = databaseDriver;
    }

    @Override
    public List<Review> getAllReviewsByUserId(String userId) {
        List<Review> reviews = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getAllReviewsByUserId, new String[]{userId, userId, userId})) {
            while (cursor.moveToNext()) {
                reviews.add(new Review(cursor.getString(0), cursor.getString(1), cursor.getInt(2)));
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return reviews;
    }

    @Override
    public List<Report> getAllReportsByUserId(String userId) {
        List<Report> reports = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getAllReportsByUserId, new String[]{userId, userId, userId})) {
            while (cursor.moveToNext()) {
                reports.add(new Report(cursor.getString(0), cursor.getInt(1) == 1));
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return reports;
    }

    @Override
    public boolean existsReview(String authorId, String loanId) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getReview, new String[]{authorId, loanId})) {
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void createReview(String authorId, String loanId, String reviewedId, int rating, String text) {
        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues review = new ContentValues();
                review.put("review_id", UUID.randomUUID().toString());
                review.put("loan_id", loanId);
                review.put("author_id", authorId);
                review.put("review_text", text);
                review.put("rating", rating);
                db.insertOrThrow("reviews", null, review);

                db.execSQL(modifyComputedRating, new Object[]{reviewedId});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void createReport(String authorId, String loanId, String text, String imageUri) {
        String reportId = UUID.randomUUID().toString();

        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues report = new ContentValues();
                report.put("report_id", reportId);
                report.put("loan_id", loanId);
                report.put("author_id", authorId);
                report.put("report_text", text);
                db.insertOrThrow("reports", null, report);

                if (imageUri != null) {
                    ContentValues evidence = new ContentValues();
                    evidence.put("report_evidence_id", UUID.randomUUID().toString());
                    evidence.put("report_id", reportId);
                    evidence.put("image", imageUri);
                    db.insertOrThrow("report_evidences", null, evidence);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }
}
