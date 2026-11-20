package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.UnauthorizedException;
import android.content.Context;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.ServerError;
import com.startupidea.yipshare.error.ServerUnavailable;
import com.startupidea.yipshare.utils.ImageFileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class ApiUserRepository implements UserRepository {

    private static final int UNAUTHORIZED = 401;
    private static final int NOT_FOUND = 404;
    private static final int CONFLICT = 409;
    private static final int BAD_REQUEST = 400;

    private final ApiClient apiClient;
    private final SessionRepository sessionRepository;
    private final Context context;

    public ApiUserRepository(ApiClient apiClient, SessionRepository sessionRepository, Context context) {
        this.apiClient = apiClient;
        this.sessionRepository = sessionRepository;
        this.context = context;
    }

    @Override
    public Optional<User> tryLogin(LoginPayload payload) {
        try {
            JSONObject body = new JSONObject()
                    .put("username", payload.username())
                    .put("password", payload.password());
            sessionRepository.saveToken(apiClient.post("/auth/login", body).getString("token"));
            return Optional.of(currentUser());
        } catch (ServerError e) {
            return switch (e.getStatus()) {
                case UNAUTHORIZED, NOT_FOUND, BAD_REQUEST -> Optional.empty();
                default -> throw e;
            };
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public User createUser(RegistrationPayload payload) {
        try {
            JSONObject body = new JSONObject()
                    .put("name", payload.firstNames())
                    .put("paternalSurname", payload.paternalLastName())
                    .put("maternalSurname", payload.maternalLastName())
                    .put("email", payload.email())
                    .put("phoneNumber", payload.phoneNumber())
                    .put("username", payload.username())
                    .put("password", payload.password());
            sessionRepository.saveToken(apiClient.post("/auth/register", body).getString("token"));
            return currentUser();
        } catch (ServerError e) {
            throw duplicateOr(e);
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public Optional<UserProfile> getUserProfileById(String id) {
        try {
            boolean self = id.equals(sessionRepository.getUserId());
            JSONObject json = apiClient.getObject(self ? "/users/me" : "/users/" + id + "/profile");
            return Optional.of(new UserProfile(
                    json.getString("userId"),
                    ApiClient.text(json, "username"),
                    ApiClient.text(json, "name"),
                    ApiClient.text(json, "paternalSurname"),
                    ApiClient.text(json, "maternalSurname"),
                    ApiClient.text(json, "phoneNumber"),
                    ApiClient.text(json, "email"),
                    json.optInt("computedRating"),
                    ImageFileUtil.toLocalUri(context, apiClient, ApiClient.text(json, "profilePicture"))));
        } catch (ServerError e) {
            if (e.getStatus() == NOT_FOUND) return Optional.empty();
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void modifyUser(String id, ProfilePayload payload, String newPassword, String currentPassword) {
        try {
            JSONObject body = new JSONObject()
                    .put("username", payload.username())
                    .put("name", payload.firstNames())
                    .put("paternalSurname", payload.paternalLastName())
                    .put("maternalSurname", payload.maternalLastName())
                    .put("phoneNumber", payload.phoneNumber())
                    .put("email", payload.email());
            if (newPassword != null) body.put("password", newPassword).put("currentPassword", currentPassword);
            apiClient.patch("/users/me", body);
        } catch (ServerError e) {
            if (e.getStatus() == UNAUTHORIZED && e.messageContains("password")) throw new UnauthorizedException("Current password does not match", R.string.error_contrasena_actual);
            throw duplicateOr(e);
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void modifyProfilePicture(String id, String imageUri) {
        try {
            String path = ImageFileUtil.uploadProfilePicture(context, apiClient, imageUri);
            apiClient.patch("/users/me", new JSONObject().put("profilePicture", path));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void deleteUser(String id) {
        apiClient.delete("/users/me");
    }

    @Override
    public boolean isActiveUser(String id) {
        try {
            return id.equals(currentUser().id());
        } catch (ServerError e) {
            return switch (e.getStatus()) {
                case UNAUTHORIZED, NOT_FOUND -> false;
                default -> throw e;
            };
        }
    }

    @Override
    public void logout() {
        try {
            apiClient.post("/auth/logout", null);
        } catch (ServerError | ServerUnavailable ignored) {
        }
    }

    private User currentUser() {
        try {
            JSONObject me = apiClient.getObject("/auth/me");
            return new User(me.getString("userId"), ApiClient.text(me, "username"));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    private static RuntimeException duplicateOr(ServerError e) {
        if (e.getStatus() != CONFLICT) return e;
        if (e.messageContains("username")) return new InvalidField(RegistrationField.USERNAME, R.string.error_usuario_duplicado);
        if (e.messageContains("email")) return new InvalidField(RegistrationField.EMAIL, R.string.error_correo_duplicado);
        if (e.messageContains("phone")) return new InvalidField(RegistrationField.PHONE_NUMBER, R.string.error_telefono_duplicado);
        return e;
    }
}
