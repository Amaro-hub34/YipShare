package com.startupidea.yipshare.user;

import java.io.Serializable;

public final class RegistrationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String paternalLastName;
    private final String maternalLastName;
    private final String firstNames;
    private final String username;
    private final String password;
    private final String phoneNumber;

    public RegistrationPayload(
            String paternalLastName,
            String maternalLastName,
            String firstNames,
            String username,
            String password,
            String phoneNumber) {
        this.paternalLastName = paternalLastName;
        this.maternalLastName = maternalLastName;
        this.firstNames = firstNames;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public String getPaternalLastName() {
        return paternalLastName;
    }

    public String getMaternalLastName() {
        return maternalLastName;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
