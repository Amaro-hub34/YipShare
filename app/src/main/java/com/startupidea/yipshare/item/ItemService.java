package com.startupidea.yipshare.item;

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
            if (!item.isOwnedBy(userId) && item.getAvailability() == ItemAvailability.AVAILABLE) matches.add(item);
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
        if (condominiumId == null) throw new NoCondominiumSelected();
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
            if (item.getPayload().getName().toLowerCase(Locale.ROOT).contains(needle)) matches.add(item);
        }
        return matches;
    }

    private void ensureOwnerCanChange(String userId, Item item) {
        if (item == null || !item.isOwnedBy(userId)) throw new ItemNotFound();
        if (item.getAvailability() != ItemAvailability.AVAILABLE) throw new ItemNotAvailable();
    }
}
