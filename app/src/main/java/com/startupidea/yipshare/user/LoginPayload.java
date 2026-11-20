package com.startupidea.yipshare.user;

import java.io.Serializable;

public record LoginPayload(String username, String password) implements Serializable {

    private static final long serialVersionUID = 1L;
}
