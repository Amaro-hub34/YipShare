package com.startupidea.yipshare.user;

import java.io.Serializable;

public final class LoginPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public LoginPayload(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
