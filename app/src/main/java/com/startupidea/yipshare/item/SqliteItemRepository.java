package com.startupidea.yipshare.item;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.ImageFileUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SqliteItemRepository implements ItemRepository {

    private static final String getAllItemsByCondominiumId
            = "SELECT article_id, lending_user, availability, name, category, description, physical_condition,"
            + " image, loan_durations FROM articles WHERE condominium_id = ? AND deleted = 0"
            + " AND lending_user IN (SELECT user_id FROM users WHERE state = 'active') ORDER BY rowid DESC";

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
    public List<Item> getAllItemsByCondominiumId(String condominiumId) {
        List<Item> items = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllItemsByCondominiumId, new String[]{condominiumId})) {
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
        values.put("article_id", id);
        values.put("condominium_id", condominiumId);
        values.put("lending_user", sessionRepository.getUserId());
        databaseDriver.getWritableDatabase().insertOrThrow("articles", null, values);
        return new Item(id, sessionRepository.getUserId(), condominiumId, ItemAvailability.AVAILABLE, stored);
    }

    @Override
    public Item modifyItem(Item item, ItemPayload payload) {
        ItemPayload stored = withStoredImage(payload);
        databaseDriver.getWritableDatabase().update("articles", toValues(stored), "article_id = ?", new String[]{item.id()});
        return new Item(item.id(), item.ownerId(), item.condominiumId(), item.availability(), stored);
    }

    @Override
    public void deleteItem(Item item) {
        databaseDriver.getWritableDatabase().execSQL("UPDATE articles SET deleted = 1 WHERE article_id = ?", new Object[]{item.id()});
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
        values.put("name", payload.getName().trim());
        values.put("category", Math.max(0, categories.indexOf(payload.getCategory())));
        values.put("description", payload.getDescription().trim());
        values.put("physical_condition", Math.max(0, conditions.indexOf(payload.getPhysicalCondition())));
        values.put("image", payload.getImageUri());
        values.put("loan_durations", TextUtils.join(",", offered));
        return values;
    }
}
