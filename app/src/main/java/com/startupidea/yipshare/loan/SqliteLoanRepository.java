package com.startupidea.yipshare.loan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.database.DemoData;
import com.startupidea.yipshare.error.DatabaseUnavailable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteLoanRepository implements LoanRepository {

    // The other person is the borrower when the user lent the item, and the lender otherwise.
    private static final String selectLoans
            = "SELECT loan.loan_id, article.name, article.description, other.user_id, other.username,"
            + " loan.lender_id = ?1, strftime('%d/%m/%Y', loan.start_time), strftime('%d/%m/%Y', loan.return_time),"
            + " loan.state, other.phone_number"
            + " FROM loans loan"
            + " JOIN articles article ON article.article_id = loan.article_id"
            + " JOIN users other ON other.user_id = CASE WHEN loan.lender_id = ?1 THEN loan.user_id ELSE loan.lender_id END"
            + " WHERE (loan.lender_id = ?1 OR loan.user_id = ?1)";

    // Pending requests show up as notifications and rejected ones are history, so neither is listed as a loan.
    // Each condominium shows only its own loans and requests (the item's condominium decides).
    private static final String getAllLoansByUserId
            = selectLoans + " AND article.condominium_id = ?2 AND loan.state IN ('accepted', 'on_loan', 'finished')"
            + " ORDER BY loan.return_time";

    private static final String getAllLoanRequestsByLenderId
            = "SELECT loan.loan_id, article.name, requester.user_id, requester.username, requester.computed_rating,"
            + " loan.request_text"
            + " FROM loans loan"
            + " JOIN articles article ON article.article_id = loan.article_id"
            + " JOIN users requester ON requester.user_id = loan.user_id"
            + " WHERE loan.lender_id = ? AND article.condominium_id = ? AND article.deleted_at IS NULL"
            + " AND loan.state = 'pending'"
            + " ORDER BY loan.rowid DESC";

    private static final String getLoanByUserId
            = "SELECT 1 FROM loans WHERE lender_id = ?1 OR user_id = ?1 LIMIT 1";

    private static final String getLoanById
            = selectLoans + " AND loan.loan_id = ?2";

    // An accepted loan takes the item out of the available list until it is finished (or turned down).
    private static final String modifyArticleAvailability
            = "UPDATE articles SET availability = CASE ?1"
            + " WHEN 'accepted' THEN 'not_available' WHEN 'on_loan' THEN 'not_available' ELSE 'available' END"
            + " WHERE article_id = (SELECT article_id FROM loans WHERE loan_id = ?2)";

    private static final String getPendingRequest
            = "SELECT 1 FROM loans WHERE user_id = ? AND article_id = ? AND state = 'pending'";

    private static final String insertLoanRequest
            = "INSERT INTO loans (loan_id, lender_id, user_id, article_id, request_text, state, return_time)"
            + " VALUES (?, ?, ?, ?, ?, 'pending', date('now', 'localtime', ?))";

    private static final String modifyLoanStatus
            = "UPDATE loans SET state = ?1,"
            + " start_time = CASE WHEN ?1 = 'on_loan' THEN date('now', 'localtime') ELSE start_time END"
            + " WHERE loan_id = ?2";

    private static final String insertLoan
            = "INSERT INTO loans (loan_id, lender_id, user_id, article_id, request_text, state, return_time)"
            + " VALUES (?, ?, ?, ?, ?, ?, date('now', 'localtime', ?))";

    private final DatabaseDriver databaseDriver;
    private final Context context;

    public SqliteLoanRepository(DatabaseDriver databaseDriver, Context context) {
        this.databaseDriver = databaseDriver;
        this.context = context;
    }

    @Override
    public List<Loan> getAllLoansByUserId(String userId, String condominiumId) {
        List<Loan> loans = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllLoansByUserId, new String[]{userId, condominiumId})) {
            while (cursor.moveToNext()) loans.add(readLoan(cursor));
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return loans;
    }

    @Override
    public List<LoanRequest> getAllLoanRequestsByLenderId(String lenderId, String condominiumId) {
        List<LoanRequest> requests = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getAllLoanRequestsByLenderId, new String[]{lenderId, condominiumId})) {
            while (cursor.moveToNext()) {
                requests.add(new LoanRequest(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getInt(4), cursor.getString(5)));
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return requests;
    }

    @Override
    public boolean existsLoanByUserId(String userId) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getLoanByUserId, new String[]{userId})) {
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public Loan getLoanById(String loanId, String userId) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getLoanById, new String[]{userId, loanId})) {
            return cursor.moveToFirst() ? readLoan(cursor) : null;
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void modifyLoanStatus(String loanId, LoanStatus status) {
        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL(modifyLoanStatus, new Object[]{status.getValue(), loanId});
                db.execSQL(modifyArticleAvailability, new Object[]{status.getValue(), loanId});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public boolean existsPendingRequest(String userId, String articleId) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getPendingRequest, new String[]{userId, articleId})) {
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void createLoanRequest(String lenderId, String borrowerId, String articleId, String requestText, int days) {
        try {
            databaseDriver.getWritableDatabase().execSQL(insertLoanRequest, new Object[]{
                    UUID.randomUUID().toString(), lenderId, borrowerId, articleId, requestText, "+" + days + " days"});
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void createDemoLoans(String userId, String administeredId, String livedId) {
        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                createDemoLoan(db, administeredId, userId, DemoData.CARLOS, R.string.demo_taladro, R.string.demo_desc_taladro, LoanStatus.ACCEPTED, "+7 days");
                createDemoLoan(db, administeredId, DemoData.ANA, userId, R.string.demo_escalera, R.string.demo_desc_escalera, LoanStatus.ACCEPTED, "+2 days");
                createDemoLoan(db, administeredId, userId, DemoData.ANA, R.string.demo_bicicleta, R.string.demo_desc_bicicleta, LoanStatus.PENDING, "+3 days");
                createDemoLoan(db, livedId, userId, DemoData.LUIS, R.string.demo_bocina, R.string.demo_desc_bocina, LoanStatus.ACCEPTED, "+14 days");
                createDemoLoan(db, livedId, userId, DemoData.LUIS, R.string.demo_casa_campana, R.string.demo_desc_casa_campana, LoanStatus.PENDING, "+5 days");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    private void createDemoLoan(SQLiteDatabase db, String condominiumId, String lenderId, String borrowerId, int name,
                                int description, LoanStatus status, String duration) {
        String articleId = UUID.randomUUID().toString();

        ContentValues article = new ContentValues();
        article.put("article_id", articleId);
        article.put("condominium_id", condominiumId);
        article.put("lending_user", lenderId);
        article.put("name", context.getString(name));
        article.put("description", context.getString(description));
        article.put("state", "good");
        // A requested item is still available; an accepted loan takes it out of the list.
        article.put("availability", status == LoanStatus.PENDING ? "available" : "not_available");
        article.put("maximum_lend_time", 14);
        db.insertOrThrow("articles", null, article);

        String requestText = status == LoanStatus.PENDING ? context.getString(R.string.demo_texto_solicitud) : "";
        db.execSQL(insertLoan, new Object[]{
                UUID.randomUUID().toString(), lenderId, borrowerId, articleId, requestText, status.getValue(), duration});
    }

    private static Loan readLoan(Cursor cursor) {
        return new Loan(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                null,
                cursor.getString(3),
                cursor.getString(4),
                cursor.getInt(5) == 1,
                cursor.getString(6),
                cursor.getString(7),
                LoanStatus.parse(cursor.getString(8)),
                cursor.getString(9));
    }
}
