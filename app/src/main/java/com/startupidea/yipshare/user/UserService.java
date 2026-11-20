package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.BadRequestException;
import com.startupidea.yipshare.error.ResourceNotFound;
import com.startupidea.yipshare.error.UnauthorizedException;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(LoginPayload payload) {
        if (payload.username().trim().isEmpty()) throw new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_vacio);
        if (payload.password().trim().isEmpty()) throw new InvalidField(RegistrationField.PASSWORD, R.string.error_contrasena_vacia);
        if (payload.username().trim().length() < UserRules.USERNAME_MIN_LENGTH
                || !UserRules.PASSWORD.matcher(payload.password()).matches()) {
            throw new ResourceNotFound("Account does not exist", R.string.error_cuenta_no_existe);
        }

        return userRepository.tryLogin(payload).orElseThrow(() -> new ResourceNotFound("Account does not exist", R.string.error_cuenta_no_existe));
    }

    public void validateNames(RegistrationDraft draft) {
        validateName(draft.paternalLastName(), RegistrationField.PATERNAL_LAST_NAME);
        validateName(draft.maternalLastName(), RegistrationField.MATERNAL_LAST_NAME);
        validateName(draft.firstNames(), RegistrationField.FIRST_NAMES);
    }

    public void validateCredentials(RegistrationDraft draft) {
        String password = draft.password();

        validateUsername(draft.username().trim());
        validatePassword(password);
        if (!password.equals(draft.passwordConfirmation())) {
            throw new InvalidField(RegistrationField.PASSWORD_CONFIRMATION, R.string.error_contrasena_no_coincide);
        }
        if (UserRules.normalizePhoneNumber(draft.phoneNumber()).isEmpty()) {
            throw new InvalidField(RegistrationField.PHONE_NUMBER, R.string.error_telefono_formato);
        }
        validateEmail(draft.email().trim());
    }

    public boolean isActiveSession(String userId) {
        return userId != null && userRepository.isActiveUser(userId);
    }

    public UserProfile getUserProfile(String id) {
        if (id == null) throw new ResourceNotFound("Account does not exist", R.string.error_cuenta_no_existe);
        return userRepository.getUserProfileById(id).orElseThrow(() -> new ResourceNotFound("Account does not exist", R.string.error_cuenta_no_existe));
    }

    public User register(RegistrationDraft draft) {
        validateNames(draft);
        validateCredentials(draft);
        return userRepository.createUser(draft.toPayload());
    }

    public void modifyProfile(String userId, ProfilePayload payload, String newPassword,
                              String passwordConfirmation, String currentPassword) {
        String username = payload.username().trim();

        validateUsername(username);
        validateName(payload.paternalLastName(), RegistrationField.PATERNAL_LAST_NAME);
        validateName(payload.maternalLastName(), RegistrationField.MATERNAL_LAST_NAME);
        validateName(payload.firstNames(), RegistrationField.FIRST_NAMES);
        String phoneNumber = UserRules.normalizePhoneNumber(payload.phoneNumber())
                .orElseThrow(() -> new InvalidField(RegistrationField.PHONE_NUMBER, R.string.error_telefono_formato));
        String email = payload.email().trim();
        validateEmail(email);

        boolean changesPassword = !newPassword.isEmpty() || !passwordConfirmation.isEmpty();
        if (changesPassword) {
            validatePassword(newPassword);
            if (!newPassword.equals(passwordConfirmation)) {
                throw new InvalidField(RegistrationField.PASSWORD_CONFIRMATION, R.string.error_contrasena_no_coincide);
            }
            if (currentPassword.isEmpty()) throw new UnauthorizedException("Current password does not match", R.string.error_contrasena_actual);
            if (newPassword.equals(currentPassword)) {
                throw new InvalidField(RegistrationField.PASSWORD, R.string.error_contrasena_igual_actual);
            }
        }
        userRepository.modifyUser(userId, new ProfilePayload(
                username,
                payload.firstNames().trim(),
                payload.paternalLastName().trim(),
                payload.maternalLastName().trim(),
                phoneNumber,
                email), changesPassword ? newPassword : null, changesPassword ? currentPassword : null);
    }

    public void modifyProfilePicture(String userId, String imageUri) {
        if (imageUri == null) throw new BadRequestException("Image could not be read", R.string.error_imagen_invalida);
        userRepository.modifyProfilePicture(userId, imageUri);
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
