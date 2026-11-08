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

    private static final String APPROVED_CODE = "ABC234";

    private final FakeCondominiumRepository repository = new FakeCondominiumRepository();
    private final CondominiumService service = new CondominiumService(repository);

    {
        repository.approvedCode = APPROVED_CODE;
    }

    private static CondominiumPayload payload(String zipCode, int units, String deed) {
        return new CondominiumPayload("Torres", "Av. 1", "Centro", zipCode, "Cuauhtémoc", "CDMX",
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
        assertEquals("user", repository.sentUserId);
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
    public void joinsWithApprovedCodeIgnoringCaseAndSpaces() {
        service.joinCondominium("user", " abc234 ", " Torre B ", " 302 ");

        assertEquals("Torre B|302", repository.joinedUnit);
    }

    @Test
    public void rejectsUnknownOrMalformedCode() {
        assertThrows(CondominiumNotFound.class, () -> service.joinCondominium("user", "ZZZ999", "", "302"));
        assertThrows(CondominiumNotFound.class, () -> service.joinCondominium("user", "ABC", "", "302"));
        assertNull(repository.joinedUnit);
    }

    @Test
    public void rejectsEmptyApartment() {
        assertThrows(EmptyApartment.class, () -> service.joinCondominium("user", APPROVED_CODE, "Torre B", " "));
    }

    @Test
    public void rejectsJoiningTwice() {
        repository.members.add(FakeCondominiumRepository.key("user", "condo"));

        assertThrows(AlreadyJoined.class, () -> service.joinCondominium("user", APPROVED_CODE, "", "302"));
        assertNull(repository.joinedUnit);
    }

    @Test
    public void createsDemoCondominiumsOnlyOnce() {
        service.getAllMemberships("user");
        service.getAllMemberships("user");

        assertEquals(1, repository.demoCalls);
    }

    @Test
    public void onlyTheAdminSeesNeighborsAndAnswersJoinRequests() {
        repository.joinRequests.add(FakeCondominiumRepository.key("maria", "condo"));

        assertThrows(NotCondominiumAdmin.class, () -> service.getAllNeighbors("resident", "condo"));
        assertThrows(NotCondominiumAdmin.class, () -> service.acceptJoinRequest("resident", "maria", "condo"));

        repository.admins.add(FakeCondominiumRepository.key("admin", "condo"));
        service.acceptJoinRequest("admin", "maria", "condo");
        assertTrue(repository.members.contains(FakeCondominiumRepository.key("maria", "condo")));
    }

    @Test
    public void answeredJoinRequestCannotBeAnsweredAgain() {
        repository.admins.add(FakeCondominiumRepository.key("admin", "condo"));

        assertThrows(JoinRequestNotFound.class, () -> service.rejectJoinRequest("admin", "maria", "condo"));
    }

    @Test
    public void adminTakesAwayAndGivesBackLoanAccess() {
        repository.admins.add(FakeCondominiumRepository.key("admin", "condo"));
        repository.members.add(FakeCondominiumRepository.key("luis", "condo"));

        service.modifyLoanAccess("admin", "luis", "condo", false);
        assertFalse(service.canRequestLoans("luis", "condo"));

        service.modifyLoanAccess("admin", "luis", "condo", true);
        assertTrue(service.canRequestLoans("luis", "condo"));
    }

    @Test
    public void residentCannotChangeLoanAccessNorAdminTheirOwn() {
        assertThrows(NotCondominiumAdmin.class, () -> service.modifyLoanAccess("resident", "luis", "condo", false));

        repository.admins.add(FakeCondominiumRepository.key("admin", "condo"));
        assertThrows(NotCondominiumAdmin.class, () -> service.modifyLoanAccess("admin", "admin", "condo", false));
    }

    @Test
    public void joinRequestsNeedASelectedCondominium() {
        assertTrue(service.getAllJoinRequests("admin", null).isEmpty());
    }
}
