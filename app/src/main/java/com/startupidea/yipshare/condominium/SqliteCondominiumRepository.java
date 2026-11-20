package com.startupidea.yipshare.condominium;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.startupidea.yipshare.R;
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
            = "SELECT condominium_id FROM condominiums WHERE short_code = ?";

    private static final String getMembership
            = "SELECT 1 FROM condominiums WHERE condominium_id = ?2 AND admin_id = ?1"
            + " UNION SELECT 1 FROM condominium_members WHERE condominium_id = ?2 AND user_id = ?1"
            + " UNION SELECT 1 FROM pending_condominium_members WHERE condominium_id = ?2 AND user_id = ?1";

    private static final String getSuggestedUnit
            = "SELECT condominium_unit_id FROM condominium_units WHERE condominium_id = ?"
            + " ORDER BY name = ? COLLATE NOCASE DESC, CAST(name AS INTEGER) LIMIT 1";

    private static final String getAllMembershipsByUserId
            = "SELECT condominium.condominium_id, condominium.name, condominium.admin_id = ?1,"
            + " (SELECT COUNT(*) FROM loans loan JOIN articles article ON article.article_id = loan.article_id"
            + " WHERE loan.lender_id = ?1 AND loan.state = 'PENDING' AND article.deleted = 0"
            + " AND article.condominium_id = condominium.condominium_id)"
            + " + CASE WHEN condominium.admin_id = ?1 THEN (SELECT COUNT(*) FROM pending_condominium_members pending"
            + " WHERE pending.condominium_id = condominium.condominium_id) ELSE 0 END,"
            + " CASE WHEN condominium.admin_id = ?1 THEN condominium.short_code END"
            + " FROM condominiums condominium"
            + " WHERE condominium.admin_id = ?1 OR EXISTS (SELECT 1 FROM condominium_members member"
            + " WHERE member.condominium_id = condominium.condominium_id AND member.user_id = ?1)"
            + " ORDER BY condominium.name";

    private static final String getAllNeighborsByCondominiumId
            = "SELECT neighbor.user_id, neighbor.username, GROUP_CONCAT(unit.name, ', '), neighbor.phone_number,"
            + " neighbor.computed_rating, condominium.admin_id = neighbor.user_id, MIN(member.can_request_loans)"
            + " FROM condominium_members member"
            + " JOIN users neighbor ON neighbor.user_id = member.user_id"
            + " JOIN condominium_units unit ON unit.condominium_unit_id = member.condominium_unit_id"
            + " JOIN condominiums condominium ON condominium.condominium_id = member.condominium_id"
            + " WHERE member.condominium_id = ? AND neighbor.state = 'active'"
            + " GROUP BY neighbor.user_id ORDER BY neighbor.username";

    private static final String getAllJoinRequestsByCondominiumId
            = "SELECT requester.user_id, requester.username,"
            + " TRIM(requester.name || ' ' || requester.paternal_surname || ' ' || IFNULL(requester.maternal_surname, '')),"
            + " requester.phone_number, pending.unit, pending.suggested_unit_id, requester.computed_rating"
            + " FROM pending_condominium_members pending"
            + " JOIN users requester ON requester.user_id = pending.user_id"
            + " WHERE pending.condominium_id = ? AND requester.state = 'active'"
            + " ORDER BY requester.username";

    private static final String getCondominiumName
            = "SELECT name FROM condominiums WHERE condominium_id = ?";

    private static final String getAllUnitsByCondominiumId
            = "SELECT condominium_unit_id, name FROM condominium_units WHERE condominium_id = ?"
            + " ORDER BY CAST(name AS INTEGER), name";

    private static final String getUnitInUse
            = "SELECT 1 FROM condominium_members WHERE condominium_unit_id = ?1"
            + " UNION SELECT 1 FROM pending_condominium_members WHERE suggested_unit_id = ?1";

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
            values.put("condominium_id", condominiumId);
            values.put("short_code", generateShortCode(db));
            values.put("admin_id", sessionRepository.getUserId());
            values.put("name", payload.name().trim());
            values.put("street", payload.street().trim());
            values.put("neighborhood", payload.neighborhood().trim());
            values.put("postal_code", payload.zipCode());
            values.put("municipality", payload.municipality().trim());
            values.put("city", payload.city().trim());
            values.put("state", payload.state().trim());
            values.put("deed", payload.deedUri());
            values.put("minutes", payload.minutesUri());
            values.put("tax_id", payload.taxIdUri());
            values.put("identification", payload.identificationUri());
            db.insertOrThrow("condominiums", null, values);
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
        values.put("user_id", userId);
        values.put("condominium_id", condominiumId);
        values.put("unit", unit);
        try (Cursor cursor = db.rawQuery(getSuggestedUnit, new String[]{condominiumId, unit})) {
            if (cursor.moveToFirst()) values.put("suggested_unit_id", cursor.getString(0));
        }
        db.insertOrThrow("pending_condominium_members", null, values);
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
            values.put("user_id", userId);
            values.put("condominium_id", condominiumId);
            values.put("condominium_unit_id", unitId);
            db.insertWithOnConflict("condominium_members", null, values, SQLiteDatabase.CONFLICT_IGNORE);
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
        databaseDriver.getWritableDatabase().execSQL(
                "UPDATE condominium_members SET can_request_loans = ? WHERE user_id = ? AND condominium_id = ?",
                new Object[]{allowed ? 1 : 0, userId, condominiumId});
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
        databaseDriver.getWritableDatabase().execSQL("UPDATE condominium_units SET name = ? WHERE condominium_unit_id = ?",
                new Object[]{name, unitId});
    }

    @Override
    public void deleteUnit(String condominiumId, String unitId) {
        SQLiteDatabase db = databaseDriver.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(getUnitInUse, new String[]{unitId})) {
            if (cursor.moveToFirst()) throw new ConflictException("Unit has members assigned", R.string.error_unidad_ocupada);
        }
        db.delete("condominium_units", "condominium_unit_id = ?", new String[]{unitId});
    }

    private void deleteJoinRequest(SQLiteDatabase db, String userId, String condominiumId) {
        int deleted = db.delete("pending_condominium_members", "user_id = ? AND condominium_id = ?", new String[]{userId, condominiumId});
        if (deleted == 0) throw new ResourceNotFound("Join request was already answered", R.string.error_solicitud_respondida);
    }

    private static void insertUnit(SQLiteDatabase db, String condominiumId, String name) {
        ContentValues values = new ContentValues();
        values.put("condominium_unit_id", UUID.randomUUID().toString());
        values.put("condominium_id", condominiumId);
        values.put("name", name);
        db.insertOrThrow("condominium_units", null, values);
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
