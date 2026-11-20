package com.startupidea.yipshare.error;

import androidx.annotation.StringRes;

public class ConflictException extends AppException {

    public ConflictException(String message, @StringRes int messageRes) {
        super(message, messageRes);
    }
}
