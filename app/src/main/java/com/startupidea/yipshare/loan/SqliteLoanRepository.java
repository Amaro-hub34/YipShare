package com.startupidea.yipshare.loan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.BadRequestException;
import com.startupidea.yipshare.error.ConflictException;
import com.startupidea.yipshare.error.UnauthorizedException;
import com.startupidea.yipshare.item.ItemAvailability;
import com.startupidea.yipshare.user.SessionRepository;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SqliteLoanRepository implements LoanRepository {

    private static final String CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private static final String selectLoan
            = "SELECT loan.loan_id, article.name, article.description, article.image, other.user_id, other.username,"
            + " loan.lender_id = ?, loan.start_time, loan.return_time, loan.state, other.phone_number"
            + " FROM loans loan"
            + " JOIN articles article ON article.article_id = loan.article_id"
            + " JOIN users other ON other.user_id = CASE WHEN loan.lender_id = ? THEN loan.user_id ELSE loan.lender_id END";

    private static final String getAllLoansByCondominiumId
            = selectLoan + " WHERE article.condominium_id = ? AND (loan.lender_id = ? OR loan.user_id = ?)"
            + " ORDER BY loan.rowid DESC";

    private static final String getLoanById = selectLoan + " WHERE loan.loan_id = ?";

    private static final String getAllLoanRequestsByCondominiumId
            = "SELECT loan.loan_id, article.name, requester.user_id, requester.username, requester.computed_rating,"
            + " loan.request_text FROM loans loan"
            + " JOIN articles article ON article.article_id = loan.article_id"
            + " JOIN users requester ON requester.user_id = loan.user_id"
            + " WHERE article.condominium_id = ? AND loan.lender_id = ? AND loan.state = 'PENDING'"
            + " AND article.deleted = 0 AND requester.state = 'active'"
            + " ORDER BY loan.rowid";

    private static final String getArticleForRequest
            = "SELECT article.lending_user, article.availability,"
            + " EXISTS (SELECT 1 FROM condominium_members member WHERE member.user_id = ?"
            + " AND member.condominium_id = article.condominium_id AND member.can_request_loans = 0),"
            + " EXISTS (SELECT 1 FROM loans loan WHERE loan.article_id = article.article_id AND loan.user_id = ?"
            + " AND loan.state = 'PENDING')"
            + " FROM articles article WHERE article.article_id = ? AND article.deleted = 0";

    private static final String getLoanState
            = "SELECT loan.state, loan.article_id, loan.user_id, loan.days, loan.code, article.name, lender.username"
            + " FROM loans loan JOIN articles article ON article.article_id = loan.article_id"
            + " JOIN users lender ON lender.user_id = loan.lender_id WHERE loan.loan_id = ?";

    private final DatabaseDriver databaseDriver;
    private final SessionRepository sessionRepository;
    private final Context context;
    private final SecureRandom random = new SecureRandom();

    public SqliteLoanRepository(Context context) {
        this.databaseDriver = DatabaseDriver.getInstance(context);
        this.sessionRepository = new SessionRepository(context);
        this.context = context.getApplicationContext();
    }

    @Override
    public List<Loan> getAllLoansByCondominiumId(String condominiumId) {
        String userId = sessionRepository.getUserId();
        List<Loan> loans = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getAllLoansByCondominiumId, new String[]{userId, userId, condominiumId, userId, userId})) {
            while (cursor.moveToNext()) loans.add(readLoan(cursor));
        }
        return loans;
    }

    @Override
    public List<LoanRequest> getAllLoanRequestsByCondominiumId(String condominiumId) {
        List<LoanRequest> requests = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getAllLoanRequestsByCondominiumId, new String[]{condominiumId, sessionRepository.getUserId()})) {
            while (cursor.moveToNext()) {
                requests.add(new LoanRequest(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getInt(4), cursor.getString(5)));
            }
        }
        return requests;
    }

    @Override
    public void createLoanRequest(String articleId, String requestText, int days) {
        String userId = sessionRepository.getUserId();
        String lenderId;
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getArticleForRequest, new String[]{userId, userId, articleId})) {
            if (!cursor.moveToFirst() || !ItemAvailability.AVAILABLE.name().equals(cursor.getString(1))) throw notAvailable();
            if (cursor.getInt(2) == 1) throw new UnauthorizedException("Condominium admin took away this user's loan access", R.string.error_sin_acceso_prestamos);
            if (cursor.getInt(3) == 1) throw new ConflictException("User already has a pending request for this item", R.string.error_ya_solicitado);
            lenderId = cursor.getString(0);
        }

        ContentValues values = new ContentValues();
        values.put("loan_id", UUID.randomUUID().toString());
        values.put("article_id", articleId);
        values.put("lender_id", lenderId);
        values.put("user_id", userId);
        values.put("request_text", requestText);
        values.put("days", days);
        databaseDriver.getWritableDatabase().insertOrThrow("loans", null, values);
    }

    @Override
    public void modifyLoanStatus(String loanId, LoanStatus answer) {
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        db.beginTransaction();
        try {
            String articleId;
            String borrowerId;
            String articleName;
            String lenderName;
            try (Cursor cursor = db.rawQuery(getLoanState, new String[]{loanId})) {
                if (!cursor.moveToFirst() || LoanStatus.parse(cursor.getString(0)) != LoanStatus.PENDING) throw new ConflictException("Loan request was already answered or is not addressed to this user", R.string.error_solicitud_respondida);
                articleId = cursor.getString(1);
                borrowerId = cursor.getString(2);
                articleName = cursor.getString(5);
                lenderName = cursor.getString(6);
            }

            db.execSQL("UPDATE loans SET state = ? WHERE loan_id = ?", new Object[]{answer.name(), loanId});
            if (answer == LoanStatus.ACCEPTED) {
                db.execSQL("UPDATE articles SET availability = ? WHERE article_id = ?", new Object[]{ItemAvailability.PENDING.name(), articleId});
                db.execSQL("UPDATE loans SET state = ? WHERE article_id = ? AND state = ?",
                        new Object[]{LoanStatus.REJECTED.name(), articleId, LoanStatus.PENDING.name()});
                databaseDriver.createNotification(borrowerId, context.getString(R.string.notif_prestamo_aceptado),
                        context.getString(R.string.notif_prestamo_aceptado_cuerpo, lenderName, articleName));
            } else {
                databaseDriver.createNotification(borrowerId, context.getString(R.string.notif_prestamo_rechazado),
                        context.getString(R.string.notif_prestamo_rechazado_cuerpo, lenderName, articleName));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public String createCode(String loanId) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) code.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
        databaseDriver.getWritableDatabase().execSQL("UPDATE loans SET code = ? WHERE loan_id = ?", new Object[]{code.toString(), loanId});
        return code.toString();
    }

    @Override
    public Loan confirmCode(String loanId, String code) {
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        db.beginTransaction();
        try {
            LoanStatus status;
            String articleId;
            int days;
            try (Cursor cursor = db.rawQuery(getLoanState, new String[]{loanId})) {
                if (!cursor.moveToFirst()) throw notActive();
                status = LoanStatus.parse(cursor.getString(0));
                if (!status.acceptsCode()) throw notActive();
                if (!code.equals(cursor.getString(4))) throw new BadRequestException("Loan code is wrong or must have 6 digits", R.string.error_codigo_longitud);
                articleId = cursor.getString(1);
                days = cursor.getInt(3);
            }

            if (status == LoanStatus.ACCEPTED) {
                Calendar calendar = Calendar.getInstance();
                String start = toDate(calendar);
                calendar.add(Calendar.DAY_OF_MONTH, days);
                db.execSQL("UPDATE loans SET state = ?, code = NULL, start_time = ?, return_time = ? WHERE loan_id = ?",
                        new Object[]{LoanStatus.ON_LOAN.name(), start, toDate(calendar), loanId});
                db.execSQL("UPDATE articles SET availability = ? WHERE article_id = ?", new Object[]{ItemAvailability.NOT_AVAILABLE.name(), articleId});
            } else {
                db.execSQL("UPDATE loans SET state = ?, code = NULL WHERE loan_id = ?", new Object[]{LoanStatus.FINISHED.name(), loanId});
                db.execSQL("UPDATE articles SET availability = ? WHERE article_id = ?", new Object[]{ItemAvailability.AVAILABLE.name(), articleId});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        String userId = sessionRepository.getUserId();
        try (Cursor cursor = db.rawQuery(getLoanById, new String[]{userId, userId, loanId})) {
            cursor.moveToFirst();
            return readLoan(cursor);
        }
    }

    private static Loan readLoan(Cursor cursor) {
        return new Loan(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6) == 1,
                cursor.getString(7),
                cursor.getString(8),
                LoanStatus.parse(cursor.getString(9)),
                cursor.getString(10));
    }

    private static String toDate(Calendar calendar) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
    }

    private static ConflictException notAvailable() {
        return new ConflictException("Only an available item can be modified or deleted", R.string.error_articulo_no_disponible);
    }

    private static ConflictException notActive() {
        return new ConflictException("Loan does not take codes in its current state", R.string.error_prestamo_no_activo);
    }
}
