package com.startupidea.yipshare.item;

import com.startupidea.yipshare.error.ConflictException;
import com.startupidea.yipshare.error.ResourceNotFound;
import android.content.Context;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.ServerError;
import com.startupidea.yipshare.error.ServerUnavailable;
import com.startupidea.yipshare.utils.ImageFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ApiItemRepository implements ItemRepository {

    private static final String[] CATEGORIES = {
            "Herramientas", "Electrodomésticos", "Jardinería", "Cocina", "Limpieza", "Electrónica", "Automotriz", "Otros"};
    private static final String[] CONDITIONS = {"excellent", "good", "medium", "bad"};
    private static final int NOT_FOUND = 404;
    private static final int CONFLICT = 409;

    private final ApiClient apiClient;
    private final Context context;
    private final List<String> categories;
    private final List<String> conditions;
    private final List<String> durations;

    public ApiItemRepository(ApiClient apiClient, Context context) {
        this.apiClient = apiClient;
        this.context = context;
        this.categories = Arrays.asList(context.getResources().getStringArray(R.array.categorias_articulo));
        this.conditions = Arrays.asList(context.getResources().getStringArray(R.array.opciones_estado_fisico));
        this.durations = Arrays.asList(context.getResources().getStringArray(R.array.tiempo_prestamo));
    }

    @Override
    public List<Item> getAllItemsByCondominiumId(String condominiumId) {
        try {
            JSONArray array = apiClient.getArray("/condominiums/" + condominiumId + "/articles");
            List<Item> items = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) items.add(readItem(array.getJSONObject(i), condominiumId));
            return items;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public Item createItem(String condominiumId, ItemPayload payload) {
        try {
            JSONObject json = apiClient.post("/condominiums/" + condominiumId + "/articles", toJson(payload));
            return new Item(json.getString("articleId"), json.getString("lendingUser"), condominiumId,
                    ItemAvailability.AVAILABLE, withLocalImage(payload, json));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public Item modifyItem(Item item, ItemPayload payload) {
        try {
            JSONObject json = apiClient.patch(articlePath(item), toJson(payload));
            return new Item(item.id(), item.ownerId(), item.condominiumId(), item.availability(),
                    withLocalImage(payload, json));
        } catch (ServerError e) {
            throw translate(e);
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void deleteItem(Item item) {
        try {
            apiClient.delete(articlePath(item));
        } catch (ServerError e) {
            throw translate(e);
        }
    }

    private JSONObject toJson(ItemPayload payload) throws JSONException {
        JSONArray days = new JSONArray();
        int maximumDays = LoanDuration.daysOf(0);
        for (String duration : payload.getLoanDurations()) {
            int index = durations.indexOf(duration);
            if (index < 0) continue;
            days.put(LoanDuration.daysOf(index));
            maximumDays = Math.max(maximumDays, LoanDuration.daysOf(index));
        }
        int category = categories.indexOf(payload.getCategory());
        return new JSONObject()
                .put("name", payload.getName())
                .put("description", payload.getDescription())
                .put("category", CATEGORIES[category < 0 ? CATEGORIES.length - 1 : category])
                .put("state", CONDITIONS[Math.max(0, conditions.indexOf(payload.getPhysicalCondition()))])
                .put("maximumLendTime", maximumDays)
                .put("loanDurations", days)
                .put("image", ImageFileUtil.upload(context, apiClient, payload.getImageUri()));
    }

    private ItemPayload withLocalImage(ItemPayload payload, JSONObject json) {
        return new ItemPayload(payload.getName(), payload.getCategory(), payload.getDescription(),
                payload.getPhysicalCondition(),
                ImageFileUtil.toLocalUri(context, apiClient, ApiClient.text(json, "image")),
                payload.getLoanDurations());
    }

    private Item readItem(JSONObject json, String condominiumId) throws JSONException {
        List<String> offered = new ArrayList<>();
        JSONArray days = json.optJSONArray("loanDurations");
        for (int i = 0; days != null && i < days.length(); i++) {
            int index = LoanDuration.indexOf(days.getInt(i));
            if (index >= 0 && index < durations.size()) offered.add(durations.get(index));
        }
        if (offered.isEmpty()) offered.add(durations.get(Math.max(0, LoanDuration.indexOf(json.optInt("maximumLendTime")))));

        int category = Arrays.asList(CATEGORIES).indexOf(ApiClient.text(json, "category"));
        int condition = Arrays.asList(CONDITIONS).indexOf(ApiClient.text(json, "state"));
        return new Item(
                json.getString("articleId"),
                json.getString("ownerId"),
                condominiumId,
                ItemAvailability.valueOf(json.getString("availability").toUpperCase(Locale.ROOT)),
                new ItemPayload(
                        json.optString("name"),
                        categories.get(category < 0 ? categories.size() - 1 : category),
                        json.optString("description"),
                        conditions.get(Math.max(0, condition)),
                        ImageFileUtil.toLocalUri(context, apiClient, ApiClient.text(json, "image")),
                        offered));
    }

    private static String articlePath(Item item) {
        return "/condominiums/" + item.condominiumId() + "/articles/" + item.id();
    }

    private static RuntimeException translate(ServerError e) {
        return switch (e.getStatus()) {
            case CONFLICT -> new ConflictException("Only an available item can be modified or deleted", R.string.error_articulo_no_disponible);
            case NOT_FOUND -> new ResourceNotFound("Item does not exist or belongs to another user", R.string.error_articulo_no_existe);
            default -> e;
        };
    }
}
