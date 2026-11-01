package com.startupidea.yipshare.user;

import androidx.annotation.StringRes;

import com.startupidea.yipshare.error.AppException;

public class InvalidField extends AppException {

    private final RegistrationField field;

    public InvalidField(RegistrationField field, @StringRes int messageRes) {
        super("Invalid value for " + field, messageRes);
        this.field = field;
    }

    public RegistrationField getField() {
        return field;
    }
}
