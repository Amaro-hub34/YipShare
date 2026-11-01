package com.startupidea.yipshare.error;

import androidx.annotation.StringRes;

public class AppException extends RuntimeException {

    private final int messageRes;

    public AppException(String internalMessage, @StringRes int messageRes) {
        super(internalMessage);
        this.messageRes = messageRes;
    }

    @StringRes
    public int getMessageRes() {
        return messageRes;
    }
}
