package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class InvalidCredentials extends AppException {

    public InvalidCredentials() {
        super("Username or password is incorrect", R.string.error_credenciales);
    }
}
