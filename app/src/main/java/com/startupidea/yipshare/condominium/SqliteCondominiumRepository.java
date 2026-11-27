package com.startupidea.yipshare.condominium;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseContract.Articles;
import com.startupidea.yipshare.database.DatabaseContract.Condominiums;
import com.startupidea.yipshare.database.DatabaseContract.Loans;
import com.startupidea.yipshare.database.DatabaseContract.Members;
import com.startupidea.yipshare.database.DatabaseContract.PendingMembers;
import com.startupidea.yipshare.database.DatabaseContract.Units;
import com.startupidea.yipshare.database.DatabaseContract.Users;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.ConflictException;
import com.startupidea.yipshare.error.ResourceNotFound;
import com.startupidea.yipshare.user.SessionRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteCondominiumRepository implements CondominiumRepository {

    private static final String SHORT_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int SHORT_CODE_LENGTH = 6;

    private static final String getCondominiumByCode
            = "SELECT " + Condominiums.ID + " FROM " + Condominiums.TABLE + " WHERE " + Condominiums.SHORT_CODE + " = ?";

    private static final String getMembership
            = "SELECT 1 FROM " + Condominiums.TABLE + " WHERE " + Condominiums.ID + " = ?2 AND " + Condominiums.ADMIN_ID + " = ?1"
            + " UNION SELECT 1 FROM " + Members.TABLE + " WHERE " + Members.CONDOMINIUM_ID + " = ?2 AND " + Members.USER_ID + " = ?1"
            + " UNION SELECT 1 FROM " + PendingMembers.TABLE
            + " WHERE " + PendingMembers.CONDOMINIUM_ID + " = ?2 AND " + PendingMembers.USER_ID + " = ?1";

    private static final String getSuggestedUnit
            = "SELECT " + Units.ID + " FROM " + Units.TABLE + " WHERE " + Units.CONDOMINIUM_ID + " = ?"
            + " ORDER BY " + Units.NAME + " = ? COLLATE NOCASE DESC, CAST(" + Units.NAME + " AS INTEGER) LIMIT 1";

    private static final String getAllMembershipsByUserId
            = "SELECT c." + Condominiums.ID + ", c." + Condominiums.NAME + ", c." + Condominiums.ADMIN_ID + " = ?1,"
            + " (SELECT COUNT(*) FROM " + Loans.TABLE + " l JOIN " + Articles.TABLE + " a ON a." + Articles.ID + " = l." + Loans.ARTICLE_ID
            + " WHERE l." + Loans.LENDER_ID + " = ?1 AND l." + Loans.STATE + " = 'PENDING' AND a." + Articles.DELETED + " = 0"
            + " AND a." + Articles.CONDOMINIUM_ID + " = c." + Condominiums.ID + ")"
            + " + CASE WHEN c." + Condominiums.ADMIN_ID + " = ?1 THEN (SELECT COUNT(*) FROM " + PendingMembers.TABLE + " p"
            + " WHERE p." + PendingMembers.CONDOMINIUM_ID + " = c." + Condominiums.ID + ") ELSE 0 END,"
            + " CASE WHEN c." + Condominiums.ADMIN_ID + " = ?1 THEN c." + Condominiums.SHORT_CODE + " END"
            + " FROM " + Condominiums.TABLE + " c"
            + " WHERE c." + Condominiums.ADMIN_ID + " = ?1 OR EXISTS (SELECT 1 FROM " + Members.TABLE + " m"
            + " WHERE m." + Members.CONDOMINIUM_ID + " = c." + Condominiums.ID + " AND m." + Members.USER_ID + " = ?1)"
            + " ORDER BY c." + Condominiums.NAME;

    private static final String getAllNeighborsByCondominiumId
            = "SELECT n." + Users.ID + ", n." + Users.USERNAME + ", GROUP_CONCAT(u." + Units.NAME + ", ', '), n." + Users.PHONE_NUMBER + ","
            + " n." + Users.COMPUTED_RATING + ", c." + Condominiums.ADMIN_ID + " = n." + Users.ID + ", MIN(m." + Members.CAN_REQUEST_LOANS + ")"
            + " FROM " + Members.TABLE + " m"
            + " JOIN " + Users.TABLE + " n ON n." + Users.ID + " = m." + Members.USER_ID
            + " JOIN " + Units.TABLE + " u ON u." + Units.ID + " = m." + Members.UNIT_ID
            + " JOIN " + Condominiums.TABLE + " c ON c." + Condominiums.ID + " = m." + Members.CONDOMINIUM_ID
            + " WHERE m." + Members.CONDOMINIUM_ID + " = ? AND n." + Users.STATE + " = 'active'"
            + " GROUP BY n." + Users.ID + " ORDER BY n." + Users.USERNAME;

    private static final String getAllJoinRequestsByCondominiumId
            = "SELECT r." + Users.ID + ", r." + Users.USERNAME + ","
            + " TRIM(r." + Users.NAME + " || ' ' || r." + Users.PATERNAL_SURNAME + " || ' ' || IFNULL(r." + Users.MATERNAL_SURNAME + ", '')),"
            + " r." + Users.PHONE_NUMBER + ", p." + PendingMembers.UNIT + ", p." + PendingMembers.SUGGESTED_UNIT_ID + ", r." + Users.COMPUTED_RATING
            + " FROM " + PendingMembers.TABLE + " p"
            + " JOIN " + Users.TABLE + " r ON r." + Users.ID + " = p." + PendingMembers.USER_ID
            + " WHERE p." + PendingMembers.CONDOMINIUM_ID + " = ? AND r." + Users.STATE + " = 'active'"
            + " ORDER BY r." + Users.USERNAME;

    private static final String getCondominiumName
            = "SELECT " + Condominiums.NAME + " FROM " + Condominiums.TABLE + " WHERE " + Condominiums.ID + " = ?";

    private static final String getAllUnitsByCondominiumId
            = "SELECT " + Units.ID + ", " + Units.NAME + " FROM " + Units.TABLE + " WHERE " + Units.CONDOMINIUM_ID + " = ?"
            + " ORDER BY CAST(" + Units.NAME + " AS INTEGER), " + Units.NAME;

    private static final String getUnitInUse
            = "SELECT 1 FROM " + Members.TABLE + " WHERE " + Members.UNIT_ID + " = ?1"
            + " UNION SELECT 1 FROM " + PendingMembers.TABLE + " WHERE " + PendingMembers.SUGGESTED_UNIT_ID + " = ?1";

    private final DatabaseDriver databaseDriver;
    private final SessionRepository sessionRepository;
    private final Context context;
    private final SecureRandom random = new SecureRandom();

    public SqliteCondominiumRepository(Context context) {
        this.databaseDriver = DatabaseDriver.getInstance(context);
        this.sessionRepository = new SessionRepository(context);
        this.context = context.getApplicationContext();
    }

    @Override
    public void createCondominiumRequest(CondominiumPayload payload) {
        String condominiumId = UUID.randomUUID().toString();
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(Condominiums.ID, condominiumId);
            values.put(Condominiums.SHORT_CODE, generateShortCode(db));
            values.put(Condominiums.ADMIN_ID, sessionRepository.getUserId());
            values.put(Condominiums.NAME, payload.name().trim());
            values.put(Condominiums.STREET, payload.street().trim());
            values.put(Condominiums.NEIGHBORHOOD, payload.neighborhood().trim());
            values.put(Condominiums.POSTAL_CODE, payload.zipCode());
            values.put(Condominiums.MUNICIPALITY, payload.municipality().trim());
            values.put(Condominiums.CITY, payload.city().trim());
            values.put(Condominiums.STATE, payload.state().trim());
            values.put(Condominiums.DEED, payload.deedUri());
            values.put(Condominiums.MINUTES, payload.minutesUri());
            values.put(Condominiums.TAX_ID, payload.taxIdUri());
            values.put(Condominiums.IDENTIFICATION, payload.identificationUri());
            db.insertOrThrow(Condominiums.TABLE, null, values);
            for (int number = 1; number <= payload.units(); number++) insertUnit(db, condominiumId, String.valueOf(number));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void createPendingMember(String shortCode, String unit) {
        String userId = sessionRepository.getUserId();
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        String condominiumId;
        try (Cursor cursor = db.rawQuery(getCondominiumByCode, new String[]{shortCode})) {
            if (!cursor.moveToFirst()) throw new ResourceNotFound("No approved condominium has that code", R.string.tv_error_codigo);
            condominiumId = cursor.getString(0);
        }
        try (Cursor cursor = db.rawQuery(getMembership, new String[]{userId, condominiumId})) {
            if (cursor.moveToFirst()) throw new ConflictException("User already joined or asked to join this condominium", R.string.error_ya_unido);
        }

        ContentValues values = new ContentValues();
        values.put(PendingMembers.USER_ID, userId);
        values.put(PendingMembers.CONDOMINIUM_ID, condominiumId);
        values.put(PendingMembers.UNIT, unit);
        try (Cursor cursor = db.rawQuery(getSuggestedUnit, new String[]{condominiumId, unit})) {
            if (cursor.moveToFirst()) values.put(PendingMembers.SUGGESTED_UNIT_ID, cursor.getString(0));
        }
        db.insertOrThrow(PendingMembers.TABLE, null, values);
    }

    @Override
    public List<CondominiumMembership> getAllMemberships() {
        List<CondominiumMembership> memberships = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getAllMembershipsByUserId, new String[]{sessionRepository.getUserId()})) {
            while (cursor.moveToNext()) {
                memberships.add(new CondominiumMembership(cursor.getString(0), cursor.getString(1),
                        cursor.getInt(2) == 1, cursor.getInt(3), cursor.getString(4)));
            }
        }
        return memberships;
    }

    @Override
    public List<Neighbor> getAllNeighborsByCondominiumId(String condominiumId) {
        List<Neighbor> neighbors = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllNeighborsByCondominiumId, new String[]{condominiumId})) {
            while (cursor.moveToNext()) {
                neighbors.add(new Neighbor(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getInt(4), cursor.getInt(5) == 1, cursor.getInt(6) == 1));
            }
        }
        return neighbors;
    }

    @Override
    public List<JoinRequest> getAllJoinRequestsByCondominiumId(String condominiumId, String condominiumName) {
        List<JoinRequest> requests = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllJoinRequestsByCondominiumId, new String[]{condominiumId})) {
            while (cursor.moveToNext()) {
                requests.add(new JoinRequest(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), condominiumId, condominiumName, cursor.getString(4),
                        cursor.getString(5), cursor.getInt(6)));
            }
        }
        return requests;
    }

    @Override
    public void acceptJoinRequest(String userId, String condominiumId, String unitId) {
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        db.beginTransaction();
        try {
            deleteJoinRequest(db, userId, condominiumId);
            ContentValues values = new ContentValues();
            values.put(Members.USER_ID, userId);
            values.put(Members.CONDOMINIUM_ID, condominiumId);
            values.put(Members.UNIT_ID, unitId);
            db.insertWithOnConflict(Members.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            try (Cursor cursor = db.rawQuery(getCondominiumName, new String[]{condominiumId})) {
                cursor.moveToFirst();
                databaseDriver.createNotification(userId, context.getString(R.string.notif_union_aceptada),
                        context.getString(R.string.notif_union_aceptada_cuerpo, cursor.getString(0)));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void rejectJoinRequest(String userId, String condominiumId) {
        deleteJoinRequest(databaseDriver.getWritableDatabase(), userId, condominiumId);
    }

    @Override
    public void modifyLoanAccess(String userId, String condominiumId, boolean allowed) {
        ContentValues values = new ContentValues();
        values.put(Members.CAN_REQUEST_LOANS, allowed ? 1 : 0);
        databaseDriver.getWritableDatabase().update(Members.TABLE, values,
                Members.USER_ID + " = ? AND " + Members.CONDOMINIUM_ID + " = ?", new String[]{userId, condominiumId});
    }

    @Override
    public List<Unit> getAllUnitsByCondominiumId(String condominiumId) {
        List<Unit> units = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllUnitsByCondominiumId, new String[]{condominiumId})) {
            while (cursor.moveToNext()) units.add(new Unit(cursor.getString(0), cursor.getString(1)));
        }
        return units;
    }

    @Override
    public void createUnit(String condominiumId, String name) {
        insertUnit(databaseDriver.getWritableDatabase(), condominiumId, name);
    }

    @Override
    public void modifyUnit(String condominiumId, String unitId, String name) {
        ContentValues values = new ContentValues();
        values.put(Units.NAME, name);
        databaseDriver.getWritableDatabase().update(Units.TABLE, values, Units.ID + " = ?", new String[]{unitId});
    }

    @Override
    public void deleteUnit(String condominiumId, String unitId) {
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(getUnitInUse, new String[]{unitId})) {
            if (cursor.moveToFirst()) throw new ConflictException("Unit has members assigned", R.string.error_unidad_ocupada);
        }
        db.delete(Units.TABLE, Units.ID + " = ?", new String[]{unitId});
    }

    private void deleteJoinRequest(SQLiteDatabase db, String userId, String condominiumId) {
        int deleted = db.delete(PendingMembers.TABLE, PendingMembers.USER_ID + " = ? AND " + PendingMembers.CONDOMINIUM_ID + " = ?", new String[]{userId, condominiumId});
        if (deleted == 0) throw new ResourceNotFound("Join request was already answered", R.string.error_solicitud_respondida);
    }

    private static void insertUnit(SQLiteDatabase db, String condominiumId, String name) {
        ContentValues values = new ContentValues();
        values.put(Units.ID, UUID.randomUUID().toString());
        values.put(Units.CONDOMINIUM_ID, condominiumId);
        values.put(Units.NAME, name);
        db.insertOrThrow(Units.TABLE, null, values);
    }

    private String generateShortCode(SQLiteDatabase db) {
        while (true) {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < SHORT_CODE_LENGTH; i++) code.append(SHORT_CODE_CHARACTERS.charAt(random.nextInt(SHORT_CODE_CHARACTERS.length())));
            try (Cursor cursor = db.rawQuery(getCondominiumByCode, new String[]{code.toString()})) {
                if (!cursor.moveToFirst()) return code.toString();
            }
        }
    }
}
