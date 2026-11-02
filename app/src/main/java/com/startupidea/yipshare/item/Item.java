package com.startupidea.yipshare.item;

import java.io.Serializable;

public final class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final ItemPayload payload;

    public Item(String id, ItemPayload payload) {
        this.id = id;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public ItemPayload getPayload() {
        return payload;
    }
}
