package com.startupidea.yipshare.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class UserServiceTest {

    private static UserService serviceWith(User loginResult, boolean usernameExists) {
        return serviceWith(loginResult, usernameExists, false);
    }

    private static UserService serviceWith(User loginResult, boolean usernameExists, boolean phoneExists) {
        return new UserService(new UserRepository() {
            @Override
            public User tryLogin(LoginPayload payload) {
                return loginResult;
            }

            @Override
            public boolean existsUsername(String username) {
                return usernameExists;
            }

            @Override
            public boolean existsPhoneNumber(String phoneNumber) {
                return phoneExists;
            }

            @Override
            public User createUser(RegistrationPayload payload) {
                return new User("new", payload.getUsername());
            }

            @Override
            public UserProfile getUserProfileById(String id) {
                return "1".equals(id) ? new UserProfile("1", "ana", "Ana", "Pérez", "López", "+525512345678", 45) : null;
            }

            @Override
            public boolean matchesPassword(String id, String password) {
                return false;
            }

            @Override
            public void modifyUser(String id, ProfilePayload payload, String newPassword) {
            }

            @Override
            public void deleteUser(String id) {
            }

            @Override
            public boolean isActiveUser(String id) {
                return "1".equals(id);
            }
        });
    }

    @Test
    public void loginRejectsBlankUsername() {
        UserService service = serviceWith(new User("1", "ana"), false);
        assertThrows(EmptyUsername.class, () -> service.login(new LoginPayload("  ", "secret")));
    }

    @Test
    public void loginRejectsBlankPassword() {
        UserService service = serviceWith(new User("1", "ana"), false);
        assertThrows(EmptyPassword.class, () -> service.login(new LoginPayload("ana", "  ")));
    }

    @Test
    public void loginRejectsUnknownCredentials() {
        UserService service = serviceWith(null, false);
        assertThrows(AccountNotFound.class, () -> service.login(new LoginPayload("anitas", "Secret1!")));
    }

    @Test
    public void loginRejectsMalformedCredentialsAsAccountNotFound() {
        UserService service = serviceWith(new User("1", "anitas"), false);
        assertThrows(AccountNotFound.class, () -> service.login(new LoginPayload("anita", "Secret1!")));
        assertThrows(AccountNotFound.class, () -> service.login(new LoginPayload("anitas", "secret")));
    }

    @Test
    public void loginReturnsUser() {
        User user = new User("1", "ana");
        assertEquals(user, serviceWith(user, false).login(new LoginPayload("anitas", "Secret1!")));
    }

    @Test
    public void ensureUsernameAvailableRejectsDuplicate() {
        UserService service = serviceWith(null, true);
        assertThrows(DuplicateUsername.class, () -> service.ensureUsernameAvailable("ana"));
    }

    private static RegistrationDraft names(String paternal, String maternal, String first) {
        return RegistrationDraft.EMPTY.withNames(paternal, maternal, first);
    }

    private static RegistrationDraft credentials(String username, String password, String confirmation) {
        return credentials(username, password, confirmation, "+525512345678");
    }

    private static RegistrationDraft credentials(String username, String password, String confirmation, String phone) {
        return names("Pérez", "De la Cruz", "Ana María").withCredentials(username, password, confirmation, phone);
    }

    private static RegistrationField failingField(Runnable action) {
        return assertThrows(InvalidField.class, action::run).getField();
    }

    @Test
    public void validateNamesAcceptsAccentsAndSpaces() {
        serviceWith(null, false).validateNames(names("Núñez", "De la Cruz", "José María"));
    }

    @Test
    public void validateNamesRejectsEmptyDigitsAndTooLong() {
        UserService service = serviceWith(null, false);
        String tooLong = "a".repeat(151);

        assertEquals(RegistrationField.PATERNAL_LAST_NAME,
                failingField(() -> service.validateNames(names(" ", "Lopez", "Ana"))));
        assertEquals(RegistrationField.MATERNAL_LAST_NAME,
                failingField(() -> service.validateNames(names("Perez", "L0pez", "Ana"))));
        assertEquals(RegistrationField.FIRST_NAMES,
                failingField(() -> service.validateNames(names("Perez", "Lopez", tooLong))));
        service.validateNames(names("Perez", "Lopez", "a".repeat(150)));
    }

    @Test
    public void validateCredentialsEnforcesUsernameLength() {
        UserService service = serviceWith(null, false);
        assertEquals(RegistrationField.USERNAME,
                failingField(() -> service.validateCredentials(credentials("anita", "Secret1!", "Secret1!"))));
        service.validateCredentials(credentials("anitas", "Secret1!", "Secret1!"));
    }

    @Test
    public void validateCredentialsEnforcesPasswordRules() {
        UserService service = serviceWith(null, false);
        for (String password : new String[]{"Secr1!", "secret1!", "Secretos!", "Secret123", "Secret 12"}) {
            assertEquals(password, RegistrationField.PASSWORD,
                    failingField(() -> service.validateCredentials(credentials("anitas", password, password))));
        }
    }

    @Test
    public void validateCredentialsRejectsMismatchedConfirmation() {
        UserService service = serviceWith(null, false);
        assertEquals(RegistrationField.PASSWORD_CONFIRMATION,
                failingField(() -> service.validateCredentials(credentials("anitas", "Secret1!", "Secret2!"))));
    }

    @Test
    public void validateCredentialsRequiresValidPhoneForCountry() {
        UserService service = serviceWith(null, false);
        for (String phone : new String[]{"", "+52", "+52551234567", "+5255123456789", "5512345678", "+52 55123a"}) {
            assertEquals(phone, RegistrationField.PHONE_NUMBER,
                    failingField(() -> service.validateCredentials(credentials("anitas", "Secret1!", "Secret1!", phone))));
        }
        service.validateCredentials(credentials("anitas", "Secret1!", "Secret1!", "+52 55 1234 5678"));
        service.validateCredentials(credentials("anitas", "Secret1!", "Secret1!", "+14155552671"));
    }

    @Test
    public void toPayloadStoresPhoneInE164() {
        assertEquals("+525512345678",
                credentials("anitas", "Secret1!", "Secret1!", "+52 55-1234-5678").toPayload().getPhoneNumber());
    }

    @Test
    public void registerRejectsDuplicateUsername() {
        UserService service = serviceWith(null, true);
        assertThrows(DuplicateUsername.class,
                () -> service.register(credentials("anitas", "Secret1!", "Secret1!")));
    }

    @Test
    public void registerRejectsDuplicatePhoneNumber() {
        UserService service = serviceWith(null, false, true);
        assertThrows(DuplicatePhoneNumber.class,
                () -> service.register(credentials("anitas", "Secret1!", "Secret1!")));
    }

    @Test
    public void registerCreatesUserWithTrimmedUsername() {
        User user = serviceWith(null, false).register(credentials(" anitas ", "Secret1!", "Secret1!"));
        assertEquals("anitas", user.getUsername());
    }

    @Test
    public void returnsProfileWithRatingInStars() {
        UserProfile profile = serviceWith(null, false).getUserProfile("1");

        assertEquals("Ana", profile.getFirstNames());
        assertEquals(4.5f, profile.getRating(), 0.001f);
    }

    @Test
    public void unknownOrMissingSessionHasNoProfile() {
        assertThrows(AccountNotFound.class, () -> serviceWith(null, false).getUserProfile("2"));
        assertThrows(AccountNotFound.class, () -> serviceWith(null, false).getUserProfile(null));
    }
}
