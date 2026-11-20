package com.startupidea.yipshare.user;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Optional;
import java.util.regex.Pattern;

final class UserRules {

    static final int USERNAME_MIN_LENGTH = 6;
    static final int NAME_MAX_LENGTH = 150;
    static final int USERNAME_MAX_LENGTH = 50;
    static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_-]+");
    static final Pattern NAME = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúÑñ]+( [A-Za-zÁÉÍÓÚáéíóúÑñ]+)*");
    static final Pattern PASSWORD = Pattern.compile("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[!-~]{8,255}");
    static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    static Optional<String> normalizePhoneNumber(String phoneNumber) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = util.parse(phoneNumber, null);
            return util.isValidNumber(number)
                    ? Optional.of(util.format(number, PhoneNumberUtil.PhoneNumberFormat.E164))
                    : Optional.empty();
        } catch (NumberParseException e) {
            return Optional.empty();
        }
    }

    private UserRules() {
    }
}
