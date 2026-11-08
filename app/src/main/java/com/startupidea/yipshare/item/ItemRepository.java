package com.startupidea.yipshare.item;

import java.util.List;

public interface ItemRepository {

    List<Item> getAllItemsByCondominiumId(String condominiumId);

    Item getItemById(String id);

    Item createItem(String ownerId, String condominiumId, ItemPayload payload);

    Item modifyItem(String id, ItemPayload payload);

    void deleteItem(String id);
}
