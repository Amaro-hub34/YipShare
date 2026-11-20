package com.startupidea.yipshare.error;

import androidx.annotation.StringRes;

public class BadRequestException extends AppException {

    public BadRequestException(String message, @StringRes int messageRes) {
        super(message, messageRes);
    }
}
