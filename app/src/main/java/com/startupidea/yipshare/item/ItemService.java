package com.startupidea.yipshare.item;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getAllItems() {
        return itemRepository.getAllItems();
    }

    public void validate(ItemPayload payload) {
        Set<ItemField> invalidFields = EnumSet.noneOf(ItemField.class);

        if (payload.getName().trim().isEmpty()) invalidFields.add(ItemField.NAME);
        if (payload.getDescription().trim().isEmpty()) invalidFields.add(ItemField.DESCRIPTION);
        if (payload.getImageUri() == null) invalidFields.add(ItemField.IMAGE);
        if (payload.getLoanDurations().isEmpty()) invalidFields.add(ItemField.LOAN_DURATIONS);

        if (!invalidFields.isEmpty()) throw new InvalidItem(invalidFields);
    }

    public Item createItem(ItemPayload payload) {
        validate(payload);
        return itemRepository.createItem(payload);
    }
}
