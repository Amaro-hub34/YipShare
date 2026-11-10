package com.startupidea.yipshare.condominium;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.database.DemoData;
import com.startupidea.yipshare.error.DatabaseUnavailable;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteCondominiumRepository implements CondominiumRepository {

    private static final String SHORT_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int SHORT_CODE_LENGTH = 6;

    private static final String getCondominiumByCode
            = "SELECT condominium_id FROM condominiums WHERE short_code = ?";

    private static final String getApprovedCondominiumByCode
            = "SELECT condominium_id FROM condominiums WHERE short_code = ? AND digital_state = 'approved'";

    private static final String getMembership
            = "SELECT 1 FROM condominium_members WHERE user_id = ? AND condominium_id = ?"
            + " UNION SELECT 1 FROM pending_condominium_members WHERE user_id = ? AND condominium_id = ?";

    private static final String getSuggestedUnit
            = "SELECT condominium_unit_id FROM condominium_units WHERE condominium_id = ?"
            + " ORDER BY name = ? DESC, CAST(name AS INTEGER) LIMIT 1";

    private static final String ADMIN_ROLE = "role_condominium_admin";
    private static final String REQUEST_LOANS = "permission_request_loans";
    private static final String LOANS_DENIED = "{\"allowed\": false}";

    private static final String isAdmin
            = "EXISTS (SELECT 1 FROM user_roles role WHERE role.user_id = member.user_id"
            + " AND role.condominium_id = member.condominium_id AND role.role_id = '" + ADMIN_ROLE + "')";

    private static final String loansDenied
            = "EXISTS (SELECT 1 FROM permission_overrides override WHERE override.user_id = member.user_id"
            + " AND override.condominium_id = member.condominium_id AND override.permission_id = '" + REQUEST_LOANS + "'"
            + " AND override.constraints = '" + LOANS_DENIED + "')";

    private static final String pendingNotifications
            = "(SELECT COUNT(*) FROM loans loan JOIN articles article ON article.article_id = loan.article_id"
            + " WHERE loan.lender_id = member.user_id AND loan.state = 'pending'"
            + " AND article.condominium_id = member.condominium_id AND article.deleted_at IS NULL)"
            + " + CASE WHEN " + isAdmin + " THEN (SELECT COUNT(*) FROM pending_condominium_members pending"
            + " WHERE pending.condominium_id = member.condominium_id) ELSE 0 END";

    private static final String getAllMembershipsByUserId
            = "SELECT DISTINCT condominium.condominium_id, condominium.name, " + isAdmin + ", " + pendingNotifications
            + " FROM condominium_members member"
            + " JOIN condominiums condominium ON condominium.condominium_id = member.condominium_id"
            + " WHERE member.user_id = ? AND condominium.digital_state = 'approved'"
            + " ORDER BY condominium.name";

    private static final String getMembershipByUserId
            = "SELECT 1 FROM condominium_members WHERE user_id = ? LIMIT 1";

    private static final String getAdminRole
            = "SELECT 1 FROM user_roles WHERE user_id = ? AND condominium_id = ? AND role_id = '" + ADMIN_ROLE + "'";

    private static final String getAllNeighborsByCondominiumId
            = "SELECT neighbor.user_id, neighbor.username, GROUP_CONCAT(unit.name, ', '), neighbor.phone_number,"
            + " neighbor.computed_rating, " + isAdmin + ", NOT " + loansDenied
            + " FROM condominium_members member"
            + " JOIN users neighbor ON neighbor.user_id = member.user_id"
            + " JOIN condominium_units unit ON unit.condominium_unit_id = member.condominium_unit_id"
            + " WHERE member.condominium_id = ? AND neighbor.state = 'active'"
            + " GROUP BY neighbor.user_id ORDER BY neighbor.username";

    private static final String getAllJoinRequestsByAdminId
            = "SELECT pending.user_id, requester.username, condominium.condominium_id, condominium.name,"
            + " pending.unit, requester.computed_rating,"
            + " requester.name, requester.paternal_surname, requester.maternal_surname, requester.phone_number"
            + " FROM pending_condominium_members pending"
            + " JOIN users requester ON requester.user_id = pending.user_id"
            + " JOIN condominiums condominium ON condominium.condominium_id = pending.condominium_id"
            + " JOIN user_roles role ON role.condominium_id = pending.condominium_id"
            + " AND role.user_id = ? AND role.role_id = '" + ADMIN_ROLE + "'"
            + " WHERE pending.condominium_id = ?"
            + " ORDER BY requester.username";

    private static final String getJoinRequest
            = "SELECT 1 FROM pending_condominium_members WHERE user_id = ? AND condominium_id = ?";

    private static final String acceptJoinRequest
            = "INSERT INTO condominium_members (user_id, condominium_id, condominium_unit_id)"
            + " SELECT user_id, condominium_id, suggested_unit_id FROM pending_condominium_members"
            + " WHERE user_id = ? AND condominium_id = ?";

    private static final String deleteJoinRequest
            = "DELETE FROM pending_condominium_members WHERE user_id = ? AND condominium_id = ?";

    private static final String canRequestLoans
            = "SELECT 1 FROM condominium_members member WHERE member.user_id = ? AND member.condominium_id = ?"
            + " AND NOT " + loansDenied;

    private static final String deleteLoanAccessOverride
            = "DELETE FROM permission_overrides WHERE user_id = ? AND condominium_id = ? AND permission_id = '" + REQUEST_LOANS + "'";

    private static final String insertDemoCondominium
            = "INSERT INTO condominiums (condominium_id, short_code, user_id, name, street, neighborhood, postal_code,"
            + " municipality, city, digital_state, approved_at, approved_by)"
            + " VALUES (?, ?, ?, ?, 'Av. Demo 100', 'Centro', '06000', 'Cuauhtémoc', 'CDMX', 'approved', datetime('now'), ?)";

    private final DatabaseDriver databaseDriver;
    private final Context context;
    private final SecureRandom random = new SecureRandom();

    public SqliteCondominiumRepository(DatabaseDriver databaseDriver, Context context) {
        this.databaseDriver = databaseDriver;
        this.context = context;
    }

    @Override
    public void createCondominiumRequest(String userId, CondominiumPayload payload) {
        String condominiumId = UUID.randomUUID().toString();

        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues condominium = new ContentValues();
                condominium.put("condominium_id", condominiumId);
                condominium.put("short_code", generateShortCode(db));
                condominium.put("user_id", userId);
                condominium.put("name", payload.getName());
                condominium.put("street", payload.getStreet());
                condominium.put("neighborhood", payload.getNeighborhood());
                condominium.put("postal_code", payload.getZipCode());
                condominium.put("municipality", payload.getMunicipality());
                condominium.put("city", payload.getCity());
                condominium.put("digital_state", "pending");
                db.insertOrThrow("condominiums", null, condominium);

                // The tax id (RFC) document has no column in the schema, so it is not stored.
                ContentValues documents = new ContentValues();
                documents.put("condominium_id", condominiumId);
                documents.put("ecrpc_document", payload.getDeedUri());
                documents.put("aana_document", payload.getMinutesUri());
                documents.put("owner_ine_document", payload.getIdentificationUri());
                db.insertOrThrow("temporary_condominium_verification_data", null, documents);

                for (int number = 1; number <= payload.getUnits(); number++) {
                    ContentValues unit = new ContentValues();
                    unit.put("condominium_unit_id", UUID.randomUUID().toString());
                    unit.put("condominium_id", condominiumId);
                    unit.put("name", String.valueOf(number));
                    db.insertOrThrow("condominium_units", null, unit);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public String getApprovedCondominiumIdByCode(String shortCode) {
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getApprovedCondominiumByCode, new String[]{shortCode})) {
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public boolean existsMembership(String userId, String condominiumId) {
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getMembership, new String[]{userId, condominiumId, userId, condominiumId})) {
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void createPendingMember(String userId, String condominiumId, String building, String apartment) {
        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();

            String suggestedUnitId;
            try (Cursor cursor = db.rawQuery(getSuggestedUnit, new String[]{condominiumId, apartment})) {
                if (!cursor.moveToFirst()) throw new DatabaseUnavailable();
                suggestedUnitId = cursor.getString(0);
            }

            ContentValues member = new ContentValues();
            member.put("user_id", userId);
            member.put("condominium_id", condominiumId);
            member.put("unit", building.isEmpty() ? apartment : building + " " + apartment);
            member.put("suggested_unit_id", suggestedUnitId);
            db.insertOrThrow("pending_condominium_members", null, member);
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public List<CondominiumMembership> getAllMembershipsByUserId(String userId) {
        List<CondominiumMembership> memberships = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllMembershipsByUserId, new String[]{userId})) {
            while (cursor.moveToNext()) {
                memberships.add(new CondominiumMembership(
                        cursor.getString(0), cursor.getString(1), cursor.getInt(2) == 1, cursor.getInt(3)));
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return memberships;
    }

    @Override
    public boolean existsMembershipByUserId(String userId) {
        return exists(getMembershipByUserId, userId);
    }

    @Override
    public boolean isCondominiumAdmin(String userId, String condominiumId) {
        return exists(getAdminRole, userId, condominiumId);
    }

    @Override
    public List<Neighbor> getAllNeighborsByCondominiumId(String condominiumId) {
        List<Neighbor> neighbors = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllNeighborsByCondominiumId, new String[]{condominiumId})) {
            while (cursor.moveToNext()) {
                neighbors.add(new Neighbor(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        cursor.getInt(4), cursor.getInt(5) == 1, cursor.getInt(6) == 1));
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return neighbors;
    }

    @Override
    public List<JoinRequest> getAllJoinRequestsByAdminId(String adminId, String condominiumId) {
        List<JoinRequest> requests = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase()
                .rawQuery(getAllJoinRequestsByAdminId, new String[]{adminId, condominiumId})) {
            while (cursor.moveToNext()) {
                String fullName = joinNonEmpty(cursor.getString(6), cursor.getString(7), cursor.getString(8));
                requests.add(new JoinRequest(cursor.getString(0), cursor.getString(1), fullName, cursor.getString(9),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5)));
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return requests;
    }

    @Override
    public boolean existsJoinRequest(String userId, String condominiumId) {
        return exists(getJoinRequest, userId, condominiumId);
    }

    @Override
    public void acceptJoinRequest(String userId, String condominiumId) {
        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL(acceptJoinRequest, new Object[]{userId, condominiumId});
                db.execSQL(deleteJoinRequest, new Object[]{userId, condominiumId});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void rejectJoinRequest(String userId, String condominiumId) {
        try {
            databaseDriver.getWritableDatabase().execSQL(deleteJoinRequest, new Object[]{userId, condominiumId});
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public boolean canRequestLoans(String userId, String condominiumId) {
        return userId != null && condominiumId != null && exists(canRequestLoans, userId, condominiumId);
    }

    @Override
    public void modifyLoanAccess(String userId, String condominiumId, boolean allowed) {
        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL(deleteLoanAccessOverride, new Object[]{userId, condominiumId});
                if (!allowed) insertLoanDenied(db, userId, condominiumId);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public void createDemoCondominiums(String userId) {
        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                DemoData.createDemoNeighbors(db);

                String administered = createDemoCondominium(db, userId, "Residencial Los Pinos", 10);
                addMember(db, administered, userId, "1");
                addMember(db, administered, DemoData.CARLOS, "2");
                addMember(db, administered, DemoData.ANA, "3");
                addMember(db, administered, DemoData.LUIS, "4");
                addAdmin(db, administered, userId);
                addJoinRequest(db, administered, DemoData.MARIA, "Torre A 5", "5");
                addJoinRequest(db, administered, DemoData.JORGE, "8", "8");
                insertLoanDenied(db, DemoData.LUIS, administered);
                addDemoArticle(db, administered, DemoData.CARLOS, R.string.demo_podadora, R.string.demo_desc_podadora, "category_2", "0,1,2");
                addDemoArticle(db, administered, DemoData.ANA, R.string.demo_licuadora, R.string.demo_desc_licuadora, "category_3", "2,3");

                String lived = createDemoCondominium(db, DemoData.CARLOS, "Torres del Valle", 6);
                addMember(db, lived, DemoData.CARLOS, "1");
                addMember(db, lived, DemoData.LUIS, "2");
                addMember(db, lived, userId, "3");
                addAdmin(db, lived, DemoData.CARLOS);
                addDemoArticle(db, lived, DemoData.LUIS, R.string.demo_aspiradora, R.string.demo_desc_aspiradora, "category_4", "3,4");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    private String createDemoCondominium(SQLiteDatabase db, String creatorId, String name, int units) {
        String condominiumId = UUID.randomUUID().toString();
        db.execSQL(insertDemoCondominium, new Object[]{condominiumId, generateShortCode(db), creatorId, name, DemoData.GLOBAL_ADMIN});
        for (int number = 1; number <= units; number++) {
            ContentValues unit = new ContentValues();
            unit.put("condominium_unit_id", unitId(condominiumId, String.valueOf(number)));
            unit.put("condominium_id", condominiumId);
            unit.put("name", String.valueOf(number));
            db.insertOrThrow("condominium_units", null, unit);
        }
        return condominiumId;
    }

    private static String unitId(String condominiumId, String unit) {
        return condominiumId + "_unit_" + unit;
    }

    private static void addMember(SQLiteDatabase db, String condominiumId, String userId, String unit) {
        ContentValues member = new ContentValues();
        member.put("user_id", userId);
        member.put("condominium_id", condominiumId);
        member.put("condominium_unit_id", unitId(condominiumId, unit));
        db.insertOrThrow("condominium_members", null, member);
    }

    private static void addAdmin(SQLiteDatabase db, String condominiumId, String userId) {
        ContentValues role = new ContentValues();
        role.put("uuid", UUID.randomUUID().toString());
        role.put("user_id", userId);
        role.put("condominium_id", condominiumId);
        role.put("role_id", ADMIN_ROLE);
        role.put("is_global", 0);
        db.insertOrThrow("user_roles", null, role);
    }

    private static void addJoinRequest(SQLiteDatabase db, String condominiumId, String userId, String unitText, String suggestedUnit) {
        ContentValues pending = new ContentValues();
        pending.put("user_id", userId);
        pending.put("condominium_id", condominiumId);
        pending.put("unit", unitText);
        pending.put("suggested_unit_id", unitId(condominiumId, suggestedUnit));
        db.insertOrThrow("pending_condominium_members", null, pending);
    }

    private static void insertLoanDenied(SQLiteDatabase db, String userId, String condominiumId) {
        ContentValues override = new ContentValues();
        override.put("uuid", UUID.randomUUID().toString());
        override.put("user_id", userId);
        override.put("condominium_id", condominiumId);
        override.put("permission_id", REQUEST_LOANS);
        override.put("constraints", LOANS_DENIED);
        db.insertOrThrow("permission_overrides", null, override);
    }

    private void addDemoArticle(SQLiteDatabase db, String condominiumId, String ownerId, int name, int description,
                                String categoryId, String loanDurations) {
        String articleId = UUID.randomUUID().toString();

        ContentValues article = new ContentValues();
        article.put("article_id", articleId);
        article.put("condominium_id", condominiumId);
        article.put("lending_user", ownerId);
        article.put("name", context.getString(name));
        article.put("description", context.getString(description));
        article.put("state", "good");
        article.put("availability", "available");
        article.put("maximum_lend_time", 7);
        article.put("loan_durations", loanDurations);
        db.insertOrThrow("articles", null, article);

        ContentValues category = new ContentValues();
        category.put("article_id", articleId);
        category.put("category_id", categoryId);
        db.insertOrThrow("article_categories", null, category);
    }

    private static String joinNonEmpty(String... parts) {
        StringBuilder joined = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) continue;
            if (joined.length() > 0) joined.append(' ');
            joined.append(part.trim());
        }
        return joined.toString();
    }

    private boolean exists(String query, String... values) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(query, values)) {
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    private String generateShortCode(SQLiteDatabase db) {
        while (true) {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
                code.append(SHORT_CODE_CHARACTERS.charAt(random.nextInt(SHORT_CODE_CHARACTERS.length())));
            }
            try (Cursor cursor = db.rawQuery(getCondominiumByCode, new String[]{code.toString()})) {
                if (!cursor.moveToFirst()) return code.toString();
            }
        }
    }
}
