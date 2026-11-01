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

        User user = userRepository.tryLogin(payload);
        if (user == null) throw new InvalidCredentials();
        return user;
    }

    public void validateNames(RegistrationDraft draft) {
        validateName(draft.getPaternalLastName(), RegistrationField.PATERNAL_LAST_NAME);
        validateName(draft.getMaternalLastName(), RegistrationField.MATERNAL_LAST_NAME);
        validateName(draft.getFirstNames(), RegistrationField.FIRST_NAMES);
    }

    public void validateCredentials(RegistrationDraft draft) {
        String username = draft.getUsername().trim();
        String password = draft.getPassword();

        if (username.isEmpty()) {
            throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_vacio);
        }
        if (username.length() < UserRules.USERNAME_MIN_LENGTH) {
            throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_corto);
        }
        if (password.trim().isEmpty()) {
            throw new InvalidField(RegistrationField.PASSWORD, R.string.error_contrasena_vacia);
        }
        if (!UserRules.PASSWORD.matcher(password).matches()) {
            throw new InvalidField(RegistrationField.PASSWORD, R.string.error_contrasena_formato);
        }
        if (!password.equals(draft.getPasswordConfirmation())) {
            throw new InvalidField(RegistrationField.PASSWORD_CONFIRMATION, R.string.error_contrasena_no_coincide);
        }
    }

    public User register(RegistrationDraft draft) {
        validateNames(draft);
        validateCredentials(draft);

        RegistrationPayload payload = draft.toPayload();
        ensureUsernameAvailable(payload.getUsername());
        return userRepository.createUser(payload);
    }

    public void ensureUsernameAvailable(String username) {
        if (userRepository.existsUsername(username)) throw new DuplicateUsername();
    }

    private void validateName(String value, RegistrationField field) {
        String name = value.trim();

        if (name.isEmpty()) throw new InvalidField(field, R.string.error_campo_obligatorio);
        if (name.length() > UserRules.NAME_MAX_LENGTH) throw new InvalidField(field, R.string.error_nombre_largo);
        if (!UserRules.NAME.matcher(name).matches()) throw new InvalidField(field, R.string.error_nombre_formato);
    }
}
