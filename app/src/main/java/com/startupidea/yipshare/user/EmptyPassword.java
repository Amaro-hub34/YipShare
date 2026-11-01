package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class EmptyPassword extends AppException {

    public EmptyPassword() {
        super("Password is empty", R.string.error_contrasena_vacia);
    }
}
