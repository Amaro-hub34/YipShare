package com.startupidea.yipshare.user;

public interface UserRepository {

    User tryLogin(LoginPayload payload);

    boolean existsUsername(String username);

    boolean existsPhoneNumber(String phoneNumber);

    User createUser(RegistrationPayload payload);

    UserProfile getUserProfileById(String id);

    boolean matchesPassword(String id, String password);

    void modifyUser(String id, ProfilePayload payload, String newPassword);

    void deleteUser(String id);

    boolean isActiveUser(String id);
}
