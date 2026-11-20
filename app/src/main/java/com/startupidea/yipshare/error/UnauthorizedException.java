package com.startupidea.yipshare.error;

import androidx.annotation.StringRes;

public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message, @StringRes int messageRes) {
        super(message, messageRes);
    }
}
