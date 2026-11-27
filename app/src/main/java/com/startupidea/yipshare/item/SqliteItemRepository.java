package com.startupidea.yipshare.item;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseContract.Articles;
import com.startupidea.yipshare.database.DatabaseContract.Users;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.ImageFileUtil;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SqliteItemRepository implements ItemRepository {

    private static final String ACCENTED = "áéíóúüñÁÉÍÓÚÜÑ";
    private static final String PLAIN = "aeiouunAEIOUUN";

    private static final String getAllItemsByCondominiumId
            = "SELECT " + Articles.ID + ", " + Articles.LENDING_USER + ", " + Articles.AVAILABILITY + ", "
            + Articles.NAME + ", " + Articles.CATEGORY + ", " + Articles.DESCRIPTION + ", " + Articles.PHYSICAL_CONDITION + ", "
            + Articles.IMAGE + ", " + Articles.LOAN_DURATIONS
            + " FROM " + Articles.TABLE
            + " WHERE " + Articles.CONDOMINIUM_ID + " = ? AND " + Articles.DELETED + " = 0"
            + " AND " + normalized(Articles.NAME) + " LIKE ? ESCAPE '\\'"
            + " AND " + Articles.LENDING_USER + " IN (SELECT " + Users.ID + " FROM " + Users.TABLE
            + " WHERE " + Users.STATE + " = 'active')"
            + " ORDER BY rowid DESC";

    private final DatabaseDriver databaseDriver;
    private final SessionRepository sessionRepository;
    private final Context context;
    private final List<String> categories;
    private final List<String> conditions;
    private final List<String> durations;

    public SqliteItemRepository(Context context) {
        this.databaseDriver = DatabaseDriver.getInstance(context);
        this.sessionRepository = new SessionRepository(context);
        this.context = context.getApplicationContext();
        this.categories = Arrays.asList(context.getResources().getStringArray(R.array.categorias_articulo));
        this.conditions = Arrays.asList(context.getResources().getStringArray(R.array.opciones_estado_fisico));
        this.durations = Arrays.asList(context.getResources().getStringArray(R.array.tiempo_prestamo));
    }

    @Override
    public List<Item> getAllItemsByCondominiumId(String condominiumId, String query) {
        String needle = Normalizer.normalize(query.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String pattern = "%" + needle.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        List<Item> items = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllItemsByCondominiumId,
                new String[]{condominiumId, pattern})) {
            while (cursor.moveToNext()) {
                List<String> offered = new ArrayList<>();
                for (String index : cursor.getString(8).split(",")) offered.add(durations.get(Integer.parseInt(index)));
                items.add(new Item(
                        cursor.getString(0),
                        cursor.getString(1),
                        condominiumId,
                        ItemAvailability.valueOf(cursor.getString(2)),
                        new ItemPayload(
                                cursor.getString(3),
                                categories.get(cursor.getInt(4)),
                                cursor.getString(5),
                                conditions.get(cursor.getInt(6)),
                                cursor.getString(7),
                                offered)));
            }
        }
        return items;
    }

    @Override
    public Item createItem(String condominiumId, ItemPayload payload) {
        String id = UUID.randomUUID().toString();
        ItemPayload stored = withStoredImage(payload);
        ContentValues values = toValues(stored);
        values.put(Articles.ID, id);
        values.put(Articles.CONDOMINIUM_ID, condominiumId);
        values.put(Articles.LENDING_USER, sessionRepository.getUserId());
        databaseDriver.getWritableDatabase().insertOrThrow(Articles.TABLE, null, values);
        return new Item(id, sessionRepository.getUserId(), condominiumId, ItemAvailability.AVAILABLE, stored);
    }

    @Override
    public Item modifyItem(Item item, ItemPayload payload) {
        ItemPayload stored = withStoredImage(payload);
        databaseDriver.getWritableDatabase().update(Articles.TABLE, toValues(stored), Articles.ID + " = ?", new String[]{item.id()});
        return new Item(item.id(), item.ownerId(), item.condominiumId(), item.availability(), stored);
    }

    @Override
    public void deleteItem(Item item) {
        ContentValues values = new ContentValues();
        values.put(Articles.DELETED, 1);
        databaseDriver.getWritableDatabase().update(Articles.TABLE, values, Articles.ID + " = ?", new String[]{item.id()});
    }

    private static String normalized(String column) {
        String expression = column;
        for (int i = 0; i < ACCENTED.length(); i++) {
            expression = "REPLACE(" + expression + ", '" + ACCENTED.charAt(i) + "', '" + PLAIN.charAt(i) + "')";
        }
        return "LOWER(" + expression + ")";
    }

    private ItemPayload withStoredImage(ItemPayload payload) {
        return new ItemPayload(payload.getName(), payload.getCategory(), payload.getDescription(),
                payload.getPhysicalCondition(), ImageFileUtil.save(context, payload.getImageUri()), payload.getLoanDurations());
    }

    private ContentValues toValues(ItemPayload payload) {
        List<String> offered = new ArrayList<>();
        for (String duration : payload.getLoanDurations()) {
            if (durations.contains(duration)) offered.add(String.valueOf(durations.indexOf(duration)));
        }
        ContentValues values = new ContentValues();
        values.put(Articles.NAME, payload.getName().trim());
        values.put(Articles.CATEGORY, Math.max(0, categories.indexOf(payload.getCategory())));
        values.put(Articles.DESCRIPTION, payload.getDescription().trim());
        values.put(Articles.PHYSICAL_CONDITION, Math.max(0, conditions.indexOf(payload.getPhysicalCondition())));
        values.put(Articles.IMAGE, payload.getImageUri());
        values.put(Articles.LOAN_DURATIONS, TextUtils.join(",", offered));
        return values;
    }
}
