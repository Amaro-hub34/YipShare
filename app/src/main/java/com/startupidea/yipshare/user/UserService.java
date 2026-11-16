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
        validateEmail(draft.getEmail().trim());
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
        return userRepository.createUser(draft.toPayload());
    }

    public void modifyProfile(String userId, ProfilePayload payload, String newPassword,
                              String passwordConfirmation, String currentPassword) {
        String username = payload.getUsername().trim();

        validateUsername(username);
        validateName(payload.getPaternalLastName(), RegistrationField.PATERNAL_LAST_NAME);
        validateName(payload.getMaternalLastName(), RegistrationField.MATERNAL_LAST_NAME);
        validateName(payload.getFirstNames(), RegistrationField.FIRST_NAMES);
        String phoneNumber = UserRules.normalizePhoneNumber(payload.getPhoneNumber());
        if (phoneNumber == null) {
            throw new InvalidField(RegistrationField.PHONE_NUMBER, R.string.error_telefono_formato);
        }
        String email = payload.getEmail().trim();
        validateEmail(email);

        boolean changesPassword = !newPassword.isEmpty() || !passwordConfirmation.isEmpty();
        if (changesPassword) {
            validatePassword(newPassword);
            if (!newPassword.equals(passwordConfirmation)) {
                throw new InvalidField(RegistrationField.PASSWORD_CONFIRMATION, R.string.error_contrasena_no_coincide);
            }
            if (currentPassword.isEmpty()) throw new WrongPassword();
            if (newPassword.equals(currentPassword)) {
                throw new InvalidField(RegistrationField.PASSWORD, R.string.error_contrasena_igual_actual);
            }
        }
        userRepository.modifyUser(userId, new ProfilePayload(
                username,
                payload.getFirstNames().trim(),
                payload.getPaternalLastName().trim(),
                payload.getMaternalLastName().trim(),
                phoneNumber,
                email), changesPassword ? newPassword : null, changesPassword ? currentPassword : null);
    }

    public void deleteAccount(String userId) {
        userRepository.deleteUser(userId);
    }

    public void logout() {
        userRepository.logout();
    }

    private void validateUsername(String username) {
        if (username.isEmpty()) throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_vacio);
        if (username.length() < UserRules.USERNAME_MIN_LENGTH) {
            throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_corto);
        }
        if (username.length() > UserRules.USERNAME_MAX_LENGTH || !UserRules.USERNAME.matcher(username).matches()) {
            throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_formato);
        }
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) throw new InvalidField(RegistrationField.EMAIL, R.string.error_campo_obligatorio);
        if (!UserRules.EMAIL.matcher(email).matches()) throw new InvalidField(RegistrationField.EMAIL, R.string.error_correo_formato);
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
