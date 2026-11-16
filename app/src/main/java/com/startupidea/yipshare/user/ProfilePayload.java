package com.startupidea.yipshare.user;

public final class ProfilePayload {

    private final String username;
    private final String firstNames;
    private final String paternalLastName;
    private final String maternalLastName;
    private final String phoneNumber;
    private final String email;

    public ProfilePayload(String username, String firstNames, String paternalLastName, String maternalLastName,
                          String phoneNumber, String email) {
        this.username = username;
        this.firstNames = firstNames;
        this.paternalLastName = paternalLastName;
        this.maternalLastName = maternalLastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getPaternalLastName() {
        return paternalLastName;
    }

    public String getMaternalLastName() {
        return maternalLastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }
}
