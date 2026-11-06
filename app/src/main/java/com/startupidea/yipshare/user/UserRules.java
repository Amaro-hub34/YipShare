package com.startupidea.yipshare.user;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.regex.Pattern;

final class UserRules {

    static final int USERNAME_MIN_LENGTH = 6;
    static final int NAME_MAX_LENGTH = 150;
    static final Pattern NAME = Pattern.compile("[\\p{L}\\p{M}]+([ '-][\\p{L}\\p{M}]+)*");
    static final Pattern PASSWORD = Pattern.compile("(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).{8,}");

    /** Returns the number in E.164 (e.g. +525512345678), or null if it's not valid for its country. */
    static String normalizePhoneNumber(String phoneNumber) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = util.parse(phoneNumber, null);
            return util.isValidNumber(number) ? util.format(number, PhoneNumberUtil.PhoneNumberFormat.E164) : null;
        } catch (NumberParseException e) {
            return null;
        }
    }

    private UserRules() {
    }
}
