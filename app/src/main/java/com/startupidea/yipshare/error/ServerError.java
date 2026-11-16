package com.startupidea.yipshare.error;

import com.startupidea.yipshare.R;

import java.util.Locale;

public class ServerError extends AppException {

    private final int status;

    public ServerError(int status, String message) {
        super(message, R.string.error_servidor);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public boolean messageContains(String text) {
        return getMessage() != null && getMessage().toLowerCase(Locale.ROOT).contains(text);
    }
}
