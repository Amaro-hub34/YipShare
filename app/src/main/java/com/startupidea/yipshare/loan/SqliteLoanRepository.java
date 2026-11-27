package com.startupidea.yipshare.loan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseContract.Articles;
import com.startupidea.yipshare.database.DatabaseContract.Loans;
import com.startupidea.yipshare.database.DatabaseContract.Members;
import com.startupidea.yipshare.database.DatabaseContract.Users;
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
            = "SELECT l." + Loans.ID + ", a." + Articles.NAME + ", a." + Articles.DESCRIPTION + ", a." + Articles.IMAGE + ","
            + " o." + Users.ID + ", o." + Users.USERNAME + ", l." + Loans.LENDER_ID + " = ?, l." + Loans.START_TIME + ","
            + " l." + Loans.RETURN_TIME + ", l." + Loans.STATE + ", o." + Users.PHONE_NUMBER
            + " FROM " + Loans.TABLE + " l"
            + " JOIN " + Articles.TABLE + " a ON a." + Articles.ID + " = l." + Loans.ARTICLE_ID
            + " JOIN " + Users.TABLE + " o ON o." + Users.ID
            + " = CASE WHEN l." + Loans.LENDER_ID + " = ? THEN l." + Loans.USER_ID + " ELSE l." + Loans.LENDER_ID + " END";

    private static final String getAllLoansByCondominiumId
            = selectLoan + " WHERE a." + Articles.CONDOMINIUM_ID + " = ? AND (l." + Loans.LENDER_ID + " = ? OR l." + Loans.USER_ID + " = ?)"
            + " ORDER BY l.rowid DESC";

    private static final String getLoanById = selectLoan + " WHERE l." + Loans.ID + " = ?";

    private static final String getAllLoanRequestsByCondominiumId
            = "SELECT l." + Loans.ID + ", a." + Articles.NAME + ", r." + Users.ID + ", r." + Users.USERNAME + ","
            + " r." + Users.COMPUTED_RATING + ", l." + Loans.REQUEST_TEXT
            + " FROM " + Loans.TABLE + " l"
            + " JOIN " + Articles.TABLE + " a ON a." + Articles.ID + " = l." + Loans.ARTICLE_ID
            + " JOIN " + Users.TABLE + " r ON r." + Users.ID + " = l." + Loans.USER_ID
            + " WHERE a." + Articles.CONDOMINIUM_ID + " = ? AND l." + Loans.LENDER_ID + " = ? AND l." + Loans.STATE + " = 'PENDING'"
            + " AND a." + Articles.DELETED + " = 0 AND r." + Users.STATE + " = 'active'"
            + " ORDER BY l.rowid";

    private static final String getArticleForRequest
            = "SELECT a." + Articles.LENDING_USER + ", a." + Articles.AVAILABILITY + ","
            + " EXISTS (SELECT 1 FROM " + Members.TABLE + " m WHERE m." + Members.USER_ID + " = ?"
            + " AND m." + Members.CONDOMINIUM_ID + " = a." + Articles.CONDOMINIUM_ID + " AND m." + Members.CAN_REQUEST_LOANS + " = 0),"
            + " EXISTS (SELECT 1 FROM " + Loans.TABLE + " l WHERE l." + Loans.ARTICLE_ID + " = a." + Articles.ID
            + " AND l." + Loans.USER_ID + " = ? AND l." + Loans.STATE + " = 'PENDING')"
            + " FROM " + Articles.TABLE + " a WHERE a." + Articles.ID + " = ? AND a." + Articles.DELETED + " = 0";

    private static final String getLoanState
            = "SELECT l." + Loans.STATE + ", l." + Loans.ARTICLE_ID + ", l." + Loans.USER_ID + ", l." + Loans.DAYS + ","
            + " l." + Loans.CODE + ", a." + Articles.NAME + ", d." + Users.USERNAME
            + " FROM " + Loans.TABLE + " l"
            + " JOIN " + Articles.TABLE + " a ON a." + Articles.ID + " = l." + Loans.ARTICLE_ID
            + " JOIN " + Users.TABLE + " d ON d." + Users.ID + " = l." + Loans.LENDER_ID
            + " WHERE l." + Loans.ID + " = ?";

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
        values.put(Loans.ID, UUID.randomUUID().toString());
        values.put(Loans.ARTICLE_ID, articleId);
        values.put(Loans.LENDER_ID, lenderId);
        values.put(Loans.USER_ID, userId);
        values.put(Loans.REQUEST_TEXT, requestText);
        values.put(Loans.DAYS, days);
        databaseDriver.getWritableDatabase().insertOrThrow(Loans.TABLE, null, values);
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

            ContentValues values = new ContentValues();
            values.put(Loans.STATE, answer.name());
            db.update(Loans.TABLE, values, Loans.ID + " = ?", new String[]{loanId});
            if (answer == LoanStatus.ACCEPTED) {
                modifyAvailability(db, articleId, ItemAvailability.PENDING);
                values.put(Loans.STATE, LoanStatus.REJECTED.name());
                db.update(Loans.TABLE, values, Loans.ARTICLE_ID + " = ? AND " + Loans.STATE + " = ?",
                        new String[]{articleId, LoanStatus.PENDING.name()});
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
        ContentValues values = new ContentValues();
        values.put(Loans.CODE, code.toString());
        databaseDriver.getWritableDatabase().update(Loans.TABLE, values, Loans.ID + " = ?", new String[]{loanId});
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

            ContentValues values = new ContentValues();
            values.putNull(Loans.CODE);
            if (status == LoanStatus.ACCEPTED) {
                Calendar calendar = Calendar.getInstance();
                values.put(Loans.START_TIME, toDate(calendar));
                calendar.add(Calendar.DAY_OF_MONTH, days);
                values.put(Loans.RETURN_TIME, toDate(calendar));
                values.put(Loans.STATE, LoanStatus.ON_LOAN.name());
                modifyAvailability(db, articleId, ItemAvailability.NOT_AVAILABLE);
            } else {
                values.put(Loans.STATE, LoanStatus.FINISHED.name());
                modifyAvailability(db, articleId, ItemAvailability.AVAILABLE);
            }
            db.update(Loans.TABLE, values, Loans.ID + " = ?", new String[]{loanId});
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

    private static void modifyAvailability(SQLiteDatabase db, String articleId, ItemAvailability availability) {
        ContentValues values = new ContentValues();
        values.put(Articles.AVAILABILITY, availability.name());
        db.update(Articles.TABLE, values, Articles.ID + " = ?", new String[]{articleId});
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
