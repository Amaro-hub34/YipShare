package com.startupidea.yipshare.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public class LocalItemRepositoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File file() {
        return new File(folder.getRoot(), "items.dat");
    }

    private static ItemPayload payload(String name) {
        return new ItemPayload(
                name, "Tools", "Works well", "Good", "file:///image", Collections.singletonList("1 day"));
    }

    @Test
    public void returnsEmptyListWithoutFile() {
        assertTrue(new LocalItemRepository(file()).getAllItems().isEmpty());
    }

    @Test
    public void keepsItemsAcrossInstances() {
        new LocalItemRepository(file()).createItem(payload("Drill"));

        List<Item> items = new LocalItemRepository(file()).getAllItems();

        assertEquals(1, items.size());
        assertEquals("Drill", items.get(0).getPayload().getName());
    }

    @Test
    public void returnsNewestItemFirst() {
        LocalItemRepository repository = new LocalItemRepository(file());
        repository.createItem(payload("Drill"));
        repository.createItem(payload("Ladder"));

        assertEquals("Ladder", repository.getAllItems().get(0).getPayload().getName());
    }

    @Test
    public void returnsEmptyListWithCorruptFile() throws IOException {
        Files.write(file().toPath(), "not items".getBytes(StandardCharsets.UTF_8));

        assertTrue(new LocalItemRepository(file()).getAllItems().isEmpty());
    }
}
