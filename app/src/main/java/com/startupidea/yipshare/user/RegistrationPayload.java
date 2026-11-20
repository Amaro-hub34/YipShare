package com.startupidea.yipshare.user;

import java.io.Serializable;

public record RegistrationPayload(
        String paternalLastName,
        String maternalLastName,
        String firstNames,
        String username,
        String password,
        String phoneNumber,
        String email) implements Serializable {

    private static final long serialVersionUID = 1L;
}
