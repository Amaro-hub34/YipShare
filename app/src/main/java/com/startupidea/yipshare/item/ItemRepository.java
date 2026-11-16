package com.startupidea.yipshare.item;

import java.util.List;

public interface ItemRepository {

    List<Item> getAllItemsByCondominiumId(String condominiumId);

    Item createItem(String condominiumId, ItemPayload payload);

    Item modifyItem(Item item, ItemPayload payload);

    void deleteItem(Item item);
}
