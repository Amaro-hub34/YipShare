package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class DuplicateEmail extends AppException {

    public DuplicateEmail() {
        super("Email already exists", R.string.error_correo_duplicado);
    }
}
