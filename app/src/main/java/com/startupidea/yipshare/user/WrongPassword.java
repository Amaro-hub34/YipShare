package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class WrongPassword extends AppException {

    public WrongPassword() {
        super("Current password does not match", R.string.error_contrasena_actual);
    }
}
