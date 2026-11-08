package com.startupidea.yipshare.user;

public final class ProfilePayload {

    private final String username;
    private final String firstNames;
    private final String paternalLastName;
    private final String maternalLastName;

    public ProfilePayload(String username, String firstNames, String paternalLastName, String maternalLastName) {
        this.username = username;
        this.firstNames = firstNames;
        this.paternalLastName = paternalLastName;
        this.maternalLastName = maternalLastName;
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
}
