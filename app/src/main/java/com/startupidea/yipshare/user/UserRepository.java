package com.startupidea.yipshare.user;

public interface UserRepository {

    User tryLogin(LoginPayload payload);

    boolean existsUsername(String username);

    boolean existsPhoneNumber(String phoneNumber);

    User createUser(RegistrationPayload payload);
}
