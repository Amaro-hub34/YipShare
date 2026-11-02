package com.startupidea.yipshare.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class ItemServiceTest {

    private static final String NAME = "Drill";
    private static final String DESCRIPTION = "Works well";
    private static final String IMAGE = "content://image/1";
    private static final List<String> DURATIONS = Collections.singletonList("1 day");

    private final ItemService service = new ItemService(new ItemRepository() {
        @Override
        public List<Item> getAllItems() {
            return Collections.emptyList();
        }

        @Override
        public Item createItem(ItemPayload payload) {
            return new Item("new", payload);
        }
    });

    private static ItemPayload payload(
            String name, String description, String imageUri, List<String> loanDurations) {
        return new ItemPayload(name, "Tools", description, "Good", imageUri, loanDurations);
    }

    private void assertInvalid(ItemPayload payload, EnumSet<ItemField> expected) {
        InvalidItem error = assertThrows(InvalidItem.class, () -> service.createItem(payload));
        assertEquals(expected, error.getFields());
    }

    @Test
    public void createsValidItem() {
        ItemPayload payload = payload(NAME, DESCRIPTION, IMAGE, DURATIONS);

        Item item = service.createItem(payload);

        assertFalse(item.getId().isEmpty());
        assertSame(payload, item.getPayload());
    }

    @Test
    public void rejectsBlankName() {
        assertInvalid(payload("   ", DESCRIPTION, IMAGE, DURATIONS), EnumSet.of(ItemField.NAME));
    }

    @Test
    public void rejectsBlankDescription() {
        assertInvalid(payload(NAME, "   ", IMAGE, DURATIONS), EnumSet.of(ItemField.DESCRIPTION));
    }

    @Test
    public void rejectsMissingImage() {
        assertInvalid(payload(NAME, DESCRIPTION, null, DURATIONS), EnumSet.of(ItemField.IMAGE));
    }

    @Test
    public void rejectsNoDurations() {
        assertInvalid(
                payload(NAME, DESCRIPTION, IMAGE, Collections.emptyList()),
                EnumSet.of(ItemField.LOAN_DURATIONS));
    }

    @Test
    public void reportsAllInvalidFields() {
        assertInvalid(
                payload("", "", null, Collections.emptyList()),
                EnumSet.allOf(ItemField.class));
    }

    @Test
    public void validateRejectsBlankName() {
        ItemPayload invalid = payload("", DESCRIPTION, IMAGE, DURATIONS);

        assertThrows(InvalidItem.class, () -> service.validate(invalid));
    }

    @Test
    public void acceptsSeveralDurations() {
        Item item = service.createItem(
                payload(NAME, DESCRIPTION, IMAGE, Arrays.asList("1 day", "1 week")));

        assertEquals(2, item.getPayload().getLoanDurations().size());
    }
}
