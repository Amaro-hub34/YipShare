package com.startupidea.yipshare.condominium;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class CondominiumNotFound extends AppException {

    public CondominiumNotFound() {
        super("No approved condominium has that code", R.string.tv_error_codigo);
    }
}
