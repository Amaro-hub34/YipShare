package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.UnauthorizedException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Optional;

public class UserProfileServiceTest {

    private static final String CURRENT_PASSWORD = "Secreta1!";

    private ProfilePayload savedProfile;
    private String savedPassword;
    private String deletedId;

    private final UserService service = new UserService(new UserRepository() {
        @Override
        public Optional<User> tryLogin(LoginPayload payload) {
            return Optional.empty();
        }

        @Override
        public User createUser(RegistrationPayload payload) {
            return null;
        }

        @Override
        public Optional<UserProfile> getUserProfileById(String id) {
            return "1".equals(id) ? Optional.of(new UserProfile("1", "ana_lopez", "Ana", "López", "Pérez", "+525512345678", "ana@test.com", 0, null)) : Optional.empty();
        }

        @Override
        public void modifyUser(String id, ProfilePayload payload, String newPassword, String currentPassword) {
            if ("taken_user".equals(payload.username())) throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_duplicado);
            if ("+525598765432".equals(payload.phoneNumber())) throw new InvalidField(RegistrationField.PHONE_NUMBER, R.string.error_telefono_duplicado);
            if (newPassword != null && !CURRENT_PASSWORD.equals(currentPassword)) throw new UnauthorizedException("Current password does not match", R.string.error_contrasena_actual);
            savedProfile = payload;
            savedPassword = newPassword;
        }

        @Override
        public void modifyProfilePicture(String id, String imageUri) {
        }

        @Override
        public void deleteUser(String id) {
            deletedId = id;
        }

        @Override
        public void logout() {
        }

        @Override
        public boolean isActiveUser(String id) {
            return "1".equals(id);
        }
    });

    private static ProfilePayload profile(String username) {
        return new ProfilePayload(username, " Ana María ", "López", "Pérez", "+525512345678", " ana@test.com ");
    }

    @Test
    public void keepsOwnUsernameAndPasswordWhenUnchanged() {
        service.modifyProfile("1", profile("ana_lopez"), "", "", "");

        assertEquals("ana_lopez", savedProfile.username());
        assertEquals("Ana María", savedProfile.firstNames());
        assertEquals("ana@test.com", savedProfile.email());
        assertNull(savedPassword);
    }

    @Test
    public void changesUsernameWhenFree() {
        service.modifyProfile("1", profile(" nueva_ana "), "", "", "");

        assertEquals("nueva_ana", savedProfile.username());
    }

    @Test
    public void validatesAndNormalizesPhoneNumber() {
        assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", new ProfilePayload("ana_lopez", "Ana", "López", "Pérez", "+52 55 9876 5432", "ana@test.com"), "", "", ""));
        InvalidField badPhone = assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", new ProfilePayload("ana_lopez", "Ana", "López", "Pérez", "123", "ana@test.com"), "", "", ""));
        assertEquals(RegistrationField.PHONE_NUMBER, badPhone.getField());
        assertNull(savedProfile);

        service.modifyProfile("1", new ProfilePayload("ana_lopez", "Ana", "López", "Pérez", "+52 55 1111 2222", "ana@test.com"), "", "", "");
        assertEquals("+525511112222", savedProfile.phoneNumber());
    }

    @Test
    public void rejectsUsernameOfAnotherUser() {
        assertThrows(InvalidField.class, () -> service.modifyProfile("1", profile("taken_user"), "", "", ""));
        assertNull(savedProfile);
    }

    @Test
    public void rejectsShortUsernameAndInvalidNames() {
        InvalidField shortName = assertThrows(InvalidField.class, () -> service.modifyProfile("1", profile("ana"), "", "", ""));
        assertEquals(RegistrationField.USERNAME, shortName.getField());

        InvalidField badName = assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", new ProfilePayload("ana_lopez", "Ana", "L0pez", "Pérez", "+525512345678", "ana@test.com"), "", "", ""));
        assertEquals(RegistrationField.PATERNAL_LAST_NAME, badName.getField());
        assertNull(savedProfile);
    }

    @Test
    public void newPasswordNeedsTheCurrentOne() {
        assertThrows(UnauthorizedException.class, () -> service.modifyProfile("1", profile("ana_lopez"), "Nueva123!", "Nueva123!", ""));
        assertThrows(UnauthorizedException.class, () -> service.modifyProfile("1", profile("ana_lopez"), "Nueva123!", "Nueva123!", "otra"));
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
    public void deletesTheAccount() {
        service.deleteAccount("1");
        assertEquals("1", deletedId);
    }

    @Test
    public void rejectsInvalidEmail() {
        InvalidField error = assertThrows(InvalidField.class, () -> service.modifyProfile(
                "1", new ProfilePayload("ana_lopez", "Ana", "López", "Pérez", "+525512345678", "ana@"), "", "", ""));
        assertEquals(RegistrationField.EMAIL, error.getField());
        assertNull(savedProfile);
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
