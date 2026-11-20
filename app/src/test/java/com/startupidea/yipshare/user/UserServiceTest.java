package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.ResourceNotFound;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.Optional;

public class UserServiceTest {

    private static UserService serviceWith(User loginResult, boolean usernameExists) {
        return new UserService(new UserRepository() {
            @Override
            public Optional<User> tryLogin(LoginPayload payload) {
                return Optional.ofNullable(loginResult);
            }

            @Override
            public User createUser(RegistrationPayload payload) {
                if (usernameExists) throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_duplicado);
                return new User("new", payload.username());
            }

            @Override
            public Optional<UserProfile> getUserProfileById(String id) {
                return "1".equals(id) ? Optional.of(new UserProfile("1", "ana", "Ana", "Pérez", "López", "+525512345678", "ana@test.com", 45, null)) : Optional.empty();
            }

            @Override
            public void modifyUser(String id, ProfilePayload payload, String newPassword, String currentPassword) {
            }

            @Override
            public void modifyProfilePicture(String id, String imageUri) {
            }

            @Override
            public void deleteUser(String id) {
            }

            @Override
            public boolean isActiveUser(String id) {
                return "1".equals(id);
            }

            @Override
            public void logout() {
            }
        });
    }

    @Test
    public void loginRejectsBlankUsername() {
        UserService service = serviceWith(new User("1", "ana"), false);
        assertThrows(InvalidField.class, () -> service.login(new LoginPayload("  ", "secret")));
    }

    @Test
    public void loginRejectsBlankPassword() {
        UserService service = serviceWith(new User("1", "ana"), false);
        assertThrows(InvalidField.class, () -> service.login(new LoginPayload("ana", "  ")));
    }

    @Test
    public void loginRejectsUnknownCredentials() {
        UserService service = serviceWith(null, false);
        assertThrows(ResourceNotFound.class, () -> service.login(new LoginPayload("anitas", "Secret1!")));
    }

    @Test
    public void loginRejectsMalformedCredentialsAsAccountNotFound() {
        UserService service = serviceWith(new User("1", "anitas"), false);
        assertThrows(ResourceNotFound.class, () -> service.login(new LoginPayload("anita", "Secret1!")));
        assertThrows(ResourceNotFound.class, () -> service.login(new LoginPayload("anitas", "secret")));
    }

    @Test
    public void loginReturnsUser() {
        User user = new User("1", "ana");
        assertEquals(user, serviceWith(user, false).login(new LoginPayload("anitas", "Secret1!")));
    }

    private static RegistrationDraft names(String paternal, String maternal, String first) {
        return RegistrationDraft.EMPTY.withNames(paternal, maternal, first);
    }

    private static RegistrationDraft credentials(String username, String password, String confirmation) {
        return credentials(username, password, confirmation, "+525512345678");
    }

    private static RegistrationDraft credentials(String username, String password, String confirmation, String phone) {
        return names("Pérez", "De la Cruz", "Ana María").withCredentials(username, password, confirmation, phone, "ana@test.com");
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
        for (String password : new String[]{"Secr1!", "secret1!", "Secretos!", "Secret123", "Secret 12", "SECRET1!"}) {
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
    public void validateCredentialsRequiresValidEmail() {
        UserService service = serviceWith(null, false);
        for (String email : new String[]{"", "ana", "ana@test", "ana @test.com"}) {
            RegistrationDraft draft = names("Pérez", "López", "Ana").withCredentials("anitas", "Secret1!", "Secret1!", "+525512345678", email);
            assertEquals(email, RegistrationField.EMAIL, failingField(() -> service.validateCredentials(draft)));
        }
    }

    @Test
    public void validateCredentialsRejectsUsernameTheServerWouldReject() {
        UserService service = serviceWith(null, false);
        assertEquals(RegistrationField.USERNAME,
                failingField(() -> service.validateCredentials(credentials("ana lopez", "Secret1!", "Secret1!"))));
        assertEquals(RegistrationField.USERNAME,
                failingField(() -> service.validateCredentials(credentials("a".repeat(51), "Secret1!", "Secret1!"))));
    }

    @Test
    public void toPayloadStoresPhoneInE164() {
        assertEquals("+525512345678",
                credentials("anitas", "Secret1!", "Secret1!", "+52 55-1234-5678").toPayload().phoneNumber());
    }

    @Test
    public void registerRejectsDuplicateUsername() {
        UserService service = serviceWith(null, true);
        assertThrows(InvalidField.class,
                () -> service.register(credentials("anitas", "Secret1!", "Secret1!")));
    }

    @Test
    public void registerCreatesUserWithTrimmedUsername() {
        User user = serviceWith(null, false).register(credentials(" anitas ", "Secret1!", "Secret1!"));
        assertEquals("anitas", user.username());
    }

    @Test
    public void returnsProfileWithRatingInStars() {
        UserProfile profile = serviceWith(null, false).getUserProfile("1");

        assertEquals("Ana", profile.firstNames());
        assertEquals(4.5f, profile.getRating(), 0.001f);
    }

    @Test
    public void unknownOrMissingSessionHasNoProfile() {
        assertThrows(ResourceNotFound.class, () -> serviceWith(null, false).getUserProfile("2"));
        assertThrows(ResourceNotFound.class, () -> serviceWith(null, false).getUserProfile(null));
    }
}
