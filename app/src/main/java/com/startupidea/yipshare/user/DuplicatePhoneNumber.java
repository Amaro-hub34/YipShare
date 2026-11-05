package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class DuplicatePhoneNumber extends AppException {
    public DuplicatePhoneNumber() {
        super("Phone number already exists", R.string.error_telefono_duplicado);
    }
}
