package com.startupidea.yipshare.error;

import androidx.annotation.StringRes;

public class ResourceNotFound extends AppException {

    public ResourceNotFound(String message, @StringRes int messageRes) {
        super(message, messageRes);
    }
}
