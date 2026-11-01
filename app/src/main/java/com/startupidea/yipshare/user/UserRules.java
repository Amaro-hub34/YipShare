package com.startupidea.yipshare.user;

import java.util.regex.Pattern;

final class UserRules {

    static final int USERNAME_MIN_LENGTH = 6;
    static final int NAME_MAX_LENGTH = 150;
    static final Pattern NAME = Pattern.compile("[\\p{L}\\p{M}]+([ '-][\\p{L}\\p{M}]+)*");
    static final Pattern PASSWORD = Pattern.compile("(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).{8,}");

    private UserRules() {
    }
}
