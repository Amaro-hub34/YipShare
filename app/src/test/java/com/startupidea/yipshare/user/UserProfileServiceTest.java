package com.startupidea.yipshare.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserProfileServiceTest {

    private static final String CURRENT_PASSWORD = "Secreta1!";

    private ProfilePayload savedProfile;
    private String savedPassword;
    private String deletedId;

    private final UserService service = new UserService(new UserRepository() {
        @Override
        public User tryLogin(LoginPayload payload) {
            return null;
        }

        @Override
        public boolean existsUsername(String username) {
            return "taken_user".equals(username) || "ana_lopez".equals(username);
        }

        @Override
        public boolean existsPhoneNumber(String phoneNumber) {
            return "+525598765432".equals(phoneNumber);
        }

        @Override
        public User createUser(RegistrationPayload payload) {
            return null;
        }

        @Override
        public UserProfile getUserProfileById(String id) {
            return "1".equals(id) ? new UserProfile("1", "ana_lopez", "Ana", "López", "Pérez", "+525512345678", 0) : null;
        }

        @Override
        public boolean matchesPassword(String id, String password) {
            return CURRENT_PASSWORD.equals(password);
        }

        @Override
        public void modifyUser(String id, ProfilePayload payload, String newPassword) {
            savedProfile = payload;
            savedPassword = newPassword;
        }

        @Override
        public void deleteUser(String id) {
            deletedId = id;
        }

        @Override
        public boolean isActiveUser(String id) {
            return "1".equals(id);
        }
    });

    private static ProfilePayload profile(String username) {
        return new ProfilePayload(username, " Ana María ", "López", "Pérez", "+525512345678");
    }

    @Test
    public void keepsOwnUsernameAndPasswordWhenUnchanged() {
        service.modifyProfile("1", profile("ana_lopez"), "", "", "");

        assertEquals("ana_lopez", savedProfile.getUsername());
        assertEquals("Ana María", savedProfile.getFirstNames());
        assertNull(savedPassword);
    }

    @Test
    public void changesUsernameWhenFree() {
        service.modifyProfile("1", profile(" nueva_ana "), "", "", "");

        assertEquals("nueva_ana", savedProfile.getUsername());
    }

    @Test
    public void validatesAndNormalizesPhoneNumber() {
        assertThrows(DuplicatePhoneNumber.class, () -> service.modifyProfile(
                "1", new ProfilePayload("ana_lopez", "Ana", "López", "Pérez", "+52 55 9876 5432"), "", "", ""));
        InvalidField badPhone = assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", new ProfilePayload("ana_lopez", "Ana", "López", "Pérez", "123"), "", "", ""));
        assertEquals(RegistrationField.PHONE_NUMBER, badPhone.getField());
        assertNull(savedProfile);

        service.modifyProfile("1", new ProfilePayload("ana_lopez", "Ana", "López", "Pérez", "+52 55 1111 2222"), "", "", "");
        assertEquals("+525511112222", savedProfile.getPhoneNumber());
    }

    @Test
    public void rejectsUsernameOfAnotherUser() {
        assertThrows(DuplicateUsername.class, () -> service.modifyProfile("1", profile("taken_user"), "", "", ""));
        assertNull(savedProfile);
    }

    @Test
    public void rejectsShortUsernameAndInvalidNames() {
        InvalidField shortName = assertThrows(InvalidField.class, () -> service.modifyProfile("1", profile("ana"), "", "", ""));
        assertEquals(RegistrationField.USERNAME, shortName.getField());

        InvalidField badName = assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", new ProfilePayload("ana_lopez", "Ana", "L0pez", "Pérez", "+525512345678"), "", "", ""));
        assertEquals(RegistrationField.PATERNAL_LAST_NAME, badName.getField());
        assertNull(savedProfile);
    }

    @Test
    public void newPasswordNeedsTheCurrentOne() {
        assertThrows(WrongPassword.class, () -> service.modifyProfile("1", profile("ana_lopez"), "Nueva123!", "Nueva123!", "otra"));
        assertNull(savedProfile);

        service.modifyProfile("1", profile("ana_lopez"), "Nueva123!", "Nueva123!", CURRENT_PASSWORD);
        assertEquals("Nueva123!", savedPassword);
    }

    @Test
    public void newPasswordFollowsTheRules() {
        InvalidField error = assertThrows(InvalidField.class,
                () -> service.modifyProfile("1", profile("ana_lopez"), "corta", "corta", CURRENT_PASSWORD));
        assertEquals(RegistrationField.PASSWORD, error.getField());
    }

    @Test
    public void deletesExistingAccountOnly() {
        service.deleteAccount("1");
        assertEquals("1", deletedId);

        assertThrows(AccountNotFound.class, () -> service.deleteAccount("2"));
    }

    @Test
    public void newPasswordMustBeConfirmed() {
        InvalidField error = assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", profile("ana_lopez"), "Nueva123!", "Nueva124!", CURRENT_PASSWORD));
        assertEquals(RegistrationField.PASSWORD_CONFIRMATION, error.getField());

        InvalidField onlyConfirmation = assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", profile("ana_lopez"), "", "Nueva123!", CURRENT_PASSWORD));
        assertEquals(RegistrationField.PASSWORD, onlyConfirmation.getField());
        assertNull(savedProfile);
    }

    @Test
    public void newPasswordCannotBeTheCurrentOne() {
        InvalidField error = assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", profile("ana_lopez"), CURRENT_PASSWORD, CURRENT_PASSWORD, CURRENT_PASSWORD));
        assertEquals(RegistrationField.PASSWORD, error.getField());
        assertNull(savedProfile);
    }

    @Test
    public void savedSessionCountsOnlyForAnActiveAccount() {
        assertTrue(service.isActiveSession("1"));
        assertFalse(service.isActiveSession("deleted-or-missing"));
        assertFalse(service.isActiveSession(null));
    }
}
