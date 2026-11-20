package com.startupidea.yipshare.user;

import java.util.Optional;

public interface UserRepository {

    Optional<User> tryLogin(LoginPayload payload);

    User createUser(RegistrationPayload payload);

    Optional<UserProfile> getUserProfileById(String id);

    void modifyUser(String id, ProfilePayload payload, String newPassword, String currentPassword);

    void modifyProfilePicture(String id, String imageUri);

    void deleteUser(String id);

    boolean isActiveUser(String id);

    void logout();
}
