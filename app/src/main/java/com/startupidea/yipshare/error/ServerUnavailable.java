package com.startupidea.yipshare.error;

import com.startupidea.yipshare.R;

public class ServerUnavailable extends AppException {

    public ServerUnavailable() {
        super("Server is unavailable", R.string.error_servidor_no_disponible);
    }
}
