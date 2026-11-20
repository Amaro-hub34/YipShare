package com.startupidea.yipshare.item;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.ConflictException;
import com.startupidea.yipshare.error.ResourceNotFound;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ItemService {

    public static final int DESCRIPTION_MIN_LENGTH = 10;

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getAllItems(String condominiumId) {
        if (condominiumId == null) return new ArrayList<>();
        return itemRepository.getAllItemsByCondominiumId(condominiumId);
    }

    public List<Item> filterMyItems(List<Item> items, String userId, String query) {
        List<Item> matches = new ArrayList<>();
        for (Item item : search(items, query)) {
            if (item.isOwnedBy(userId)) matches.add(item);
        }
        return matches;
    }

    public List<Item> filterAvailableItems(List<Item> items, String userId, String query) {
        List<Item> matches = new ArrayList<>();
        for (Item item : search(items, query)) {
            if (!item.isOwnedBy(userId) && item.availability() == ItemAvailability.AVAILABLE) matches.add(item);
        }
        return matches;
    }

    public void validate(ItemPayload payload) {
        Set<ItemField> invalidFields = EnumSet.noneOf(ItemField.class);

        if (payload.getName().trim().isEmpty()) invalidFields.add(ItemField.NAME);
        if (payload.getDescription().trim().length() < DESCRIPTION_MIN_LENGTH) invalidFields.add(ItemField.DESCRIPTION);
        if (payload.getImageUri() == null) invalidFields.add(ItemField.IMAGE);
        if (payload.getLoanDurations().isEmpty()) invalidFields.add(ItemField.LOAN_DURATIONS);

        if (!invalidFields.isEmpty()) throw new InvalidItem(invalidFields);
    }

    public Item createItem(String condominiumId, ItemPayload payload) {
        if (condominiumId == null) throw new ConflictException("User has no condominium to publish in", R.string.error_sin_condominio);
        validate(payload);
        return itemRepository.createItem(condominiumId, payload);
    }

    public Item modifyItem(String userId, Item item, ItemPayload payload) {
        ensureOwnerCanChange(userId, item);
        validate(payload);
        return itemRepository.modifyItem(item, payload);
    }

    public void deleteItem(String userId, Item item) {
        if (item.isOwnedBy(userId)) ensureOwnerCanChange(userId, item);
        itemRepository.deleteItem(item);
    }

    private List<Item> search(List<Item> items, String query) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        List<Item> matches = new ArrayList<>();
        for (Item item : items) {
            if (item.payload().getName().toLowerCase(Locale.ROOT).contains(needle)) matches.add(item);
        }
        return matches;
    }

    private void ensureOwnerCanChange(String userId, Item item) {
        if (item == null || !item.isOwnedBy(userId)) throw new ResourceNotFound("Item does not exist or belongs to another user", R.string.error_articulo_no_existe);
        if (item.availability() != ItemAvailability.AVAILABLE) throw new ConflictException("Only an available item can be modified or deleted", R.string.error_articulo_no_disponible);
    }
}
