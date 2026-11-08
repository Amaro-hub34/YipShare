package com.startupidea.yipshare.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.startupidea.yipshare.condominium.FakeCondominiumRepository;

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
    private static final String DESCRIPTION = "Works well";
    private static final String IMAGE = "content://image/1";
    private static final List<String> DURATIONS = Collections.singletonList("1 day");

    private final List<Item> stored = new ArrayList<>();
    private String deletedId;

    private final FakeCondominiumRepository condominiums = new FakeCondominiumRepository();

    {
        condominiums.members.add(FakeCondominiumRepository.key(OWNER, CONDO));
    }

    private final ItemService service = new ItemService(new ItemRepository() {
        @Override
        public List<Item> getAllItemsByCondominiumId(String condominiumId) {
            List<Item> inCondominium = new ArrayList<>();
            for (Item item : stored) {
                if (item.getCondominiumId().equals(condominiumId)) inCondominium.add(item);
            }
            return inCondominium;
        }

        @Override
        public Item getItemById(String id) {
            for (Item item : stored) {
                if (item.getId().equals(id)) return item;
            }
            return null;
        }

        @Override
        public Item createItem(String ownerId, String condominiumId, ItemPayload payload) {
            return new Item("new", ownerId, condominiumId, ItemAvailability.AVAILABLE, payload);
        }

        @Override
        public Item modifyItem(String id, ItemPayload payload) {
            Item item = getItemById(id);
            return new Item(id, item.getOwnerId(), item.getCondominiumId(), item.getAvailability(), payload);
        }

        @Override
        public void deleteItem(String id) {
            deletedId = id;
        }
    }, condominiums);

    private static ItemPayload payload(
            String name, String description, String imageUri, List<String> loanDurations) {
        return new ItemPayload(name, "Tools", description, "Good", imageUri, loanDurations);
    }

    private static Item item(String id, String ownerId, ItemAvailability availability, String name) {
        return new Item(id, ownerId, CONDO, availability, payload(name, DESCRIPTION, IMAGE, DURATIONS));
    }

    private void assertInvalid(ItemPayload payload, EnumSet<ItemField> expected) {
        InvalidItem error = assertThrows(InvalidItem.class, () -> service.createItem(OWNER, CONDO, payload));
        assertEquals(expected, error.getFields());
    }

    @Test
    public void createsValidItemForItsOwner() {
        ItemPayload payload = payload(NAME, DESCRIPTION, IMAGE, DURATIONS);

        Item item = service.createItem(OWNER, CONDO, payload);

        assertFalse(item.getId().isEmpty());
        assertSame(payload, item.getPayload());
        assertEquals(OWNER, item.getOwnerId());
        assertEquals(ItemAvailability.AVAILABLE, item.getAvailability());
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
        assertInvalid(payload("", "", null, Collections.emptyList()), EnumSet.allOf(ItemField.class));
    }

    @Test
    public void acceptsSeveralDurations() {
        Item item = service.createItem(OWNER, CONDO, payload(NAME, DESCRIPTION, IMAGE, Arrays.asList("1 day", "1 week")));

        assertEquals(2, item.getPayload().getLoanDurations().size());
    }

    @Test
    public void searchesOnlyOwnItemsByNameIgnoringCase() {
        Item drill = item("1", OWNER, ItemAvailability.AVAILABLE, "Power Drill");
        Item ladder = item("2", OWNER, ItemAvailability.AVAILABLE, "Ladder");
        stored.addAll(Arrays.asList(drill, ladder, item("3", "other", ItemAvailability.AVAILABLE, "Drill")));

        assertEquals(Collections.singletonList(drill), service.searchMyItems(OWNER, CONDO, " dRi "));
        assertEquals(Arrays.asList(drill, ladder), service.searchMyItems(OWNER, CONDO, ""));
    }

    @Test
    public void availableItemsAreOtherNeighborsItemsThatCanBeRequested() {
        Item mine = item("1", OWNER, ItemAvailability.AVAILABLE, "Drill");
        Item free = item("2", "other", ItemAvailability.AVAILABLE, "Ladder");
        Item lent = item("3", "other", ItemAvailability.NOT_AVAILABLE, "Speaker");
        Item elsewhere = new Item("4", "other", "another", ItemAvailability.AVAILABLE, payload("Bike", DESCRIPTION, IMAGE, DURATIONS));
        stored.addAll(Arrays.asList(mine, free, lent, elsewhere));

        assertEquals(Collections.singletonList(free), service.searchAvailableItems(OWNER, CONDO, ""));
        assertTrue(service.searchAvailableItems(OWNER, null, "").isEmpty());
    }

    @Test
    public void publishesOnlyInACondominiumTheOwnerBelongsTo() {
        assertThrows(NoCondominiumSelected.class,
                () -> service.createItem(OWNER, null, payload(NAME, DESCRIPTION, IMAGE, DURATIONS)));
        assertThrows(NoCondominiumSelected.class,
                () -> service.createItem(OWNER, "another", payload(NAME, DESCRIPTION, IMAGE, DURATIONS)));
    }

    @Test
    public void adminRemovesAnyPublicationOfTheirCondominium() {
        stored.add(item("1", "other", ItemAvailability.NOT_AVAILABLE, NAME));
        condominiums.admins.add(FakeCondominiumRepository.key("admin", CONDO));

        service.deleteItem("admin", "1");

        assertEquals("1", deletedId);
    }

    @Test
    public void adminOfAnotherCondominiumCannotRemoveIt() {
        stored.add(item("1", "other", ItemAvailability.AVAILABLE, NAME));
        condominiums.admins.add(FakeCondominiumRepository.key("admin", "another"));

        assertThrows(ItemNotFound.class, () -> service.deleteItem("admin", "1"));
        assertNull(deletedId);
    }

    @Test
    public void ownerModifiesAvailableItem() {
        stored.add(item("1", OWNER, ItemAvailability.AVAILABLE, NAME));

        Item modified = service.modifyItem(OWNER, "1", payload("Hammer", DESCRIPTION, IMAGE, DURATIONS));

        assertEquals("Hammer", modified.getPayload().getName());
    }

    @Test
    public void ownerDeletesAvailableItem() {
        stored.add(item("1", OWNER, ItemAvailability.AVAILABLE, NAME));

        service.deleteItem(OWNER, "1");

        assertEquals("1", deletedId);
    }

    @Test
    public void cannotModifyOrDeleteUnlessAvailable() {
        stored.add(item("1", OWNER, ItemAvailability.NOT_AVAILABLE, NAME));
        stored.add(item("2", OWNER, ItemAvailability.PENDING, NAME));

        assertThrows(ItemNotAvailable.class,
                () -> service.modifyItem(OWNER, "1", payload(NAME, DESCRIPTION, IMAGE, DURATIONS)));
        assertThrows(ItemNotAvailable.class, () -> service.deleteItem(OWNER, "2"));
        assertNull(deletedId);
    }

    @Test
    public void cannotTouchItemsOfOtherUsersOrMissingOnes() {
        stored.add(item("1", "other", ItemAvailability.AVAILABLE, NAME));

        assertThrows(ItemNotFound.class, () -> service.deleteItem(OWNER, "1"));
        assertThrows(ItemNotFound.class, () -> service.deleteItem(OWNER, "missing"));
        assertNull(deletedId);
    }
}
