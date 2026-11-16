package com.startupidea.yipshare.user;

public interface UserRepository {

    User tryLogin(LoginPayload payload);

    User createUser(RegistrationPayload payload);

    UserProfile getUserProfileById(String id);

    void modifyUser(String id, ProfilePayload payload, String newPassword, String currentPassword);

    void deleteUser(String id);

    boolean isActiveUser(String id);

    void logout();
}
