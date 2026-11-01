package com.startupidea.yipshare.user;

public interface UserRepository {

    User tryLogin(LoginPayload payload);

    boolean existsUsername(String username);

    User createUser(RegistrationPayload payload);
}
