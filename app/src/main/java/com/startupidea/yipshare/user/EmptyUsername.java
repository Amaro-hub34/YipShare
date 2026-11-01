package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class EmptyUsername extends AppException {

    public EmptyUsername() {
        super("Username is empty", R.string.error_usuario_vacio);
    }
}
