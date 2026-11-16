package com.startupidea.yipshare.user;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.ServerError;
import com.startupidea.yipshare.error.ServerUnavailable;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiUserRepository implements UserRepository {

    private static final int UNAUTHORIZED = 401;
    private static final int NOT_FOUND = 404;
    private static final int CONFLICT = 409;
    private static final int BAD_REQUEST = 400;

    private final ApiClient apiClient;
    private final SessionRepository sessionRepository;

    public ApiUserRepository(ApiClient apiClient, SessionRepository sessionRepository) {
        this.apiClient = apiClient;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public User tryLogin(LoginPayload payload) {
        try {
            JSONObject body = new JSONObject()
                    .put("username", payload.getUsername())
                    .put("password", payload.getPassword());
            sessionRepository.saveToken(apiClient.post("/auth/login", body).getString("token"));
            return currentUser();
        } catch (ServerError e) {
            if (e.getStatus() == UNAUTHORIZED || e.getStatus() == NOT_FOUND || e.getStatus() == BAD_REQUEST) return null;
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public User createUser(RegistrationPayload payload) {
        try {
            JSONObject body = new JSONObject()
                    .put("name", payload.getFirstNames())
                    .put("paternalSurname", payload.getPaternalLastName())
                    .put("maternalSurname", payload.getMaternalLastName())
                    .put("email", payload.getEmail())
                    .put("phoneNumber", payload.getPhoneNumber())
                    .put("username", payload.getUsername())
                    .put("password", payload.getPassword());
            sessionRepository.saveToken(apiClient.post("/auth/register", body).getString("token"));
            return currentUser();
        } catch (ServerError e) {
            throw duplicateOr(e);
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public UserProfile getUserProfileById(String id) {
        try {
            boolean self = id.equals(sessionRepository.getUserId());
            JSONObject json = apiClient.getObject(self ? "/users/me" : "/users/" + id + "/profile");
            return new UserProfile(
                    json.getString("userId"),
                    ApiClient.text(json, "username"),
                    ApiClient.text(json, "name"),
                    ApiClient.text(json, "paternalSurname"),
                    ApiClient.text(json, "maternalSurname"),
                    ApiClient.text(json, "phoneNumber"),
                    ApiClient.text(json, "email"),
                    json.optInt("computedRating"));
        } catch (ServerError e) {
            if (e.getStatus() == NOT_FOUND) return null;
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void modifyUser(String id, ProfilePayload payload, String newPassword, String currentPassword) {
        try {
            JSONObject body = new JSONObject()
                    .put("username", payload.getUsername())
                    .put("name", payload.getFirstNames())
                    .put("paternalSurname", payload.getPaternalLastName())
                    .put("maternalSurname", payload.getMaternalLastName())
                    .put("phoneNumber", payload.getPhoneNumber())
                    .put("email", payload.getEmail());
            if (newPassword != null) body.put("password", newPassword).put("currentPassword", currentPassword);
            apiClient.patch("/users/me", body);
        } catch (ServerError e) {
            if (e.getStatus() == UNAUTHORIZED && e.messageContains("password")) throw new WrongPassword();
            throw duplicateOr(e);
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
            return id.equals(currentUser().getId());
        } catch (ServerError e) {
            if (e.getStatus() == UNAUTHORIZED || e.getStatus() == NOT_FOUND) return false;
            throw e;
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
        if (e.messageContains("username")) return new DuplicateUsername();
        if (e.messageContains("email")) return new DuplicateEmail();
        if (e.messageContains("phone")) return new DuplicatePhoneNumber();
        return e;
    }
}
