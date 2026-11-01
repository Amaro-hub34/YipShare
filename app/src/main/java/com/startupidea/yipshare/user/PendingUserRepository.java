package com.startupidea.yipshare.user;

import java.util.UUID;

// TODO: replace with the real implementation once the database exists.
// It must throw DatabaseUnavailable when the database cannot be reached.
public class PendingUserRepository implements UserRepository {

    @Override
    public User tryLogin(LoginPayload payload) {
        return new User(UUID.randomUUID().toString(), payload.getUsername());
    }

    @Override
    public boolean existsUsername(String username) {
        return false;
    }

    @Override
    public User createUser(RegistrationPayload payload) {
        return new User(UUID.randomUUID().toString(), payload.getUsername());
    }
}
