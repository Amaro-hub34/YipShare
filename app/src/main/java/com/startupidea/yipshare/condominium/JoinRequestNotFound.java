package com.startupidea.yipshare.condominium;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class JoinRequestNotFound extends AppException {

    public JoinRequestNotFound() {
        super("Join request was already answered", R.string.error_solicitud_respondida);
    }
}
