package com.startupidea.yipshare.condominium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.EnumSet;

public class CondominiumServiceTest {

    private static final String PDF = "content://docs/1";

    private final FakeCondominiumRepository repository = new FakeCondominiumRepository();
    private final CondominiumService service = new CondominiumService(repository);

    private static CondominiumPayload payload(String zipCode, int units, String deed) {
        return new CondominiumPayload("Torres", "Av. 1", "Centro", zipCode, "Cuauhtémoc", "CDMX", "CDMX",
                units, deed, PDF, PDF, PDF);
    }

    private void assertInvalid(CondominiumPayload payload, EnumSet<CondominiumField> expected) {
        InvalidCondominium error = assertThrows(InvalidCondominium.class,
                () -> service.createCondominiumRequest("user", payload));
        assertEquals(expected, error.getFields());
    }

    @Test
    public void sendsValidRequest() {
        CondominiumPayload payload = payload("06000", 10, PDF);

        service.createCondominiumRequest("user", payload);

        assertSame(payload, repository.sent);
    }

    @Test
    public void rejectsZipCodeWithoutFiveDigits() {
        assertInvalid(payload("0600", 10, PDF), EnumSet.of(CondominiumField.ZIP_CODE));
        assertInvalid(payload("060001", 10, PDF), EnumSet.of(CondominiumField.ZIP_CODE));
        assertInvalid(payload("06a00", 10, PDF), EnumSet.of(CondominiumField.ZIP_CODE));
    }

    @Test
    public void rejectsMissingUnitsAndDocument() {
        assertInvalid(payload("06000", 0, null), EnumSet.of(CondominiumField.UNITS, CondominiumField.DEED));
    }

    @Test
    public void rejectsBlankState() {
        CondominiumPayload payload = new CondominiumPayload("Torres", "Av. 1", "Centro", "06000", "Cuauhtémoc", "CDMX", "  ",
                10, PDF, PDF, PDF, PDF);
        assertInvalid(payload, EnumSet.of(CondominiumField.STATE));
    }

    @Test
    public void joinsWithCodeIgnoringCaseSpacesAndDash() {
        service.joinCondominium("user", " abc-234 ", " Torre B ", " 302 ");

        assertEquals("ABC234", repository.joinedCode);
        assertEquals("Torre B 302", repository.joinedUnit);
    }

    @Test
    public void unitIsJustTheApartmentWithoutBuilding() {
        service.joinCondominium("user", "ABC234", "  ", "302");

        assertEquals("302", repository.joinedUnit);
    }

    @Test
    public void rejectsMalformedCode() {
        assertThrows(CondominiumNotFound.class, () -> service.joinCondominium("user", "ABC", "", "302"));
        assertThrows(CondominiumNotFound.class, () -> service.joinCondominium("user", "ABC 2345 67", "", "302"));
        assertNull(repository.joinedUnit);
    }

    @Test
    public void rejectsEmptyApartment() {
        assertThrows(EmptyApartment.class, () -> service.joinCondominium("user", "ABC234", "Torre B", " "));
    }

    @Test
    public void onlyTheAdminSeesNeighborsAndJoinRequests() {
        repository.memberships.add(new CondominiumMembership("lived", "Torres", false, 0));
        repository.memberships.add(new CondominiumMembership("administered", "Pinos", true, 0));

        assertThrows(NotCondominiumAdmin.class, () -> service.getAllNeighbors("user", "lived"));
        assertTrue(service.getAllJoinRequests("user", "lived").isEmpty());
        assertEquals("Pinos", service.getAllJoinRequests("user", "administered").get(0).getCondominiumName());
        assertTrue(service.isAdmin("user", "administered"));
        assertFalse(service.isAdmin("user", null));
    }

    @Test
    public void acceptsWithTheSuggestedUnitOrThePickedOne() {
        JoinRequest suggested = new JoinRequest("maria", "maria", "María", null, "c", "Pinos", "5", "unit-5", 0);
        JoinRequest withoutUnit = new JoinRequest("jorge", "jorge", "Jorge", null, "c", "Pinos", "Torre A 8", null, 0);

        service.acceptJoinRequest("admin", suggested, null);
        assertEquals("unit-5", repository.acceptedUnitId);

        service.acceptJoinRequest("admin", withoutUnit, "unit-8");
        assertEquals("unit-8", repository.acceptedUnitId);
    }

    @Test
    public void adminCannotChangeTheirOwnLoanAccess() {
        assertThrows(NotCondominiumAdmin.class, () -> service.modifyLoanAccess("admin", "admin", "condo", false));

        service.modifyLoanAccess("admin", "luis", "condo", false);
        assertEquals("luis", repository.loanAccessUserId);
    }

    @Test
    public void unitNeedsAName() {
        assertThrows(EmptyApartment.class, () -> service.createUnit("condo", "  "));

        service.createUnit("condo", " 101 ");
        assertEquals("101", repository.createdUnit);
    }

    @Test
    public void listsNeedASelectedCondominium() {
        assertTrue(service.getAllJoinRequests("admin", null).isEmpty());
        assertTrue(service.getAllUnits(null).isEmpty());
    }
}
