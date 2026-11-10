package com.startupidea.yipshare.item;

import com.startupidea.yipshare.condominium.CondominiumRepository;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ItemService {

    private final ItemRepository itemRepository;
    private final CondominiumRepository condominiumRepository;

    public ItemService(ItemRepository itemRepository, CondominiumRepository condominiumRepository) {
        this.itemRepository = itemRepository;
        this.condominiumRepository = condominiumRepository;
    }

    public List<Item> searchMyItems(String userId, String condominiumId, String query) {
        List<Item> matches = new ArrayList<>();
        for (Item item : search(condominiumId, query)) {
            if (item.isOwnedBy(userId)) matches.add(item);
        }
        return matches;
    }

    public List<Item> searchAvailableItems(String userId, String condominiumId, String query) {
        List<Item> matches = new ArrayList<>();
        for (Item item : search(condominiumId, query)) {
            if (!item.isOwnedBy(userId) && item.getAvailability() == ItemAvailability.AVAILABLE) matches.add(item);
        }
        return matches;
    }

    public void validate(ItemPayload payload) {
        Set<ItemField> invalidFields = EnumSet.noneOf(ItemField.class);

        if (payload.getName().trim().isEmpty()) invalidFields.add(ItemField.NAME);
        if (payload.getDescription().trim().isEmpty()) invalidFields.add(ItemField.DESCRIPTION);
        if (payload.getImageUri() == null) invalidFields.add(ItemField.IMAGE);
        if (payload.getLoanDurations().isEmpty()) invalidFields.add(ItemField.LOAN_DURATIONS);

        if (!invalidFields.isEmpty()) throw new InvalidItem(invalidFields);
    }

    public Item createItem(String ownerId, String condominiumId, ItemPayload payload) {
        if (condominiumId == null || !condominiumRepository.existsMembership(ownerId, condominiumId)) {
            throw new NoCondominiumSelected();
        }
        validate(payload);
        return itemRepository.createItem(ownerId, condominiumId, payload);
    }

    public Item modifyItem(String userId, String itemId, ItemPayload payload) {
        ensureOwnerCanChange(userId, itemId);
        validate(payload);
        return itemRepository.modifyItem(itemId, payload);
    }

    public void deleteItem(String userId, String itemId) {
        Item stored = itemRepository.getItemById(itemId);
        if (stored == null) throw new ItemNotFound();
        if (!condominiumRepository.isCondominiumAdmin(userId, stored.getCondominiumId())) ensureOwnerCanChange(userId, itemId);
        itemRepository.deleteItem(itemId);
    }

    private List<Item> search(String condominiumId, String query) {
        if (condominiumId == null) return new ArrayList<>();

        String needle = query.trim().toLowerCase(Locale.ROOT);
        List<Item> matches = new ArrayList<>();
        for (Item item : itemRepository.getAllItemsByCondominiumId(condominiumId)) {
            if (item.getPayload().getName().toLowerCase(Locale.ROOT).contains(needle)) matches.add(item);
        }
        return matches;
    }

    private void ensureOwnerCanChange(String userId, String itemId) {
        Item stored = itemRepository.getItemById(itemId);
        if (stored == null || !stored.isOwnedBy(userId)) throw new ItemNotFound();
        if (stored.getAvailability() != ItemAvailability.AVAILABLE) throw new ItemNotAvailable();
    }
}
