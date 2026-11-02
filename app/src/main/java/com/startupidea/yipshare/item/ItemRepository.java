package com.startupidea.yipshare.item;

import java.util.List;

public interface ItemRepository {

    List<Item> getAllItems();

    Item createItem(ItemPayload payload);
}
