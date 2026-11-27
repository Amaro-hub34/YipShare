package com.startupidea.yipshare.feedback;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseContract.Articles;
import com.startupidea.yipshare.database.DatabaseContract.Loans;
import com.startupidea.yipshare.database.DatabaseContract.ReportEvidences;
import com.startupidea.yipshare.database.DatabaseContract.Reports;
import com.startupidea.yipshare.database.DatabaseContract.Reviews;
import com.startupidea.yipshare.database.DatabaseContract.Users;
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
            = "SELECT w." + Users.USERNAME + ", r." + Reviews.REVIEW_TEXT + ", r." + Reviews.RATING
            + " FROM " + Reviews.TABLE + " r"
            + " JOIN " + Users.TABLE + " w ON w." + Users.ID + " = r." + Reviews.AUTHOR_ID
            + " WHERE r." + Reviews.USER_ID + " = ? ORDER BY r.rowid DESC";

    private static final String getAllReportsByUserId
            = "SELECT " + Reports.REPORT_TEXT + ", " + Reports.STATE + " FROM " + Reports.TABLE
            + " WHERE " + Reports.USER_ID + " = ? ORDER BY rowid DESC";

    private static final String getAllReviewsByCondominiumId
            = "SELECT w." + Users.USERNAME + " || ' → ' || t." + Users.USERNAME + " || ' · ' || a." + Articles.NAME + ","
            + " r." + Reviews.REVIEW_TEXT + ", r." + Reviews.RATING
            + " FROM " + Reviews.TABLE + " r"
            + " JOIN " + Users.TABLE + " w ON w." + Users.ID + " = r." + Reviews.AUTHOR_ID
            + " JOIN " + Users.TABLE + " t ON t." + Users.ID + " = r." + Reviews.USER_ID
            + " JOIN " + Loans.TABLE + " l ON l." + Loans.ID + " = r." + Reviews.LOAN_ID
            + " JOIN " + Articles.TABLE + " a ON a." + Articles.ID + " = l." + Loans.ARTICLE_ID
            + " WHERE a." + Articles.CONDOMINIUM_ID + " = ? ORDER BY r.rowid DESC";

    private static final String getAllReportsByCondominiumId
            = "SELECT r." + Reports.ID + ", w." + Users.USERNAME + " || ' → ' || t." + Users.USERNAME + " || ': ' || r." + Reports.REPORT_TEXT + ","
            + " r." + Reports.STATE
            + " FROM " + Reports.TABLE + " r"
            + " JOIN " + Users.TABLE + " w ON w." + Users.ID + " = r." + Reports.AUTHOR_ID
            + " JOIN " + Users.TABLE + " t ON t." + Users.ID + " = r." + Reports.USER_ID
            + " JOIN " + Loans.TABLE + " l ON l." + Loans.ID + " = r." + Reports.LOAN_ID
            + " JOIN " + Articles.TABLE + " a ON a." + Articles.ID + " = l." + Loans.ARTICLE_ID
            + " WHERE a." + Articles.CONDOMINIUM_ID + " = ? ORDER BY r.rowid DESC";

    private static final String getOtherUserByLoanId
            = "SELECT CASE WHEN " + Loans.LENDER_ID + " = ? THEN " + Loans.USER_ID + " ELSE " + Loans.LENDER_ID + " END"
            + " FROM " + Loans.TABLE + " WHERE " + Loans.ID + " = ?";

    private static final String getFeedbackByAuthor
            = "SELECT 1 FROM %s WHERE " + Reviews.LOAN_ID + " = ? AND " + Reviews.AUTHOR_ID + " = ?";

    private static final String updateComputedRating
            = "UPDATE " + Users.TABLE + " SET " + Users.COMPUTED_RATING
            + " = (SELECT CAST(AVG(" + Reviews.RATING + ") AS INTEGER) FROM " + Reviews.TABLE + " WHERE " + Reviews.USER_ID + " = ?)"
            + " WHERE " + Users.ID + " = ?";

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
        if (exists(Reviews.TABLE, loanId, authorId)) throw new ConflictException("User already reviewed this loan", R.string.error_ya_calificaste);
        String userId = otherUserOf(loanId, authorId);

        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(Reviews.ID, UUID.randomUUID().toString());
            values.put(Reviews.LOAN_ID, loanId);
            values.put(Reviews.AUTHOR_ID, authorId);
            values.put(Reviews.USER_ID, userId);
            values.put(Reviews.REVIEW_TEXT, text);
            values.put(Reviews.RATING, stars * RATING_SCALE);
            db.insertOrThrow(Reviews.TABLE, null, values);
            db.execSQL(updateComputedRating, new Object[]{userId, userId});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void createReport(String loanId, String text, List<String> imageUris) {
        String authorId = sessionRepository.getUserId();
        if (exists(Reports.TABLE, loanId, authorId)) throw new ConflictException("User already reported this loan", R.string.error_ya_reportado);
        List<String> images = new ArrayList<>();
        for (String imageUri : imageUris) images.add(ImageFileUtil.save(context, imageUri));

        String reportId = UUID.randomUUID().toString();
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(Reports.ID, reportId);
            values.put(Reports.LOAN_ID, loanId);
            values.put(Reports.AUTHOR_ID, authorId);
            values.put(Reports.USER_ID, otherUserOf(loanId, authorId));
            values.put(Reports.REPORT_TEXT, text);
            values.put(Reports.STATE, ReportState.PENDING.name());
            db.insertOrThrow(Reports.TABLE, null, values);
            for (String image : images) {
                ContentValues evidence = new ContentValues();
                evidence.put(ReportEvidences.ID, UUID.randomUUID().toString());
                evidence.put(ReportEvidences.REPORT_ID, reportId);
                evidence.put(ReportEvidences.IMAGE, image);
                db.insertOrThrow(ReportEvidences.TABLE, null, evidence);
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
        ContentValues values = new ContentValues();
        values.put(Reports.STATE, state.name());
        databaseDriver.getWritableDatabase().update(Reports.TABLE, values, Reports.ID + " = ?", new String[]{reportId});
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
