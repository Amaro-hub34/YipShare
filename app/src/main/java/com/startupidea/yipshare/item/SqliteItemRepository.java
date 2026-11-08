package com.startupidea.yipshare.item;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.DatabaseUnavailable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * The database keeps language-free values (category_2, 'good', '0,3'); the app shows the texts of the
 * current language. This class translates between both using the app's option lists.
 */
public class SqliteItemRepository implements ItemRepository {

    // Same order as R.array.opciones_estado_fisico and article_state in the schema.
    private static final String[] CONDITIONS = {"excellent", "good", "medium", "bad"};
    private static final String CATEGORY_PREFIX = "category_";

    private static final String selectItems
            = "SELECT article.article_id, article.lending_user, article.condominium_id, article.availability,"
            + " article.name, category.category_id, article.description, article.state, article.image, article.loan_durations"
            + " FROM articles article"
            + " LEFT JOIN article_categories category ON category.article_id = article.article_id"
            + " WHERE article.deleted_at IS NULL";

    private static final String getAllItemsByCondominiumId
            = selectItems + " AND article.condominium_id = ? ORDER BY article.rowid DESC";

    private static final String getItemById
            = selectItems + " AND article.article_id = ?";

    private static final String deleteItem
            = "UPDATE articles SET deleted_at = datetime('now') WHERE article_id = ?";

    private final DatabaseDriver databaseDriver;
    private final List<String> categories;
    private final List<String> conditions;
    private final List<String> durations;

    public SqliteItemRepository(DatabaseDriver databaseDriver, Context context) {
        this.databaseDriver = databaseDriver;
        this.categories = Arrays.asList(context.getResources().getStringArray(R.array.categorias_articulo));
        this.conditions = Arrays.asList(context.getResources().getStringArray(R.array.opciones_estado_fisico));
        this.durations = Arrays.asList(context.getResources().getStringArray(R.array.tiempo_prestamo));
    }

    @Override
    public List<Item> getAllItemsByCondominiumId(String condominiumId) {
        List<Item> items = new ArrayList<>();
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getAllItemsByCondominiumId, new String[]{condominiumId})) {
            while (cursor.moveToNext()) items.add(readItem(cursor));
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return items;
    }

    @Override
    public Item getItemById(String id) {
        try (Cursor cursor = databaseDriver.getReadableDatabase().rawQuery(getItemById, new String[]{id})) {
            return cursor.moveToFirst() ? readItem(cursor) : null;
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    @Override
    public Item createItem(String ownerId, String condominiumId, ItemPayload payload) {
        String id = UUID.randomUUID().toString();
        ContentValues article = toValues(payload);
        article.put("article_id", id);
        article.put("condominium_id", condominiumId);
        article.put("lending_user", ownerId);
        article.put("availability", "available");

        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                db.insertOrThrow("articles", null, article);
                saveCategory(db, id, payload);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return getItemById(id);
    }

    @Override
    public Item modifyItem(String id, ItemPayload payload) {
        try {
            SQLiteDatabase db = databaseDriver.getWritableDatabase();
            db.beginTransaction();
            try {
                db.update("articles", toValues(payload), "article_id = ?", new String[]{id});
                db.delete("article_categories", "article_id = ?", new String[]{id});
                saveCategory(db, id, payload);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
        return getItemById(id);
    }

    @Override
    public void deleteItem(String id) {
        try {
            databaseDriver.getWritableDatabase().execSQL(deleteItem, new Object[]{id});
        } catch (SQLiteException e) {
            throw new DatabaseUnavailable();
        }
    }

    private ContentValues toValues(ItemPayload payload) {
        List<String> durationIndexes = new ArrayList<>();
        int maximumDays = LoanDuration.daysOf(0);
        for (String duration : payload.getLoanDurations()) {
            int index = durations.indexOf(duration);
            if (index < 0) continue;
            durationIndexes.add(String.valueOf(index));
            maximumDays = Math.max(maximumDays, LoanDuration.daysOf(index));
        }

        ContentValues article = new ContentValues();
        article.put("name", payload.getName());
        article.put("description", payload.getDescription());
        article.put("state", CONDITIONS[Math.max(0, conditions.indexOf(payload.getPhysicalCondition()))]);
        article.put("image", payload.getImageUri());
        article.put("loan_durations", TextUtils.join(",", durationIndexes));
        article.put("maximum_lend_time", maximumDays);
        return article;
    }

    private void saveCategory(SQLiteDatabase db, String articleId, ItemPayload payload) {
        int index = categories.indexOf(payload.getCategory());
        ContentValues category = new ContentValues();
        category.put("article_id", articleId);
        category.put("category_id", CATEGORY_PREFIX + (index < 0 ? categories.size() - 1 : index));
        db.insertOrThrow("article_categories", null, category);
    }

    private Item readItem(Cursor cursor) {
        List<String> offered = new ArrayList<>();
        for (String index : TextUtils.split(cursor.getString(9), ",")) {
            int position = Integer.parseInt(index);
            if (position < durations.size()) offered.add(durations.get(position));
        }

        String categoryId = cursor.getString(5);
        int category = categoryId == null ? categories.size() - 1 : Integer.parseInt(categoryId.substring(CATEGORY_PREFIX.length()));
        int condition = Arrays.asList(CONDITIONS).indexOf(cursor.getString(7));

        return new Item(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                ItemAvailability.valueOf(cursor.getString(3).toUpperCase(Locale.ROOT)),
                new ItemPayload(
                        cursor.getString(4),
                        categories.get(Math.min(category, categories.size() - 1)),
                        cursor.getString(6),
                        conditions.get(Math.max(0, condition)),
                        cursor.getString(8),
                        offered));
    }
}
