package com.startupidea.yipshare.item;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.ConflictException;
import com.startupidea.yipshare.error.ResourceNotFound;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class ItemServiceTest {

    private static final String OWNER = "owner";
    private static final String CONDO = "condo";
    private static final String NAME = "Drill";
    private static final String DESCRIPTION = "Works well, barely used";
    private static final String IMAGE = "content://image/1";
    private static final List<String> DURATIONS = Collections.singletonList("1 day");

    private final List<Item> stored = new ArrayList<>();
    private Item deleted;
    private String lastQuery;

    private final ItemService service = new ItemService(new ItemRepository() {
        @Override
        public List<Item> getAllItemsByCondominiumId(String condominiumId, String query) {
            lastQuery = query;
            List<Item> inCondominium = new ArrayList<>();
            for (Item item : stored) {
                if (item.condominiumId().equals(condominiumId)) inCondominium.add(item);
            }
            return inCondominium;
        }

        @Override
        public Item createItem(String condominiumId, ItemPayload payload) {
            return new Item("new", OWNER, condominiumId, ItemAvailability.AVAILABLE, payload);
        }

        @Override
        public Item modifyItem(Item item, ItemPayload payload) {
            return new Item(item.id(), item.ownerId(), item.condominiumId(), item.availability(), payload);
        }

        @Override
        public void deleteItem(Item item) {
            deleted = item;
        }
    });

    private static ItemPayload payload(
            String name, String description, String imageUri, List<String> loanDurations) {
        return new ItemPayload(name, "Tools", description, "Good", imageUri, loanDurations);
    }

    private static Item item(String id, String ownerId, ItemAvailability availability, String name) {
        return new Item(id, ownerId, CONDO, availability, payload(name, DESCRIPTION, IMAGE, DURATIONS));
    }

    private void assertInvalid(ItemPayload payload, EnumSet<ItemField> expected) {
        InvalidItem error = assertThrows(InvalidItem.class, () -> service.createItem(CONDO, payload));
        assertEquals(expected, error.getFields());
    }

    @Test
    public void createsValidItemForItsOwner() {
        ItemPayload payload = payload(NAME, DESCRIPTION, IMAGE, DURATIONS);

        Item item = service.createItem(CONDO, payload);

        assertFalse(item.id().isEmpty());
        assertSame(payload, item.payload());
        assertEquals(OWNER, item.ownerId());
        assertEquals(ItemAvailability.AVAILABLE, item.availability());
    }

    @Test
    public void rejectsBlankName() {
        assertInvalid(payload("   ", DESCRIPTION, IMAGE, DURATIONS), EnumSet.of(ItemField.NAME));
    }

    @Test
    public void rejectsBlankOrShortDescription() {
        assertInvalid(payload(NAME, "   ", IMAGE, DURATIONS), EnumSet.of(ItemField.DESCRIPTION));
        assertInvalid(payload(NAME, "Too short", IMAGE, DURATIONS), EnumSet.of(ItemField.DESCRIPTION));
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
        assertInvalid(payload("", "", null, Collections.emptyList()), EnumSet.allOf(ItemField.class));
    }

    @Test
    public void acceptsSeveralDurations() {
        Item item = service.createItem(CONDO, payload(NAME, DESCRIPTION, IMAGE, Arrays.asList("1 day", "1 week")));

        assertEquals(2, item.payload().getLoanDurations().size());
    }

    @Test
    public void filtersOnlyOwnItemsAndForwardsSearchToRepository() {
        Item drill = item("1", OWNER, ItemAvailability.AVAILABLE, "Power Drill");
        Item ladder = item("2", OWNER, ItemAvailability.AVAILABLE, "Ladder");
        stored.addAll(Arrays.asList(drill, ladder, item("3", "other", ItemAvailability.AVAILABLE, "Drill")));
        List<Item> items = service.getAllItems(CONDO, " dRi ");

        assertEquals(" dRi ", lastQuery);
        assertEquals(Arrays.asList(drill, ladder), service.filterMyItems(items, OWNER));
    }

    @Test
    public void availableItemsAreOtherNeighborsItemsThatCanBeRequested() {
        Item mine = item("1", OWNER, ItemAvailability.AVAILABLE, "Drill");
        Item free = item("2", "other", ItemAvailability.AVAILABLE, "Ladder");
        Item lent = item("3", "other", ItemAvailability.NOT_AVAILABLE, "Speaker");
        Item elsewhere = new Item("4", "other", "another", ItemAvailability.AVAILABLE, payload("Bike", DESCRIPTION, IMAGE, DURATIONS));
        stored.addAll(Arrays.asList(mine, free, lent, elsewhere));

        assertEquals(Collections.singletonList(free), service.filterAvailableItems(service.getAllItems(CONDO, ""), OWNER));
        assertTrue(service.getAllItems(null, "").isEmpty());
    }

    @Test
    public void publishesOnlyWithACondominiumSelected() {
        assertThrows(ConflictException.class,
                () -> service.createItem(null, payload(NAME, DESCRIPTION, IMAGE, DURATIONS)));
    }

    @Test
    public void adminDeletesPublicationsOfOthersEvenWhenLent() {
        Item lent = item("1", "other", ItemAvailability.NOT_AVAILABLE, NAME);

        service.deleteItem("admin", lent);

        assertSame(lent, deleted);
    }

    @Test
    public void ownerModifiesAvailableItem() {
        Item modified = service.modifyItem(OWNER, item("1", OWNER, ItemAvailability.AVAILABLE, NAME),
                payload("Hammer", DESCRIPTION, IMAGE, DURATIONS));

        assertEquals("Hammer", modified.payload().getName());
    }

    @Test
    public void ownerDeletesAvailableItem() {
        Item drill = item("1", OWNER, ItemAvailability.AVAILABLE, NAME);

        service.deleteItem(OWNER, drill);

        assertSame(drill, deleted);
    }

    @Test
    public void ownerCannotModifyOrDeleteUnlessAvailable() {
        Item lent = item("1", OWNER, ItemAvailability.NOT_AVAILABLE, NAME);
        Item pending = item("2", OWNER, ItemAvailability.PENDING, NAME);

        assertThrows(ConflictException.class,
                () -> service.modifyItem(OWNER, lent, payload(NAME, DESCRIPTION, IMAGE, DURATIONS)));
        assertThrows(ConflictException.class, () -> service.deleteItem(OWNER, pending));
        assertNull(deleted);
    }

    @Test
    public void cannotModifyItemsOfOtherUsers() {
        Item others = item("1", "other", ItemAvailability.AVAILABLE, NAME);

        assertThrows(ResourceNotFound.class,
                () -> service.modifyItem(OWNER, others, payload(NAME, DESCRIPTION, IMAGE, DURATIONS)));
    }
}
