package com.startupidea.yipshare.item;

import com.startupidea.yipshare.error.DatabaseUnavailable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: replace with the real implementation once the database exists.
public class LocalItemRepository implements ItemRepository {

    private final File file;

    public LocalItemRepository(File file) {
        this.file = file;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Item> getAllItems() {
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<Item>) input.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Item createItem(ItemPayload payload) {
        Item item = new Item(UUID.randomUUID().toString(), payload);
        List<Item> items = getAllItems();
        items.add(0, item);

        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(items);
        } catch (IOException e) {
            throw new DatabaseUnavailable();
        }
        return item;
    }
}
