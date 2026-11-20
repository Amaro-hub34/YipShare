package com.startupidea.yipshare.feedback;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.ConflictException;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.ImageFileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteFeedbackRepository implements FeedbackRepository {

    private static final int RATING_SCALE = 10;

    private static final String getAllReviewsByUserId
            = "SELECT author.username, review.review_text, review.rating FROM reviews review"
            + " JOIN users author ON author.user_id = review.author_id"
            + " WHERE review.user_id = ? ORDER BY review.rowid DESC";

    private static final String getAllReportsByUserId
            = "SELECT report_text, state FROM reports WHERE user_id = ? ORDER BY rowid DESC";

    private static final String getAllReviewsByCondominiumId
            = "SELECT author.username || ' → ' || reviewed.username || ' · ' || article.name, review.review_text, review.rating"
            + " FROM reviews review"
            + " JOIN users author ON author.user_id = review.author_id"
            + " JOIN users reviewed ON reviewed.user_id = review.user_id"
            + " JOIN loans loan ON loan.loan_id = review.loan_id"
            + " JOIN articles article ON article.article_id = loan.article_id"
            + " WHERE article.condominium_id = ? ORDER BY review.rowid DESC";

    private static final String getAllReportsByCondominiumId
            = "SELECT report.report_id, author.username || ' → ' || reported.username || ': ' || report.report_text, report.state"
            + " FROM reports report"
            + " JOIN users author ON author.user_id = report.author_id"
            + " JOIN users reported ON reported.user_id = report.user_id"
            + " JOIN loans loan ON loan.loan_id = report.loan_id"
            + " JOIN articles article ON article.article_id = loan.article_id"
            + " WHERE article.condominium_id = ? ORDER BY report.rowid DESC";

    private static final String getOtherUserByLoanId
            = "SELECT CASE WHEN lender_id = ? THEN user_id ELSE lender_id END FROM loans WHERE loan_id = ?";

    private static final String getFeedbackByAuthor
            = "SELECT 1 FROM %s WHERE loan_id = ? AND author_id = ?";

    private static final String updateComputedRating
            = "UPDATE users SET computed_rating = (SELECT CAST(AVG(rating) AS INTEGER) FROM reviews WHERE user_id = ?) WHERE user_id = ?";

    private final DatabaseDriver databaseDriver;
    private final SessionRepository sessionRepository;
    private final Context context;

    public SqliteFeedbackRepository(Context context) {
        this.databaseDriver = DatabaseDriver.getInstance(context);
        this.sessionRepository = new SessionRepository(context);
        this.context = context.getApplicationContext();
    }

    @Override
    public List<Review> getAllReviewsByUserId(String userId) {
        return readReviews(getAllReviewsByUserId, userId);
    }

    @Override
    public List<Report> getAllReportsByUserId(String userId) {
        List<Report> reports = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllReportsByUserId, new String[]{userId})) {
            while (cursor.moveToNext()) reports.add(new Report(null, cursor.getString(0), ReportState.parse(cursor.getString(1))));
        }
        return reports;
    }

    @Override
    public void createReview(String loanId, int stars, String text) {
        String authorId = sessionRepository.getUserId();
        if (exists("reviews", loanId, authorId)) throw new ConflictException("User already reviewed this loan", R.string.error_ya_calificaste);
        String userId = otherUserOf(loanId, authorId);

        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("review_id", UUID.randomUUID().toString());
            values.put("loan_id", loanId);
            values.put("author_id", authorId);
            values.put("user_id", userId);
            values.put("review_text", text);
            values.put("rating", stars * RATING_SCALE);
            db.insertOrThrow("reviews", null, values);
            db.execSQL(updateComputedRating, new Object[]{userId, userId});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void createReport(String loanId, String text, List<String> imageUris) {
        String authorId = sessionRepository.getUserId();
        if (exists("reports", loanId, authorId)) throw new ConflictException("User already reported this loan", R.string.error_ya_reportado);
        List<String> images = new ArrayList<>();
        for (String imageUri : imageUris) images.add(ImageFileUtil.save(context, imageUri));

        String reportId = UUID.randomUUID().toString();
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("report_id", reportId);
            values.put("loan_id", loanId);
            values.put("author_id", authorId);
            values.put("user_id", otherUserOf(loanId, authorId));
            values.put("report_text", text);
            values.put("state", ReportState.PENDING.name());
            db.insertOrThrow("reports", null, values);
            for (String image : images) {
                ContentValues evidence = new ContentValues();
                evidence.put("report_evidence_id", UUID.randomUUID().toString());
                evidence.put("report_id", reportId);
                evidence.put("image", image);
                db.insertOrThrow("report_evidences", null, evidence);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public List<Review> getAllReviewsByCondominiumId(String condominiumId) {
        return readReviews(getAllReviewsByCondominiumId, condominiumId);
    }

    @Override
    public List<Report> getAllReportsByCondominiumId(String condominiumId) {
        List<Report> reports = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllReportsByCondominiumId, new String[]{condominiumId})) {
            while (cursor.moveToNext()) {
                reports.add(new Report(cursor.getString(0), cursor.getString(1), ReportState.parse(cursor.getString(2))));
            }
        }
        return reports;
    }

    @Override
    public void modifyReportState(String condominiumId, String reportId, ReportState state) {
        databaseDriver.getWritableDatabase().execSQL("UPDATE reports SET state = ? WHERE report_id = ?", new Object[]{state.name(), reportId});
    }

    private List<Review> readReviews(String query, String id) {
        List<Review> reviews = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(query, new String[]{id})) {
            while (cursor.moveToNext()) reviews.add(new Review(cursor.getString(0), cursor.getString(1), cursor.getInt(2)));
        }
        return reviews;
    }

    private boolean exists(String table, String loanId, String authorId) {
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(String.format(getFeedbackByAuthor, table), new String[]{loanId, authorId})) {
            return cursor.moveToFirst();
        }
    }

    private String otherUserOf(String loanId, String userId) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getOtherUserByLoanId, new String[]{userId, loanId})) {
            cursor.moveToFirst();
            return cursor.getString(0);
        }
    }
}
