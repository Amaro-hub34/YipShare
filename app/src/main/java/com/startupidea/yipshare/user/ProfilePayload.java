package com.startupidea.yipshare.user;

public record ProfilePayload(
        String username,
        String firstNames,
        String paternalLastName,
        String maternalLastName,
        String phoneNumber,
        String email) {
}
