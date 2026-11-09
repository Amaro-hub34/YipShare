package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(LoginPayload payload) {
        if (payload.getUsername().trim().isEmpty()) throw new EmptyUsername();
        if (payload.getPassword().trim().isEmpty()) throw new EmptyPassword();
        // An account that breaks the registration rules can't exist; skip the DB lookup.
        if (payload.getUsername().trim().length() < UserRules.USERNAME_MIN_LENGTH
                || !UserRules.PASSWORD.matcher(payload.getPassword()).matches()) {
            throw new AccountNotFound();
        }

        User user = userRepository.tryLogin(payload);
        if (user == null) throw new AccountNotFound();
        return user;
    }

    public void validateNames(RegistrationDraft draft) {
        validateName(draft.getPaternalLastName(), RegistrationField.PATERNAL_LAST_NAME);
        validateName(draft.getMaternalLastName(), RegistrationField.MATERNAL_LAST_NAME);
        validateName(draft.getFirstNames(), RegistrationField.FIRST_NAMES);
    }

    public void validateCredentials(RegistrationDraft draft) {
        String password = draft.getPassword();

        validateUsername(draft.getUsername().trim());
        validatePassword(password);
        if (!password.equals(draft.getPasswordConfirmation())) {
            throw new InvalidField(RegistrationField.PASSWORD_CONFIRMATION, R.string.error_contrasena_no_coincide);
        }
        if (UserRules.normalizePhoneNumber(draft.getPhoneNumber()) == null) {
            throw new InvalidField(RegistrationField.PHONE_NUMBER, R.string.error_telefono_formato);
        }
    }

    public boolean isActiveSession(String userId) {
        return userId != null && userRepository.isActiveUser(userId);
    }

    public UserProfile getUserProfile(String id) {
        UserProfile profile = id == null ? null : userRepository.getUserProfileById(id);
        if (profile == null) throw new AccountNotFound();
        return profile;
    }

    public User register(RegistrationDraft draft) {
        validateNames(draft);
        validateCredentials(draft);

        RegistrationPayload payload = draft.toPayload();
        ensureUsernameAvailable(payload.getUsername());
        ensurePhoneNumberAvailable(payload.getPhoneNumber());
        return userRepository.createUser(payload);
    }

    /**
     * Updates the logged-in user's data. Empty password fields keep the current password; a new one
     * must be confirmed, needs the current password and must be different from it. Everything points to user_id, so reviews and reports stay with the account.
     */
    public void modifyProfile(String userId, ProfilePayload payload, String newPassword,
                              String passwordConfirmation, String currentPassword) {
        UserProfile current = getUserProfile(userId);
        String username = payload.getUsername().trim();

        validateUsername(username);
        validateName(payload.getPaternalLastName(), RegistrationField.PATERNAL_LAST_NAME);
        validateName(payload.getMaternalLastName(), RegistrationField.MATERNAL_LAST_NAME);
        validateName(payload.getFirstNames(), RegistrationField.FIRST_NAMES);
        String phoneNumber = UserRules.normalizePhoneNumber(payload.getPhoneNumber());
        if (phoneNumber == null) {
            throw new InvalidField(RegistrationField.PHONE_NUMBER, R.string.error_telefono_formato);
        }

        boolean changesPassword = !newPassword.isEmpty() || !passwordConfirmation.isEmpty();
        if (changesPassword) {
            validatePassword(newPassword);
            if (!newPassword.equals(passwordConfirmation)) {
                throw new InvalidField(RegistrationField.PASSWORD_CONFIRMATION, R.string.error_contrasena_no_coincide);
            }
            if (!userRepository.matchesPassword(userId, currentPassword)) throw new WrongPassword();
            if (newPassword.equals(currentPassword)) {
                throw new InvalidField(RegistrationField.PASSWORD, R.string.error_contrasena_igual_actual);
            }
        }
        if (!username.equals(current.getUsername())) ensureUsernameAvailable(username);
        if (!phoneNumber.equals(current.getPhoneNumber())) ensurePhoneNumberAvailable(phoneNumber);

        userRepository.modifyUser(userId, new ProfilePayload(
                username,
                payload.getFirstNames().trim(),
                payload.getPaternalLastName().trim(),
                payload.getMaternalLastName().trim(),
                phoneNumber), changesPassword ? newPassword : null);
    }

    /** Logical delete: the data stays (reviews, reports, loans) but the account can no longer log in. */
    public void deleteAccount(String userId) {
        getUserProfile(userId);
        userRepository.deleteUser(userId);
    }

    public void ensureUsernameAvailable(String username) {
        if (userRepository.existsUsername(username)) throw new DuplicateUsername();
    }

    public void ensurePhoneNumberAvailable(String phoneNumber) {
        if (userRepository.existsPhoneNumber(phoneNumber)) throw new DuplicatePhoneNumber();
    }

    private void validateUsername(String username) {
        if (username.isEmpty()) throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_vacio);
        if (username.length() < UserRules.USERNAME_MIN_LENGTH) {
            throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_corto);
        }
    }

    private void validatePassword(String password) {
        if (password.trim().isEmpty()) throw new InvalidField(RegistrationField.PASSWORD, R.string.error_contrasena_vacia);
        if (!UserRules.PASSWORD.matcher(password).matches()) {
            throw new InvalidField(RegistrationField.PASSWORD, R.string.error_contrasena_formato);
        }
    }

    private void validateName(String value, RegistrationField field) {
        String name = value.trim();

        if (name.isEmpty()) throw new InvalidField(field, R.string.error_campo_obligatorio);
        if (name.length() > UserRules.NAME_MAX_LENGTH) throw new InvalidField(field, R.string.error_nombre_largo);
        if (!UserRules.NAME.matcher(name).matches()) throw new InvalidField(field, R.string.error_nombre_formato);
    }
}
