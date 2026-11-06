package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class AccountNotFound extends AppException {

    public AccountNotFound() {
        super("Account does not exist", R.string.error_cuenta_no_existe);
    }
}
