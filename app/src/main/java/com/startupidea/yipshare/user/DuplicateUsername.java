package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class DuplicateUsername extends AppException {

    public DuplicateUsername() {
        super("Username already exists", R.string.error_usuario_duplicado);
    }
}
